package com.gemini.jobcoin.clients

import akka.stream.Materializer
import com.gemini.jobcoin.json.Address
import com.typesafe.config.Config
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class AddressClient(config: Config)(implicit materializer: Materializer) {

  private val wsClient = StandaloneAhcWSClient()
  private val apiAddressesUrl = config.getString("jobcoin.apiAddressesUrl")

  def get(address: String): Future[Address] = async {
    val response = await {
      wsClient
        .url(s"$apiAddressesUrl/$address")
        .get()
    }

    response
      .body[JsValue]
      .validate[Address]
      .get
  }
}
