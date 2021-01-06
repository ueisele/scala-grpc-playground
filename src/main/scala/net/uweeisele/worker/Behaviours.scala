package net.uweeisele.worker

object Behaviours {
  implicit def receive[T](onMessage: (T, WorkerRef[T]) => Behaviour[T]): Behaviour[T] = (message, self) => onMessage.apply(message, self)
  implicit def receive[T](onMessage: T => Behaviour[T]): Behaviour[T] = (message, _) => onMessage.apply(message)
}
