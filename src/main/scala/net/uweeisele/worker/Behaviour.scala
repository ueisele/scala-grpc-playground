package net.uweeisele.worker

trait Behaviour[Req] {
  def apply(message: Req, self: WorkerRef[Req]): Behaviour[Req]
  def sameBehaviour: Behaviour[Req] = this
}
