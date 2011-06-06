package com.redbee.smm.twitter

import akka.actor.Actor._
import akka.actor.Actor
import akka.camel.{ Message, Consumer }
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO implementar aqui logica para delays de reinicios lineales o exponenciales
 */
case object DelayedRestart

/**
 * Encargado de ejecutar tareas una vez x segundo
 */
class TwitterEventScheduler extends Actor with Consumer {

  val logger = LoggerFactory.getLogger(getClass);

  logger.info("TwitterEventScheduler initializing")

  // Una vez x minuto
  def endpointUri = "quartz://example?cron=0+*+*+*+*+?"


  def receive = {
    case DelayedRestart => {
      // Debe haber una forma razonable de hacer esto...
      logger.info("Esperando 60 segundos para reinicio")
      Thread sleep 60000l
      logger.info("Reiniciando")
      Actor.registry.actorsFor("com.redbee.smm.twitter.TwitterServiceActor").head ! Restart
    }
    case _ => { logger.info("TwitterEventScheduler"); Actor.registry.actorsFor("com.redbee.smm.twitter.TwitterServiceActor").head ! Update }
  }
}