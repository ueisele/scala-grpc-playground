package net.uweeisele.actor

import java.lang.Thread.currentThread
import java.util
import java.util.concurrent.TimeUnit.{MILLISECONDS, SECONDS}
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{BlockingQueue, CountDownLatch, LinkedBlockingQueue, TimeUnit}
import java.util.logging.{Level, Logger}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

object Actor {
  def apply[T](initialBehaviour: Behaviour[T])(implicit ec: ExecutionContext): Actor[T] = {
    val worker = new Actor[T](initialBehaviour, new LinkedBlockingQueue[T]())
    ec.execute(worker)
    worker
  }
}

class Actor[T](initialBehaviour: Behaviour[T], queue: BlockingQueue[T]) extends Runnable {

  private[this] val logger = Logger.getLogger(Actor.getClass.getName)

  private[this] val shutdown: AtomicBoolean = new AtomicBoolean(false)
  private[this] val terminationJoin: CountDownLatch = new CountDownLatch(1)

  final val ref: ActorRef[T] = new QueueActorRef[T](queue, shutdown)

  override def run(): Unit = {
    var behaviour = initialBehaviour
    try {
      while (!(shutdown.get() && queue.isEmpty)) {
        var message: Option[T] = None
        try {
          message = Option(queue.poll(1, SECONDS))
        } catch {
          case _: InterruptedException =>
            currentThread.interrupt()
            shutdown.set(true)
        }
        message match {
          case Some(value) =>
            try {
              behaviour = behaviour.apply(value, ref)
            } catch {
              case e: Exception =>
                logger.log(Level.WARNING, s"Exception during processing of message with type ${if (message != null) message.getClass.getName else "null"}", e)
            }
          case _ => ()
        }
      }
    } finally terminationJoin.countDown()
  }

  def shutdown(): Unit = {
    shutdown.set(true)
  }

  def shutdownNow(): List[T] = {
    shutdown.set(true)
    val drainedTasks: util.List[T] = new util.ArrayList[T]
    queue.drainTo(drainedTasks)
    drainedTasks.asScala.toList
  }

  def isShutdown: Boolean = shutdown.get

  @throws[InterruptedException]
  def awaitTermination(): Unit = terminationJoin.await()

  @throws[InterruptedException]
  def awaitTermination(timeout: Timeout): Boolean = awaitTermination(timeout.duration.toMillis, MILLISECONDS)

  @throws[InterruptedException]
  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = terminationJoin.await(timeout, unit)

  def isTerminated: Boolean = terminationJoin.getCount <= 0
}
