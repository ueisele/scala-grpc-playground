package net.uweeisele.worker

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

object AskPattern {

  implicit final class Askable[Req](val ref: WorkerRef[Req]) extends AnyVal {

    def ask[Res](message: WorkerRef[Res] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = {
      val promise = Promise[Res]()
      val replyTo = PromiseWorkerRef[Res](promise)
      ref.tell(message.apply(replyTo))
      promise.future
    }

    def ?[Res](message: WorkerRef[Res] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = ask(message)

    def askTry[Res](message: WorkerRef[Try[Res]] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = {
      ask(message) transform { tryable =>
        tryable match {
          case Success(Success(value)) => Success(value)
          case Success(Failure(exception)) => Failure(exception)
          case Failure(exception) => Failure(exception)
        }
      }
    }

    def ??[Res](message: WorkerRef[Try[Res]] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = askTry(message)
  }
}