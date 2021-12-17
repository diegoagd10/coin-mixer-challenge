package com.gemini.jobcoin.json

import java.util.Date

import play.api.libs.json.{Json, Reads}

case class Transaction(
  timestamp: Date,
  toAddress: String,
  amount: BigDecimal)

object Transaction {
  implicit val jsonReads: Reads[Transaction] = Json.reads[Transaction]
}
