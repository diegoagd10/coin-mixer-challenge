package com.gemini.jobcoin.json

import play.api.libs.json._

case class PlaceholderResponse(
  userId: Int,
  id: Int,
  title: String,
  body: String
)

object PlaceholderResponse {
  implicit val jsonReads: Reads[PlaceholderResponse] = Json.reads[PlaceholderResponse]
}
