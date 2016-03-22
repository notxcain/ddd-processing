package com.qiwi.processing.account

import cats.data.Xor
import com.qiwi.processing.account.Account.State
import com.qiwi.processing.common.Amount
import com.qiwi.processing.payment._
import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.AggregateReaction.{Accept, Collaborate, Ignore, Reject}
import pl.newicom.dddd.aggregate.{AggregateReaction, AggregateRootBehavior}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import Reactions._
import shapeless.ops.coproduct.Inject
import shapeless.{:+:, CNil, Coproduct, Inl, Inr}

trait Behavior[S, C, E, R] {
  def handle: C => AggregateReaction[E, R]
  def apply: E => S
}

object Behavior {
  def apply[S, C, E, R](handleCommand: => HandleCommand[C, E, R], applyEvent: => ApplyEvent[S, E]): Behavior[S, C, E, R] = new Behavior[S, C, E, R] {
    override def handle: (C) => AggregateReaction[E, R] = handleCommand.handleCommand
    override def apply: (E) => S = applyEvent.applyEvent
  }
}

object HandleCommand {
  def apply[C, E, R](f: C => AggregateReaction[E, R]): HandleCommand[C, E, R] = new HandleCommand[C, E, R] {
    override val handleCommand: (C) => AggregateReaction[E, R] = f
  }
}

trait HandleCommand[-C, +E, +R] { outer =>
  val handleCommand: C => AggregateReaction[E, R]
  def ~[C1, E1, R1](x: HandleCommand[C1, E1, R1]): HandleCommand[C :+: C1 :+: CNil, E :+: E1 :+: CNil, R :+: R1 :+: CNil] =
    new HandleCommand[C :+: C1 :+: CNil, E :+: E1 :+: CNil, R :+: R1 :+: CNil] {
      override val handleCommand: (:+:[C, :+:[C1, CNil]]) => AggregateReaction[:+:[E, :+:[E1, CNil]], :+:[R, :+:[R1, CNil]]] = {
        case Inl(c) =>
          outer.handleCommand(c).map(Coproduct[E :+: E1 :+: CNil](_)).leftMap(Coproduct[R :+: R1 :+: CNil](_))
        case Inr(Inl(c)) =>
          x.handleCommand(c).map(Coproduct[E :+: E1 :+: CNil](_)).leftMap(Coproduct[R :+: R1 :+: CNil](_))
        case other => ???
      }
    }
}

object ApplyEvent {
  def apply[S, E](f: E => S): ApplyEvent[S, E] = new ApplyEvent[S, E] {
    override val applyEvent: (E) => S = f
  }
}

trait ApplyEvent[+S, -E] {
  val applyEvent: E => S
}

class AggregateReactionOps[E, R](val self: AggregateReaction[E, R]) extends AnyVal {
  def map[E1](f: E => E1): AggregateReaction[E1, R] = self match {
    case Accept(e) => Accept(f(e))
    case Collaborate(future) => Collaborate { implicit e =>
      future(e).map(_.map(f))
    }
    case other => other.asInstanceOf[AggregateReaction[E1, R]]
  }
  def leftMap[R1](f: R => R1): AggregateReaction[E, R1] = self match {
    case Reject(r) => Reject(f(r))
    case Collaborate(future) => Collaborate { implicit e =>
      future(e).map(_.leftMap(f))
    }
    case other => other.asInstanceOf[AggregateReaction[E, R1]]
  }
}

object Reactions {
  def accept[E, R](e: E): AggregateReaction[E, R] = Accept(e)
  def reject[E, R](e: R): AggregateReaction[E, R] = Reject(e)
  def collaborate[E, R](f: ExecutionContext => Future[AggregateReaction[E, R]]): AggregateReaction[E, R] = Collaborate(f)
  def ignore[E, R]: AggregateReaction[E, R] = Ignore
  implicit def toOps[E, R](a: AggregateReaction[E, R]): AggregateReactionOps[E, R] = new AggregateReactionOps(a)
}

class ARTotalBehavior[S, C <: aggregate.Command, E <: aggregate.DomainEvent, R, IC <: C : ClassTag, IE <: E : ClassTag, IR <: R]
(initialBehavior: Behavior[S, IC, IE, IR], withState: S => Behavior[S, C, E, R]) extends AggregateRootBehavior[S, C, E, R] {
  override def processCommand(state: S): ProcessCommand = withState(state).handle

  override def applyEvent(state: S): ApplyEvent = withState(state).apply

  override def processFirstCommand: ProcessFirstCommand = {
    case c: IC =>
      initialBehavior.handle(c)
  }

  override def applyFirstEvent: ApplyFirstEvent = {
    case e: IE =>
      initialBehavior.apply(e)
  }
}

object Account {
  case class State(accountId: AccountId, balance: Amount, holds: Map[PaymentId, Amount], processedPayments: Set[PaymentId]) {
    def credit(paymentId: PaymentId, amount: Amount): State = copy(balance = balance + amount, processedPayments = processedPayments + paymentId)
    def placeHold(paymentId: PaymentId, amount: Amount): State = copy(balance = balance - amount, holds = holds + (paymentId -> amount))
    def debit(paymentId: PaymentId): State = copy(holds = holds - paymentId, processedPayments = processedPayments + paymentId)
  }
}

class Account extends ARTotalBehavior[State, Command, Event, Rejection, CreditAccount, AccountCredited, Nothing](
  initialBehavior = Behavior(
    HandleCommand {
      case CreditAccount(accountId, txn, amount) =>
        accept(AccountCredited(accountId, txn, amount))
    },
    ApplyEvent {
      case AccountCredited(accountId, paymentId, amount) =>
        State(accountId, amount, Map.empty, Set(paymentId))
    }
  ),
  withState = { state =>
    Behavior(
      HandleCommand {
        case CreditAccount(accountId, paymentId, amount) =>
          if (state.processedPayments(paymentId))
            reject(DuplicateCommand(accountId, paymentId))
          else
            accept(AccountCredited(accountId, paymentId, amount))

        case PlaceHold(_, paymentId, amount) =>
          if (state.holds.contains(paymentId) || state.processedPayments.contains(paymentId))
            reject(DuplicateCommand(state.accountId, paymentId))
          else if (state.balance > amount)
            accept(HoldPlaced(state.accountId, paymentId, amount))
          else
            reject(InsufficientFunds(state.accountId, paymentId))

        case DebitAccount(accountId, paymentId) =>
          state.holds.get(paymentId) match {
            case Some(amount) => accept(AccountDebited(state.accountId, paymentId))
            case None => reject(HoldMissing(accountId, paymentId))
          }
      },
      ApplyEvent {
        case e: AccountCredited => state.credit(e.paymentId, e.amount)
        case e: HoldPlaced => state.placeHold(e.paymentId, e.amount)
        case e: AccountDebited => state.debit(e.paymentId)
      }
    )
  }
)