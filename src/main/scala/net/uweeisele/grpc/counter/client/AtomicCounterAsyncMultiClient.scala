package net.uweeisele.grpc.counter.client

import io.grpc._
import io.grpc.netty.NettyChannelBuilder
import io.netty.channel.ChannelFactory
import net.uweeisele.grpc.counter.{AtomicCounterGrpc, GetRequest, IncrementAndGetRequest}

import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success

object AtomicCounterAsyncMultiClient {

  private[this] val logger = Logger.getLogger(AtomicCounterAsyncClient.getClass.getName)

  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    val expectedClients = 2
    val expectedCallsPerClient = 10
    val currentValue = getCurrentCounterValue()
    println(s"Current Value: ${currentValue}")
    val clients: ListBuffer[Future[Long]] = ListBuffer()
    for(clientNo <- 1 to expectedClients) {
      val future = Future {
        val channel = createChannel(clientNo)
        try {
          val asyncStub = AtomicCounterGrpc.stub(channel)
          val responses: ListBuffer[Future[_]] = ListBuffer()
          val start = System.currentTimeMillis()
          for(callNo <- 1 to expectedCallsPerClient) {
            responses += asyncStub.incrementAndGet(IncrementAndGetRequest())
          }
          Await.result(Future.sequence(responses), 500.seconds)
          System.currentTimeMillis() - start
        } finally {
          channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
        }
      }
      clients += future
    }
    Await.result(Future.sequence(clients), 500.seconds)

    val newValue = getCurrentCounterValue()
    println(s"Before Value: ${currentValue}")
    println(s"New Value: ${newValue}")
    println(s"Expected Value: ${currentValue + expectedClients * expectedCallsPerClient}")
    val totalDuration = clients.map(f => f.value match {
      case Some(Success(duration)) => duration
      case _ => 0
    }).sum
    println(s"Avg Duration: ${totalDuration / expectedClients} ms")
  }

  def createChannel(no: Long): ManagedChannel = {
    NettyChannelBuilder
      .forAddress("127.0.0.1", 50051)
      .usePlaintext()
      .build()
  }

  def getCurrentCounterValue(): Long = {
    val channel = createChannel(-1)
    try {
      val syncStub = AtomicCounterGrpc.blockingStub(channel)
      syncStub.get(GetRequest()).counterValue
    } finally {
      channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
    }
  }
}