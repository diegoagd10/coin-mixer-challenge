package com.gemini.jobcoin.json

import java.util.Date

import play.api.libs.json.{Json, Reads, Writes}

case class Transaction(
  toAddress: String,
  fromAddress: Option[String],
  amount: BigDecimal,
  timestamp: Option[Date] = None)

object Transaction {
  implicit val jsonReads: Reads[Transaction] = Json.reads[Transaction]
  implicit val jsonWrites: Writes[Transaction] = Json.writes[Transaction]
}
