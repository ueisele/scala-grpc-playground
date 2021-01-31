package net.uweeisele.actor

import monix.execution.Scheduler

import scala.concurrent.{Future, Promise}

object AskPattern {

  implicit final class Askable[Req](val ref: ActorRef[Req]) extends AnyVal {

    @throws[InterruptedException]
    def ask[Res](message: ActorRef[Res] => Req)(implicit timeout: Timeout, scheduler: Scheduler): Future[Res] = {
      val promise = Promise[Res]()
      val replyTo = PromiseActorRef[Res](promise)
      ref.tell(message.apply(replyTo))
      promise.future
    }

    @throws[InterruptedException]
    def ?[Res](message: ActorRef[Res] => Req)(implicit timeout: Timeout, scheduler: Scheduler): Future[Res] = ask(message)
  }
}