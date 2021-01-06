package net.uweeisele.actor

import java.util.logging.{Level, Logger}

object Actor {
  def apply[Req](behaviour: Behaviour[Req])(implicit actorSystem: ActorSystem): Actor[Req] = actorSystem.actor(behaviour)
}

class Actor[-Req](initialBehaviour: Behaviour[Req], mailbox: Mailbox) extends Receiver[Req] {

  private[this] val logger = Logger.getLogger(classOf[Actor[Req]].getName)

  private[this] var behaviour: Behaviour[Req] = initialBehaviour

  final val ref: ActorRef[Req] = mailbox.register(this)

  private[actor] def onMessage(message: Req): Unit = {
    try {
      behaviour = behaviour.apply(message, ref)
    } catch {
      case e: Exception =>
        logger.log(Level.WARNING, s"Exception during processing of message with type ${if (message != null) message.getClass.getName else "null"}", e)
    }
  }
}
