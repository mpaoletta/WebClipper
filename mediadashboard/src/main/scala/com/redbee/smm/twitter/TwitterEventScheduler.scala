package com.redbee.smm.twitter

import akka.actor.Actor._
import akka.actor.Actor
import akka.camel.{ Message, Consumer }
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO implementar aqui logica para delays de reinicios lineales o exponenciales
 */
case class DelayedRestart(seconds: Long)

/**
 * Encargado de ejecutar tareas una vez x segundo
 */
class TwitterEventScheduler extends Actor with Consumer {

  val logger = LoggerFactory.getLogger(getClass);

  logger.info("TwitterEventScheduler initializing")

  // Una vez x minuto
  def endpointUri = "quartz://example?cron=0+*+*+*+*+?"

  def receive = {
    case DelayedRestart(t) => {
      // Debe haber una forma razonable de hacer esto...
      logger.info("Esperando " + (t/1000) + " segundos para reinicio")
      Thread sleep t
      logger.info("Reiniciando")
      Actor.registry.actorsFor("com.redbee.smm.twitter.TwitterServiceActor").head ! Restart
    }
    case _ => { logger.info("TwitterEventScheduler"); Actor.registry.actorsFor("com.redbee.smm.twitter.TwitterServiceActor").head ! Update }
  }
}