package net.uweeisele.actor

import monix.execution.Scheduler

import java.util.concurrent.{TimeUnit, TimeoutException}
import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success}

object PromiseActorRef {

  def apply[T](promise: Promise[T])(implicit timeout: Timeout, ec: ExecutionContext): PromiseActorRef[T] = {
    val scheduler = Scheduler(ec)
    scheduler.scheduleOnce(timeout.duration.toMillis, TimeUnit.MILLISECONDS, () => {
      promise.tryComplete(Failure(new TimeoutException()))
    })
    new PromiseActorRef[T](promise)
  }
}

class PromiseActorRef[-T](promise: Promise[T]) extends ActorRef[T] {
  override def tell(message: T): Unit = promise.tryComplete(Success(message))
}
