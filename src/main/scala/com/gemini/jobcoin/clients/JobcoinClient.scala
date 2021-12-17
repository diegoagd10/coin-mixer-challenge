package com.gemini.jobcoin.clients

import akka.stream.Materializer
import com.gemini.jobcoin.json.PlaceholderResponse
import com.typesafe.config.Config
import play.api.libs.json._
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.ahc._

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class JobcoinClient(config: Config)(implicit materializer: Materializer) {
  private val wsClient = StandaloneAhcWSClient()
  private val apiAddressesUrl = config.getString("jobcoin.apiAddressesUrl")

  // Docs:
  // https://github.com/playframework/play-ws
  // https://www.playframework.com/documentation/2.6.x/ScalaJsonCombinators
  def testGet(): Future[PlaceholderResponse] = async {
    val response = await {
      wsClient
        .url("https://jsonplaceholder.typicode.com/posts/1")
        .get()
    }

    response
      .body[JsValue]
      .validate[PlaceholderResponse]
      .get
  }
}
