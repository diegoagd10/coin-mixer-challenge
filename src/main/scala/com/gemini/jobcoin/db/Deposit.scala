package com.gemini.jobcoin.db

case class Deposit(
  key: String,
  addresses: Seq[String]
) extends Storable
