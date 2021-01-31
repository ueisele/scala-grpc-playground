package net.uweeisele.actor

import java.util
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue, TimeUnit}
import scala.jdk.CollectionConverters._

trait Receiver[-Req] {
  def onMessage(message: Req): Unit
}

case class Envelope[Req](message: Req, onMessage: Receiver[Req])

object Mailbox {
  def apply(): Mailbox = new Mailbox(new LinkedBlockingQueue[Envelope[_]]())
}

class Mailbox(queue: BlockingQueue[Envelope[_ <: Any]]) {

  def subscribe[Req](receiver: Receiver[Req]): ActorRef[Req] = message => queue.put(Envelope(message, receiver))

  def isEmpty: Boolean = queue.isEmpty

  @throws[InterruptedException]
  def poll(timeout: Timeout): Option[Envelope[_ <: Any]] = Option(queue.poll(timeout.duration.toMillis, TimeUnit.MILLISECONDS))

  def drain(): List[Envelope[_ <: Any]] = {
    val drainedTasks: util.List[Envelope[_ <: Any]] = new util.ArrayList[Envelope[_ <: Any]]
    queue.drainTo(drainedTasks)
    drainedTasks.asScala.toList
  }
}
