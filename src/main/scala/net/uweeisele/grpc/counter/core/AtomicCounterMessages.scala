package net.uweeisele.grpc.counter.core

import net.uweeisele.grpc.counter._
import net.uweeisele.actor.ActorRef

sealed trait AtomicCounterMessage {
  def message: AnyRef
}
final case class SetCommand(message: SetRequest, replyTo: ActorRef[SetResponse] = ActorRef.noSender) extends AtomicCounterMessage
final case class CompareAndExchangeCommand(message: CompareAndExchangeRequest, replyTo: ActorRef[CompareAndExchangeResponse] = ActorRef.noSender) extends AtomicCounterMessage
final case class AddAndGetCommand(message: AddAndGetRequest, replyTo: ActorRef[AddAndGetResponse] = ActorRef.noSender) extends AtomicCounterMessage
final case class DecrementAndGetCommand(message: DecrementAndGetRequest, replyTo: ActorRef[DecrementAndGetResponse] = ActorRef.noSender) extends AtomicCounterMessage
final case class IncrementAndGetCommand(message: IncrementAndGetRequest, replyTo: ActorRef[IncrementAndGetResponse] = ActorRef.noSender) extends AtomicCounterMessage
final case class GetCommand(message: GetRequest, replyTo: ActorRef[GetResponse] = ActorRef.noSender) extends AtomicCounterMessage