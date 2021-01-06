package net.uweeisele.actor

import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class QueueActorRef[-T](queue: BlockingQueue[T], shutdown: AtomicBoolean) extends ActorRef[T] {
  override def tell(message: T): Unit = if (!shutdown.get() && message != null) queue.put(message)
}
