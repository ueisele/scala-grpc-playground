package net.uweeisele.grpc.counter.server

import net.uweeisele.grpc.counter.AtomicCounterGrpc.AtomicCounter
import net.uweeisele.grpc.counter._

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future


class AtomicCounterRefImpl() extends AtomicCounter {

  private[this] val counter: AtomicLong = new AtomicLong(0)

  override def set(request: SetRequest): Future[SetResponse] = {
    counter.set(request.counterValue)
    Future.successful(SetResponse())
  }

  override def compareAndExchange(request: CompareAndExchangeRequest): Future[CompareAndExchangeResponse] = {
    val witnessValue = counter.compareAndExchange(request.expectedValue, request.updateValue)
    val success = witnessValue == request.expectedValue
    val currentValue = if (success) request.updateValue else witnessValue
    Future.successful(CompareAndExchangeResponse(success = success, witnessValue = witnessValue, expectedValue = request.expectedValue, currentValue = currentValue))
  }

  override def addAndGet(request: AddAndGetRequest): Future[AddAndGetResponse] = {
    val counterValue = counter.addAndGet(request.delta)
    Future.successful(AddAndGetResponse(counterValue))
  }

  override def decrementAndGet(request: DecrementAndGetRequest): Future[DecrementAndGetResponse] = {
    val counterValue = counter.decrementAndGet()
    Future.successful(DecrementAndGetResponse(counterValue))
  }

  override def incrementAndGet(request: IncrementAndGetRequest): Future[IncrementAndGetResponse] = {
    val counterValue = counter.incrementAndGet()
    Future.successful(IncrementAndGetResponse(counterValue))
  }

  override def get(request: GetRequest): Future[GetResponse] = {
    val counterValue = counter.get()
    Future.successful(GetResponse(counterValue))
  }
}
