package net.uweeisele.actor

import net.uweeisele.actor.AskPattern.Askable

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object TryAskPattern {

  implicit final class TryAskable[Req](val ref: ActorRef[Req]) extends AnyVal {

    def tryAsk[Res](message: ActorRef[Try[Res]] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = {
      ref.ask(message) transform { tryable =>
        tryable match {
          case Success(Success(value)) => Success(value)
          case Success(Failure(exception)) => Failure(exception)
          case Failure(exception) => Failure(exception)
        }
      }
    }

    def ??[Res](message: ActorRef[Try[Res]] => Req)(implicit timeout: Timeout, ec: ExecutionContext): Future[Res] = tryAsk(message)
  }
}