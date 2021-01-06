package net.uweeisele.grpc.counter.server

import io.grpc._
import io.grpc.netty.NettyServerBuilder
import net.uweeisele.grpc.counter.AtomicCounterGrpc
import net.uweeisele.grpc.counter.core.{AtomicCounterBehaviour, AtomicCounterMessage}
import net.uweeisele.worker.Worker

import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import scala.concurrent.ExecutionContext

object AtomicCounterServer {
  private val logger = Logger.getLogger(classOf[AtomicCounterServer].getName)

  def main(args: Array[String]): Unit = {
    val server = new AtomicCounterServer()(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
}

class AtomicCounterServer(implicit executionContext: ExecutionContext) { self =>
  private[this] var worker: Worker[AtomicCounterMessage] = null
  private[this] var server: Server = null

  def start(): Unit = {
    worker = Worker(AtomicCounterBehaviour(0))
    server = NettyServerBuilder
      .forPort(50051)
      .maxConcurrentCallsPerConnection(1)
      .addService(AtomicCounterGrpc.bindService(AtomicCounterImpl(worker.ref), executionContext))
      //.addService(AtomicCounterGrpc.bindService(new AtomicCounterRefImpl(), executionContext))
      .build()
      .start()
    AtomicCounterServer.logger.info("Server started, listening on 50051")
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
    if (worker != null) {
      worker.shutdown()
    }
  }

  private def awaitTermination(timeout: Long, timeUnit: TimeUnit): Unit = {
    if (server != null) {
      server.awaitTermination(timeout, timeUnit)
    }
    if (worker != null) {
      worker.awaitTermination(timeout, timeUnit)
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
    if (worker != null) {
      worker.awaitTermination()
    }
  }

  private def serverInterceptor(): ServerInterceptor = {
    new ServerInterceptor {
      override def interceptCall[ReqT, RespT](call: ServerCall[ReqT, RespT], headers: Metadata, next: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {
        val requestThread = Thread.currentThread().getName
        AtomicCounterServer.logger.info(s"InterceptCall => thread: ${requestThread} method: ${call.getMethodDescriptor.getFullMethodName}")
        val listener = next.startCall(call, headers)
        new ServerCall.Listener[ReqT]() {
          override def onMessage(message: ReqT): Unit = {
            AtomicCounterServer.logger.info(s"InterceptResponse => requestThread ${requestThread} responseThread: ${Thread.currentThread().getName} method: ${call.getMethodDescriptor.getFullMethodName}")
            listener.onMessage(message)
          }

          override def onHalfClose(): Unit = listener.onHalfClose()

          override def onCancel(): Unit = listener.onCancel()

          override def onComplete(): Unit = listener.onComplete()

          override def onReady(): Unit = listener.onReady()
        }
      }
    }
  }
}
