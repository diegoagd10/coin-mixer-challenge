package com.gemini.jobcoin.db

case class Transaction(
  key: String,
  leftTransactions: Int,
  discountAmount: Double
) extends Storable
