package net.uweeisele.worker

import net.uweeisele.worker.AskPattern.Askable

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object TryAskPattern {

  implicit final class TryAskable[Req](val ref: WorkerRef[Req]) extends AnyVal {

    def tryAsk[Res](message: WorkerRef[Try[Res]] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = {
      ref.ask(message) transform { tryable =>
        tryable match {
          case Success(Success(value)) => Success(value)
          case Success(Failure(exception)) => Failure(exception)
          case Failure(exception) => Failure(exception)
        }
      }
    }

    def ??[Res](message: WorkerRef[Try[Res]] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = tryAsk(message)
  }
}