package net.uweeisele.grpc.counter.core

import net.uweeisele.grpc.counter._
import net.uweeisele.worker.{Behaviour, Behaviours}

import java.util.logging.Logger

object AtomicCounterBehaviour {

  private val logger: Logger = Logger.getLogger(AtomicCounterBehaviour.getClass.getName)

  def apply(value: Long): Behaviour[AtomicCounterMessage] = {
    Behaviours.receive[AtomicCounterMessage] { (message, self) =>
      message match {
        case SetCommand(SetRequest(counterValue, _), sender) =>
          sender ! SetResponse()
          apply(counterValue)
        case CompareAndExchangeCommand(CompareAndExchangeRequest(`value`, updateValue, _), sender) =>
          sender ! CompareAndExchangeResponse(success = true, witnessValue = value, expectedValue = value, currentValue = updateValue)
          apply(updateValue)
        case CompareAndExchangeCommand(CompareAndExchangeRequest(expectedValue, updateValue, _), sender) =>
          sender ! CompareAndExchangeResponse(success = false, witnessValue = value, expectedValue = expectedValue, currentValue = value)
          apply(value)
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
  }

}
