package net.uweeisele.grpc.counter.server

import net.uweeisele.grpc.counter.AtomicCounterGrpc.AtomicCounter
import net.uweeisele.grpc.counter._
import net.uweeisele.grpc.counter.core._
import net.uweeisele.worker.AskPattern.Askable
import net.uweeisele.worker.{Timeout, WorkerRef}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

object AtomicCounterImpl {
  def apply(worker: WorkerRef[AtomicCounterMessage])(implicit timeout: Timeout = 10.seconds, ec: ExecutionContext = ExecutionContext.global): AtomicCounterImpl = new AtomicCounterImpl(worker)
}

class AtomicCounterImpl(worker: WorkerRef[AtomicCounterMessage])(implicit timeout: Timeout, ec: ExecutionContext) extends AtomicCounter {

  override def set(request: SetRequest): Future[SetResponse] = worker ? (replyTo => SetCommand(request, replyTo))

  override def compareAndExchange(request: CompareAndExchangeRequest): Future[CompareAndExchangeResponse] = worker ? (replyTo => CompareAndExchangeCommand(request, replyTo))

  override def addAndGet(request: AddAndGetRequest): Future[AddAndGetResponse] = worker ? (replyTo => AddAndGetCommand(request, replyTo))

  override def decrementAndGet(request: DecrementAndGetRequest): Future[DecrementAndGetResponse] = worker ? (replyTo => DecrementAndGetCommand(request, replyTo))

  override def incrementAndGet(request: IncrementAndGetRequest): Future[IncrementAndGetResponse] = worker ? (replyTo => IncrementAndGetCommand(request, replyTo))

  override def get(request: GetRequest): Future[GetResponse] = worker ? (replyTo => GetCommand(request, replyTo))
}
