package com.qiwi.processing.account

import com.qiwi.processing.common.Amount
import com.qiwi.processing.payment.PaymentId
import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate._
import pl.newicom.dddd.office.{AggregateContract, OfficeInfo}

case class AccountId(value: String) extends AnyVal

trait AccountOffice

object AccountOffice {
  implicit val info: OfficeInfo[AccountOffice] = new OfficeInfo[AccountOffice] {
    override def name: String = "Payment"
  }

  implicit val contract: AggregateContract.Aux[AccountOffice, Command, Event, Rejection] = new AggregateContract[AccountOffice] {
    override type C = Command
    override type R = Rejection
    override type E = Event
  }
}


sealed trait Command extends aggregate.Command {
  def accountId: AccountId
  override def aggregateId: EntityId = accountId.value
}

case class PlaceHold(accountId: AccountId, paymentId: PaymentId, amount: Amount) extends Command
case class DebitAccount(accountId: AccountId, paymentId: PaymentId) extends Command
case class CreditAccount(accountId: AccountId, paymentId: PaymentId, amount: Amount) extends Command


sealed trait Event extends DomainEvent {
  def accountId: AccountId
  override def aggregateId: EntityId = accountId.toString
}
case class HoldPlaced(accountId: AccountId, paymentId: PaymentId, amount: Amount) extends Event
case class AccountDebited(accountId: AccountId, paymentId: PaymentId) extends Event
case class AccountCredited(accountId: AccountId, paymentId: PaymentId, amount: Amount) extends Event


sealed trait Rejection
case class InsufficientFunds(accountId: AccountId, paymentId: PaymentId) extends Rejection
case class HoldExists(accountId: AccountId, paymentId: PaymentId) extends Rejection
case class HoldMissing(accountId: AccountId, paymentId: PaymentId) extends Rejection
case class AccountNotFound(accountId: AccountId) extends Rejection
case class DuplicateCommand(accountId: AccountId, paymentId: PaymentId) extends Rejection