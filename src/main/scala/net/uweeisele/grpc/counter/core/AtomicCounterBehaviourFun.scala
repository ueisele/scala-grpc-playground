package net.uweeisele.grpc.counter.core

import net.uweeisele.actor.Behaviour
import net.uweeisele.grpc.counter._

import java.util.logging.Logger

object AtomicCounterBehaviourFun {

  private val logger: Logger = Logger.getLogger(AtomicCounterBehaviourFun.getClass.getName)

  def apply(value: Long): Behaviour[AtomicCounterMessage] = (message, self) =>
    message match {
      case SetCommand(SetRequest(newValue, _), sender) =>
        sender ! SetResponse()
        apply(newValue)
      case CompareAndExchangeCommand(CompareAndExchangeRequest(expectedValue, updateValue, _), sender) =>
        val witnessValue = value
        val success = witnessValue == expectedValue
        val newValue = if (success) updateValue else value
        sender ! CompareAndExchangeResponse(success = success, witnessValue = witnessValue, expectedValue = expectedValue, currentValue = newValue)
        apply(newValue)
      case AddAndGetCommand(AddAndGetRequest(delta, _), sender) =>
        val newValue = value + delta
        sender ! AddAndGetResponse(counterValue = newValue)
        apply(newValue)
      case DecrementAndGetCommand(DecrementAndGetRequest(_), sender) =>
        val newValue = value - 1
        sender ! DecrementAndGetResponse(counterValue = newValue)
        apply(newValue)
      case IncrementAndGetCommand(IncrementAndGetRequest(_), sender) =>
        val newValue = value + 1
        sender ! IncrementAndGetResponse(counterValue = newValue)
        apply(newValue)
      case GetCommand(GetRequest(_), sender) =>
        sender ! GetResponse(counterValue = value)
        apply(value)
      case unknownMessage =>
        logger.warning(s"Received unknown message with type ${if (unknownMessage != null) message.getClass.getName else "null"}: ${unknownMessage}")
        apply(value)
    }
}
