package net.uweeisele.actor

trait Behaviour[Req] {
  def apply(message: Req, self: ActorRef[Req]): Behaviour[Req]
}
