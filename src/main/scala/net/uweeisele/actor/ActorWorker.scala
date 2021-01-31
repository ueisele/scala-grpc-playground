package net.uweeisele.actor

import java.lang.Thread.currentThread
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.logging.{Level, Logger}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object ActorWorker {
  def apply(mailbox: Mailbox)(implicit ec: ExecutionContext): ActorWorker = {
    val worker = new ActorWorker(mailbox)
    ec.execute(worker)
    worker
  }
}

class ActorWorker(val mailbox: Mailbox) extends Runnable {

  private[this] val logger = Logger.getLogger(ActorWorker.getClass.getName)

  private[this] val shutdownFlag: AtomicBoolean = new AtomicBoolean(false)
  private[this] val terminationJoin: CountDownLatch = new CountDownLatch(1)

  override def run(): Unit = {
    try {
      while (!(isShutdown && mailbox.isEmpty)) {
        var envelope: Option[Envelope[_ <: Any]] = None
        try {
          envelope = mailbox.poll(1.second)
        } catch {
          case _: InterruptedException =>
            currentThread.interrupt()
            shutdown()
        }
        envelope match {
          case Some(Envelope(message, receiver)) =>
            try {
              receiver.onMessage(message)
            } catch {
              case e: Exception =>
                logger.log(Level.WARNING, s"Exception during processing of message with type ${if (envelope != null) envelope.getClass.getName else "null"}", e)
            }
          case _ => ()
        }
      }
    } finally terminationJoin.countDown()
  }

  def shutdown(): Unit = {
    shutdownFlag.set(true)
  }

  def shutdownNow(): List[Envelope[_ <: Any]] = {
    shutdownFlag.set(true)
    mailbox.drain()
  }

  def isShutdown: Boolean = shutdownFlag.get

  @throws[InterruptedException]
  def awaitTermination(): Unit = terminationJoin.await()

  @throws[InterruptedException]
  def awaitTermination(timeout: Timeout): Boolean = awaitTermination(timeout.duration.toMillis, MILLISECONDS)

  @throws[InterruptedException]
  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = terminationJoin.await(timeout, unit)

  def isTerminated: Boolean = terminationJoin.getCount <= 0
}
