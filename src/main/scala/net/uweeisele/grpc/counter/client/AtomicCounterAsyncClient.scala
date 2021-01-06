package net.uweeisele.grpc.counter.client

import com.google.common.util.concurrent.MoreExecutors
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.netty.NettyChannelBuilder
import io.grpc.{CallOptions, Channel, ClientCall, ClientInterceptor, ManagedChannel, ManagedChannelBuilder, Metadata, MethodDescriptor, Status}
import net.uweeisele.grpc.counter.AtomicCounterGrpc.AtomicCounterStub
import net.uweeisele.grpc.counter.{AtomicCounterGrpc, CompareAndExchangeRequest, GetRequest, IncrementAndGetRequest}

import java.util.concurrent.{Executor, TimeUnit}
import java.util.logging.{Level, Logger}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object AtomicCounterAsyncClient {

  private[this] val logger = Logger.getLogger(AtomicCounterAsyncClient.getClass.getName)

  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())
    val channel = createChannel()
    try {
      val syncStub = AtomicCounterGrpc.blockingStub(channel)
      val asyncStub = AtomicCounterGrpc.stub(channel)
      val currentValue = syncStub.get(GetRequest()).counterValue
      val responses = for {
        i <- currentValue to currentValue + 100
        response <- List(
          asyncStub.compareAndExchange(CompareAndExchangeRequest(expectedValue = i, updateValue = i+1)) andThen {
            case Success(r) => println(s"Call Completed => thread: ${Thread.currentThread().getName} success: ${r.success} expectedValue: ${r.expectedValue} witnessValue: ${r.witnessValue} currentValue: ${r.currentValue}")
            case Failure(exception) => logger.log(Level.WARNING, exception.getMessage, exception)
          }
        )
      } yield response
      println("Executed all queries")
      Await.result(Future.sequence(responses), 10.seconds)
      println("Completed :)")
    } finally {
      channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
    }
  }

  def sleep(no: Long, sleep: scala.concurrent.duration.Duration): Boolean = {
    if (no%100 == 0)
      Thread sleep sleep.toMillis
    true
  }

  def createChannel(): ManagedChannel = {
    //ManagedChannelBuilder
    NettyChannelBuilder
      .forAddress("127.0.0.1", 50051)
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
  }
}