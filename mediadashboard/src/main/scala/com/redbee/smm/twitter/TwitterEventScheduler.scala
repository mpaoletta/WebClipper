package com.redbee.smm.twitter

import akka.actor.Actor._
import akka.actor.Actor
import akka.camel.{ Message, Consumer }

/**
 * Encargado de ejecutar tareas una vez x segundo
 */
class TwitterEventScheduler extends Actor with Consumer {
  
  println("TwitterEventScheduler initializing")

  // Una vez x minuto
  def endpointUri = "quartz://example?cron=0+*+*+*+*+?"


  def receive = {
    case msg => { println("TwitterEventScheduler"); Actor.registry.actorsFor("com.redbee.smm.twitter.TwitterServiceActor").head ! Update }
  }
}