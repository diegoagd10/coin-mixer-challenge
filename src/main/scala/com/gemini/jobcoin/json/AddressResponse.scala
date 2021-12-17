package com.gemini.jobcoin.json

import play.api.libs.json._

case class AddressResponse(
  balance: BigDecimal,
  transactions: Seq[Transaction]
)

object AddressResponse {
  implicit val jsonReads: Reads[AddressResponse] = Json.reads[AddressResponse]
}

