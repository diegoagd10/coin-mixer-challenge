package com.gemini.jobcoin.repository

import com.gemini.jobcoin.db.Transaction

/**
  * Using HashMap to simulate MongoDB. MongoDB make sense here
  * 'cause relationships are not that necessary here.
  *
  * Also, sharding by key is easier since we can use the DepositAddress to redirect
  * the DB requests to the correct shard.
  */
class TransactionRepository extends Repository[Transaction]
  with MemoryRepository[Transaction]
