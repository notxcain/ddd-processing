package com.qiwi.processing.payment

import com.qiwi.processing.account._
import com.qiwi.processing.common.Amount
import org.slf4j.LoggerFactory
import pl.newicom.dddd.office.OfficePath
import pl.newicom.dddd.process._
import shapeless.{:+:, CNil, HNil}

//case class PaymentProcess(accounts: OfficePath[AccountOffice], payments: OfficePath[PaymentOffice]) extends SagaConfig {
//  override def name: String = "PaymentProcess"
//
//  val log = LoggerFactory.getLogger(name)
//
//  type Input = PaymentReceived :+: HoldPlaced :+: AccountCredited :+: AccountDebited :+: CNil
//
//  object resolveId extends ResolveId {
//    implicit val _ = cases {
//      at[PaymentReceived](_.paymentId.value) ::
//        at[HoldPlaced](_.paymentId.value) ::
//        at[AccountCredited](_.paymentId.value) ::
//        at[AccountDebited](_.paymentId.value) ::
//        HNil
//    }
//  }
//
//
//  case class State(paymentId: PaymentId, from: AccountId, to: AccountId, amount: Amount)
//
//  object applyEvent extends ApplyEvent[State] {
//    implicit val _ = cases {
//      at[PaymentReceived] {
//        case PaymentReceived(paymentId, from, to, amount) => {
//          case None =>
//            changeState(State(paymentId, from, to, amount)) and
//              (accounts !! PlaceHold(from, paymentId, amount))
//          case Some(s) => ignore
//        }
//      } ::
//        at[HoldPlaced] { case e @ HoldPlaced(accountId, paymentId, amount) => withState { state =>
//          accounts !! CreditAccount(state.to, paymentId, amount)
//        }} ::
//        at[AccountCredited] { e => withState { s =>
//          accounts !! DebitAccount(s.from, s.paymentId)
//        }} ::
//        at[AccountDebited] { e => withState { s =>
//          payments !! AcceptPayment(s.paymentId)
//        }} ::
//        HNil
//    }
//  }
//
//  object receiveEvent extends ReceiveEvent[State] {
//    implicit val _ = cases {
//      at[PaymentReceived](_ => accept) ::
//        at[TransferCreated](_ => accept) ::
//        at[NotificationSent] { case (state, _) =>
//          state.map(_.notificationStatus).map {
//            case Initial => reject
//            case Waiting => accept
//            case Done => ignore
//          }.getOrElse(reject)
//        } ::
//        at[NotificationFailed] { case (state, _) =>
//          state.map(_.notificationStatus).map {
//            case Initial => reject
//            case Waiting => accept
//            case Done => ignore
//          }.getOrElse(reject)
//        } ::
//        at[NotificationFailureReceived] (_ => accept) ::
//        at[TransferExpired](_ => accept) ::
//        HNil
//    }
//  }
//}
