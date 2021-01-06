package net.uweeisele.actor

object Behaviours {
  implicit def receive[T](onMessage: (T, ActorRef[T]) => Behaviour[T]): Behaviour[T] = (message, self) => onMessage.apply(message, self)
  implicit def receive[T](onMessage: T => Behaviour[T]): Behaviour[T] = (message, _) => onMessage.apply(message)
}
