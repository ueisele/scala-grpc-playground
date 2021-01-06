package net.uweeisele.actor

import java.util
import java.util.concurrent.{BlockingQueue, TimeUnit}
import scala.jdk.CollectionConverters._

trait Receiver[-Req] {
  private[actor] def onMessage(message: Req): Unit
}

case class Envelope[Req](message: Req, onMessage: Receiver[Req])

class Mailbox(queue: BlockingQueue[Envelope[_ <: Any]]) {
  def register[Req](onMessage: Receiver[Req]): ActorRef[Req] = message => if (message != null) queue.put(Envelope(message, onMessage))

  def isEmpty: Boolean = queue.isEmpty

  @throws[InterruptedException]
  def poll(timeout: Timeout): Option[Envelope[_ <: Any]] = Option(queue.poll(timeout.duration.toMillis, TimeUnit.MILLISECONDS))

  def drain(): List[Envelope[_ <: Any]] = {
    val drainedTasks: util.List[Envelope[_ <: Any]] = new util.ArrayList[Envelope[_ <: Any]]
    queue.drainTo(drainedTasks)
    drainedTasks.asScala.toList
  }
}
