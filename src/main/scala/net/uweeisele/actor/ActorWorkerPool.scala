package net.uweeisele.actor

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object ActorWorkerPool {
  def apply(numWorkers: Int = 1)(implicit ec: ExecutionContext): ActorWorkerPool = new ActorWorkerPool(numWorkers)
}

class ActorWorkerPool(val numWorkers: Int)(implicit ec: ExecutionContext) {

  private[this] val workers: List[ActorWorker] = (1 to numWorkers).map(_ => ActorWorker(Mailbox())).toList

  private[this] val nextWorkerIndex: AtomicInteger = new AtomicInteger(0)

  def apply(index: Int): ActorWorker = workers(index)

  def next(): ActorWorker = workers(getAndIncrementNextWorkerIndex())

  def shutdown(): Unit =  workers.foreach(worker => worker.shutdown())

  def shutdownNow(): Unit = workers.foreach(worker => worker.shutdownNow())

  def isShutdown: Boolean = workers.exists(worker => worker.isShutdown)

  @throws[InterruptedException]
  def awaitTermination(): Unit = workers.foreach(worker => worker.awaitTermination())

  @throws[InterruptedException]
  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = awaitTermination(Timeout(timeout, unit))

  @throws[InterruptedException]
  def awaitTermination(timeout: Timeout): Boolean =
    Await.result(Future.sequence(workers.map(worker => Future(worker.awaitTermination(timeout)))), Duration.Inf).forall(success => success)

  def isTerminated: Boolean = workers.forall(worker => worker.isTerminated)

  private def getAndIncrementNextWorkerIndex(): Integer = {
    nextWorkerIndex.getAndAccumulate(1, (prev, updateValue) => (prev + updateValue % numWorkers).toInt)
  }
}
