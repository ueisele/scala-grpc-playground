package net.uweeisele.worker

import scala.concurrent.{ExecutionContext, Future, Promise}

object AskPattern {

  implicit final class Askable[Req](val ref: WorkerRef[Req]) extends AnyVal {

    def ask[Res](message: WorkerRef[Res] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = {
      val promise = Promise[Res]()
      val replyTo = PromiseWorkerRef[Res](promise)
      ref.tell(message.apply(replyTo))
      promise.future
    }

    def ?[Res](message: WorkerRef[Res] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = ask(message)
  }
}