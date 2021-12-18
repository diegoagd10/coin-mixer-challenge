package com.gemini.jobcoin.clients

import akka.stream.Materializer
import com.gemini.jobcoin.json.Transaction
import com.typesafe.config.Config
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.JsonBodyWritables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class TransactionClient(config: Config)(implicit materializer: Materializer) {

  private val wsClient = StandaloneAhcWSClient()
  private val apiTransactionsUrl = config.getString("jobcoin.apiTransactionsUrl")

  def get(): Future[Transaction] = async {
    val response = await {
      wsClient
        .url(apiTransactionsUrl)
        .get()
    }

    response
      .body[JsValue]
      .validate[Transaction]
      .get
  }

  def post(transaction: Transaction): Future[Map[String, String]] = async {
    val response = await {
      wsClient
        .url(apiTransactionsUrl)
        .post(Json.toJson(transaction))
    }

    response
      .body[JsValue]
      .validate[Map[String, String]]
      .get
  }
}