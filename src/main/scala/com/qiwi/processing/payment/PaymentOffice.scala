package com.qiwi.processing.payment

import com.qiwi.processing.account.AccountId
import com.qiwi.processing.common.Amount
import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate._
import pl.newicom.dddd.office.{AggregateContract, OfficeInfo}

trait PaymentOffice

object PaymentOffice {
  implicit val info: OfficeInfo[PaymentOffice] = new OfficeInfo[PaymentOffice] {
    override def name: String = "Payment"
  }

  implicit val contract: AggregateContract.Aux[PaymentOffice, Command, Event, Rejection] = new AggregateContract[PaymentOffice] {
    override type C = Command
    override type R = Rejection
    override type E = Event
  }
}

case class PaymentId(value: String) extends AnyVal

sealed trait Command extends aggregate.Command {
  def paymentId: PaymentId
  override def aggregateId = paymentId.value
}

case class ReceivePayment(paymentId: PaymentId, from: AccountId, to: AccountId, amount: Amount) extends Command
case class AcceptPayment(paymentId: PaymentId) extends Command
case class DeclinePayment(paymentId: PaymentId, reason: String) extends Command


sealed trait Event extends DomainEvent {
  def paymentId: PaymentId
  override def aggregateId: EntityId = paymentId.toString
}
case class PaymentReceived(paymentId: PaymentId, from: AccountId, to: AccountId, amount: Amount) extends Event
case class PaymentAccepted(paymentId: PaymentId) extends Event
case class PaymentDeclined(paymentId: PaymentId) extends Event

sealed trait Status
object Status {
  case object Initial extends Status
  case object Accepted extends Status
  case object Declined extends Status
}

sealed trait Rejection
case class PaymentAlreadyReceived(paymentId: PaymentId, status: Status) extends Rejection
case class PaymentAlreadyDeclined(paymentId: PaymentId) extends Rejection
case class PaymentAlreadyAccepted(paymentId: PaymentId) extends Rejection
