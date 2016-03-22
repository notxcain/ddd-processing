package com.qiwi.processing

import akka.actor.ActorSystem
import com.qiwi.processing.account.{Account, AccountOffice}
import pl.newicom.dddd.cluster.{DefaultShardIdResolver, GlobalOffice, ShardIdResolver}
import pl.newicom.dddd.office.Office

object App extends App with GlobalOffice {
  implicit val actorSystem: ActorSystem = ActorSystem("processing")
  implicit def shardIdResolution[A]: ShardIdResolver[A] = new DefaultShardIdResolver[A](1)
  val accounts = Office.openOffice[AccountOffice](new Account)
}
