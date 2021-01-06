package net.uweeisele.worker

trait WorkerRef[-Req] {

  def tell(message: Req): Unit

  def !(message: Req): Unit = tell(message)
}

object WorkerRef {
  final val noSender: WorkerRef[Any] = (_: Any) => ()
}