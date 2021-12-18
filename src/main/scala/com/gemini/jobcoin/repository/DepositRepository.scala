package com.gemini.jobcoin.repository

import com.gemini.jobcoin.db.Deposit

/**
  * Using HashMap to simulate MongoDB. MongoDB make sense here
  * 'cause we are not storing a lot of columns and we need to retrieve
  * quickly the addresses using the Deposit Address.
  *
  * Also, sharding by key is easier since we can use the DepositAddress to redirect
  * the DB requests to the correct shard.
  */
class DepositRepository extends Repository[Deposit]
  with MemoryRepository[Deposit]
