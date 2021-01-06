package net.uweeisele.actor

import scala.concurrent.{ExecutionContext, Future, Promise}

object AskPattern {

  implicit final class Askable[Req](val ref: ActorRef[Req]) extends AnyVal {

    def ask[Res](message: ActorRef[Res] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = {
      val promise = Promise[Res]()
      val replyTo = PromiseActorRef[Res](promise)
      ref.tell(message.apply(replyTo))
      promise.future
    }

    def ?[Res](message: ActorRef[Res] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = ask(message)
  }
}