package com.redbee.smm

import akka.config.Supervision._
import akka.actor.SupervisorFactory
import akka.actor.Actor._
import akka.camel.CamelServiceManager._

import com.redbee.smm.twitter._

class Boot {
    
  // start the root service actor (and any service actors you want to specify supervision details for)
  val supervisor = SupervisorFactory (
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      List(
        //Supervise(actorOf[RootService], Permanent)
        Supervise(actorOf[TwitterStreamOwnerActor], Permanent)
        , Supervise(actorOf[TwitterServiceActor], Permanent)
        , Supervise(actorOf[TwitterEventScheduler], Permanent)
      )
    )
  )
  
  println("supervisor.newInstance.start")
  supervisor.newInstance.start
  
  actorOf[TwitterServiceActor] ! Restart
  
  startCamelService
  
}