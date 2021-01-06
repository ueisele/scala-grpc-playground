package net.uweeisele.worker

import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class QueueWorkerRef[-T](queue: BlockingQueue[T], shutdown: AtomicBoolean) extends WorkerRef[T] {
  override def tell(message: T): Unit = if (!shutdown.get() && message != null) queue.put(message)
}
