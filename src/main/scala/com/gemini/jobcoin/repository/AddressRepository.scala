package com.gemini.jobcoin.repository

import com.gemini.jobcoin.db.Address

class AddressRepository extends Repository[Address]
  with MemoryRepository[Address]

