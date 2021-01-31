package net.uweeisele.grpc.counter.server

import monix.execution.Scheduler
import net.uweeisele.actor.AskPattern.Askable
import net.uweeisele.actor.{ActorRef, Timeout}
import net.uweeisele.grpc.counter.AtomicCounterGrpc.AtomicCounter
import net.uweeisele.grpc.counter._
import net.uweeisele.grpc.counter.core._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object AtomicCounterImpl {
  def apply(actorRef: ActorRef[AtomicCounterMessage])(implicit timeout: Timeout = 10.seconds, scheduler: Scheduler): AtomicCounterImpl = new AtomicCounterImpl(actorRef)
}

class AtomicCounterImpl(actorRef: ActorRef[AtomicCounterMessage])(implicit timeout: Timeout, scheduler: Scheduler) extends AtomicCounter {

  override def set(request: SetRequest): Future[SetResponse] = actorRef ? (replyTo => SetCommand(request, replyTo))

  override def compareAndExchange(request: CompareAndExchangeRequest): Future[CompareAndExchangeResponse] = actorRef ? (replyTo => CompareAndExchangeCommand(request, replyTo))

  override def addAndGet(request: AddAndGetRequest): Future[AddAndGetResponse] = actorRef ? (replyTo => AddAndGetCommand(request, replyTo))

  override def decrementAndGet(request: DecrementAndGetRequest): Future[DecrementAndGetResponse] = actorRef ? (replyTo => DecrementAndGetCommand(request, replyTo))

  override def incrementAndGet(request: IncrementAndGetRequest): Future[IncrementAndGetResponse] = actorRef ? (replyTo => IncrementAndGetCommand(request, replyTo))

  override def get(request: GetRequest): Future[GetResponse] = actorRef ? (replyTo => GetCommand(request, replyTo))
}
