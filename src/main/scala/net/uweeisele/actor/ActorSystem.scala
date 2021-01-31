package net.uweeisele.actor

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext

object ActorSystem {
  def apply(numWorkers: Int = 1)(implicit ex: ExecutionContext): ActorSystem = new ActorSystem(numWorkers)
}

class ActorSystem(numWorkers: Int)(implicit ex: ExecutionContext) {

  private val workers: ActorWorkerPool = ActorWorkerPool(numWorkers)

  def actor[Req](behaviour: Behaviour[Req]): ActorRef[Req] = new Actor[Req](behaviour, workers.next().mailbox).ref

  def shutdown(): Unit = {
    workers.shutdown()
  }

  def shutdownNow(): Unit = {
    workers.shutdownNow()
  }

  def isShutdown: Boolean = workers.isShutdown

  @throws[InterruptedException]
  def awaitTermination(): Unit = workers.awaitTermination()

  @throws[InterruptedException]
  def awaitTermination(timeout: Timeout): Boolean = workers.awaitTermination(timeout)

  @throws[InterruptedException]
  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = workers.awaitTermination(timeout, unit)

  def isTerminated: Boolean = workers.isTerminated
}
