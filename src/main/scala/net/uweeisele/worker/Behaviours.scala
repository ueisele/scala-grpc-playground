package net.uweeisele.worker

object Behaviours {

  def receive[T](onMessage: Behaviour[T]): Behaviour[T] = onMessage
}
