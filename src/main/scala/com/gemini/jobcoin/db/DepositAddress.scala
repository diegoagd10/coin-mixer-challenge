package com.gemini.jobcoin.db

case class DepositAddress(
  key: String,
  addresses: Seq[String]
) extends Storable
