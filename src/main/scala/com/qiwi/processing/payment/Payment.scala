package com.qiwi.processing.payment

import org.slf4j.LoggerFactory
import pl.newicom.dddd.aggregate.AggregateRootBehavior

object Payment {
  case class State(paymentId: PaymentId, status: Status)
}

class Payment extends AggregateRootBehavior[Payment.State, Command, Event, Rejection] {
  import Status._
  import Payment.State

  val log = LoggerFactory.getLogger("Payment")


  override def processFirstCommand = {
    case ReceivePayment(paymentId, from, to, amount) =>
      accept(PaymentReceived(paymentId, from, to, amount))
  }

  override def applyFirstEvent = {
    case e: PaymentReceived => State(e.paymentId, Initial)
  }

  override def processCommand(state: State): ProcessCommand = {
    case c: ReceivePayment  => reject(PaymentAlreadyReceived(state.paymentId, state.status))
    case c: AcceptPayment =>
      if (state.status == Initial)
        accept(PaymentAccepted(state.paymentId))
      else
        reject(PaymentAlreadyAccepted(state.paymentId))
    case c: DeclinePayment =>
      if (state.status == Initial) {
        accept(PaymentDeclined(state.paymentId))
      } else {
        reject(PaymentAlreadyDeclined(state.paymentId))
      }
  }

  override def applyEvent(state: State): ApplyEvent = {
    case e: PaymentReceived => state
    case e: PaymentAccepted => state.copy(status = Accepted)
    case e: PaymentDeclined => state.copy(status = Declined)
  }
}
