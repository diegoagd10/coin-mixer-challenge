package com.gemini.jobcoin.repository

import com.gemini.jobcoin.db.Address

/**
  * I can use Deposit repository but using individuals
  * repository to speed up addresses lookups. Besides
  * sharding by address key is easier.
  */
class AddressRepository extends Repository[Address]
  with MemoryRepository[Address]

