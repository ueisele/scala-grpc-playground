package net.uweeisele.actor

import monix.execution.Scheduler

import java.util.concurrent.{TimeUnit, TimeoutException}
import java.util.logging.{Level, Logger}
import scala.concurrent.Promise
import scala.util.{Failure, Success}

object PromiseActorRef {
  def apply[T](promise: Promise[T])(implicit timeout: Timeout, scheduler: Scheduler): PromiseActorRef[T] = new PromiseActorRef[T](promise)
}

class PromiseActorRef[-T](promise: Promise[T])(implicit timeout: Timeout, scheduler: Scheduler) extends ActorRef[T] {

  private[this] val logger = Logger.getLogger(classOf[PromiseActorRef[T]].getName)

  private val timeoutHandler = scheduler.scheduleOnce(timeout.duration.toMillis, TimeUnit.MILLISECONDS, () => {
    promise.tryComplete(Failure(new TimeoutException()))
  })

  override def tell(message: T): Unit = {
    if (promise.tryComplete(Success(message))) {
      timeoutHandler.cancel()
    } else {
      logger.log(Level.FINE, s"The timeout has already been completed! The message ${message.getClass.getSimpleName} is lost!")
    }
  }
}
