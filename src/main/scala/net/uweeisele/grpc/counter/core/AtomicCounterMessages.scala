package net.uweeisele.grpc.counter.core

import net.uweeisele.grpc.counter._
import net.uweeisele.worker.WorkerRef

sealed trait AtomicCounterMessage {
  def message: AnyRef
}
final case class SetCommand(message: SetRequest, replyTo: WorkerRef[SetResponse] = WorkerRef.noSender) extends AtomicCounterMessage
final case class CompareAndExchangeCommand(message: CompareAndExchangeRequest, replyTo: WorkerRef[CompareAndExchangeResponse] = WorkerRef.noSender) extends AtomicCounterMessage
final case class AddAndGetCommand(message: AddAndGetRequest, replyTo: WorkerRef[AddAndGetResponse] = WorkerRef.noSender) extends AtomicCounterMessage
final case class DecrementAndGetCommand(message: DecrementAndGetRequest, replyTo: WorkerRef[DecrementAndGetResponse] = WorkerRef.noSender) extends AtomicCounterMessage
final case class IncrementAndGetCommand(message: IncrementAndGetRequest, replyTo: WorkerRef[IncrementAndGetResponse] = WorkerRef.noSender) extends AtomicCounterMessage
final case class GetCommand(message: GetRequest, replyTo: WorkerRef[GetResponse] = WorkerRef.noSender) extends AtomicCounterMessage