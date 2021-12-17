package com.gemini.jobcoin.repository

import com.gemini.jobcoin.db.DepositAddress

class DepositAddressRepository extends Repository[DepositAddress]
  with MemoryRepository[DepositAddress]
