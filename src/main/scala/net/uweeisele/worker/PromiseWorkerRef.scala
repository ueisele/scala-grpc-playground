package net.uweeisele.worker

import monix.execution.Scheduler

import java.util.concurrent.{TimeUnit, TimeoutException}
import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success}

object PromiseWorkerRef {

  def apply[T](promise: Promise[T])(implicit timeout: Timeout, ec: ExecutionContext): PromiseWorkerRef[T] = {
    val scheduler = Scheduler(ec)
    scheduler.scheduleOnce(timeout.duration.toMillis, TimeUnit.MILLISECONDS, () => {
      promise.tryComplete(Failure(new TimeoutException()))
    })
    new PromiseWorkerRef[T](promise)
  }
}

class PromiseWorkerRef[-T](promise: Promise[T]) extends WorkerRef[T] {
  override def tell(message: T): Unit = promise.tryComplete(Success(message))
}
