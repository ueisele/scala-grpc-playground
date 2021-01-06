package net.uweeisele.actor

trait ActorRef[-Req] {

  def tell(message: Req): Unit

  def !(message: Req): Unit = tell(message)
}

object ActorRef {
  final val noSender: ActorRef[Any] = (_: Any) => ()
}