package net.uweeisele.actor

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}
import scala.concurrent.ExecutionContext

object ActorSystem {
  def apply()(implicit ex: ExecutionContext): ActorSystem = new ActorSystem()
}

class ActorSystem(implicit ex: ExecutionContext) {

  private val mailbox: Mailbox = new Mailbox(new LinkedBlockingQueue[Envelope[_]]())
  private val worker: ActorWorker = ActorWorker(mailbox)

  def actor[Req](behaviour: Behaviour[Req]): Actor[Req] = new Actor[Req](behaviour, mailbox)

  def shutdown(): Unit = {
    worker.shutdown()
  }

  def shutdownNow(): Unit = {
    worker.shutdownNow()
  }

  def isShutdown: Boolean = worker.isShutdown

  @throws[InterruptedException]
  def awaitTermination(): Unit = worker.awaitTermination()

  @throws[InterruptedException]
  def awaitTermination(timeout: Timeout): Boolean = worker.awaitTermination(timeout)

  @throws[InterruptedException]
  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = worker.awaitTermination(timeout, unit)

  def isTerminated: Boolean = worker.isTerminated
}
