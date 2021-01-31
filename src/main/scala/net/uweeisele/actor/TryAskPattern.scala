package net.uweeisele.actor

import monix.execution.Scheduler
import net.uweeisele.actor.AskPattern.Askable

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object TryAskPattern {

  implicit final class TryAskable[Req](val ref: ActorRef[Req]) extends AnyVal {

    @throws[InterruptedException]
    def tryAsk[Res](message: ActorRef[Try[Res]] => Req)(implicit timeout: Timeout, scheduler: Scheduler): Future[Res] = {
      ref.ask(message) transform { tryable =>
        tryable match {
          case Success(Success(value)) => Success(value)
          case Success(Failure(exception)) => Failure(exception)
          case Failure(exception) => Failure(exception)
        }
      }
    }

    @throws[InterruptedException]
    def ??[Res](message: ActorRef[Try[Res]] => Req)(implicit timeout: Timeout, scheduler: Scheduler): Future[Res] = tryAsk(message)
  }
}