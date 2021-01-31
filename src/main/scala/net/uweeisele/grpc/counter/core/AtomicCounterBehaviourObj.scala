package net.uweeisele.grpc.counter.core

import net.uweeisele.actor.{ActorRef, Behaviour}
import net.uweeisele.grpc.counter._

import java.util.logging.Logger

object AtomicCounterBehaviourObj {
  def apply(initialValue: Long): AtomicCounterBehaviourObj = new AtomicCounterBehaviourObj(initialValue)
}

class AtomicCounterBehaviourObj(var value: Long) extends Behaviour[AtomicCounterMessage] {

  private val logger: Logger = Logger.getLogger(AtomicCounterBehaviourFun.getClass.getName)

  override def apply(message: AtomicCounterMessage, self: ActorRef[AtomicCounterMessage]): Behaviour[AtomicCounterMessage] = {
    message match {
      case SetCommand(SetRequest(newValue, _), sender) =>
        value = newValue
        sender ! SetResponse()
      case CompareAndExchangeCommand(CompareAndExchangeRequest(expectedValue, updateValue, _), sender) =>
        val witnessValue = value
        val success = witnessValue == expectedValue
        if (success) value = updateValue
        sender ! CompareAndExchangeResponse(success = success, witnessValue = witnessValue, expectedValue = expectedValue, currentValue = value)
      case AddAndGetCommand(AddAndGetRequest(delta, _), sender) =>
        value += delta
        sender ! AddAndGetResponse(counterValue = value)
      case DecrementAndGetCommand(DecrementAndGetRequest(_), sender) =>
        value -= 1
        sender ! DecrementAndGetResponse(counterValue = value)
      case IncrementAndGetCommand(IncrementAndGetRequest(_), sender) =>
        value += 1
        sender ! IncrementAndGetResponse(counterValue = value)
      case GetCommand(GetRequest(_), sender) =>
        sender ! GetResponse(counterValue = value)
      case unknownMessage =>
        logger.warning(s"Received unknown message with type ${if (unknownMessage != null) message.getClass.getName else "null"}: ${unknownMessage}")
    }
    this
  }
}
