package net.uweeisele.actor

trait ActorRef[-Req] {

  @throws[InterruptedException]
  def tell(message: Req): Unit

  @throws[InterruptedException]
  def !(message: Req): Unit = tell(message)
}

object ActorRef {
  final val noSender: ActorRef[Any] = (_: Any) => ()
}