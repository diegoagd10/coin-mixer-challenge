package com.gemini.jobcoin.json

import play.api.libs.json._

case class Address(
  balance: BigDecimal,
  transactions: Seq[Transaction]
)

object Address {
  implicit val jsonReads: Reads[Address] = Json.reads[Address]
}

