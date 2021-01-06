package net.uweeisele.grpc.counter.client

import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.netty.NettyChannelBuilder
import io.grpc.{CallOptions, Channel, ClientCall, ClientInterceptor, ManagedChannel, ManagedChannelBuilder, Metadata, MethodDescriptor, Status, StatusRuntimeException}
import net.uweeisele.grpc.counter.AtomicCounterGrpc.AtomicCounterBlockingStub
import net.uweeisele.grpc.counter.{AtomicCounterGrpc, IncrementAndGetRequest}

import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}

object AtomicCounterBlockingClient {
  def apply(host: String, port: Int): AtomicCounterBlockingClient = {
    val channel = NettyChannelBuilder
      .forAddress(host, port)
      .usePlaintext()
      .intercept(new ClientInterceptor {
        override def interceptCall[ReqT, RespT](method: MethodDescriptor[ReqT, RespT], callOptions: CallOptions, next: Channel): ClientCall[ReqT, RespT] = {
          println(s"InterceptCall => thread: ${Thread.currentThread().getName} method: ${method.getFullMethodName}")
          new SimpleForwardingClientCall[ReqT, RespT](next.newCall(method, callOptions)) {
            override def start(responseListener: ClientCall.Listener[RespT], headers: Metadata): Unit = {
              super.start(new ClientCall.Listener[RespT]() {
                override def onHeaders(headers: Metadata): Unit = responseListener.onHeaders(headers)

                override def onMessage(message: RespT): Unit = {
                  println(s"InterceptResponse => thread: ${Thread.currentThread().getName} method: ${method.getFullMethodName}")
                  responseListener.onMessage(message)
                }

                override def onClose(status: Status, trailers: Metadata): Unit = responseListener.onClose(status, trailers)

                override def onReady(): Unit = responseListener.onReady()
              }, headers)
            }
          }
        }
      })
      .build()
    val blockingStub = AtomicCounterGrpc.blockingStub(channel)
    new AtomicCounterBlockingClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    val client = AtomicCounterBlockingClient("127.0.0.1", 50051)
    try {
      client.incrementAndGet()
      client.incrementAndGet()
      client.incrementAndGet()
      client.incrementAndGet()
      client.incrementAndGet()
    } finally {
      client.shutdown()
    }
  }
}

class AtomicCounterBlockingClient private(
   private val channel: ManagedChannel,
   private val blockingStub: AtomicCounterBlockingStub
) {
  private[this] val logger = Logger.getLogger(classOf[AtomicCounterBlockingClient].getName)

  def shutdown(): Unit = {
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  /** Say hello to server. */
  def incrementAndGet(): Unit = {
    logger.info("Will try to increment ...")
    val request = IncrementAndGetRequest()
    try {
      val response = blockingStub.incrementAndGet(request)
      logger.info("Current Value: " + response.counterValue)
    }
    catch {
      case e: StatusRuntimeException =>
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
    }
  }
}