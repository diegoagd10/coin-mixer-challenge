package com.gemini.jobcoin.repository

import com.gemini.jobcoin.db.Storable
import javax.management.openmbean.KeyAlreadyExistsException

import scala.async.Async._
import scala.collection.mutable.{Map => MMap}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

trait MemoryRepository[A <: Storable] {
  this: Repository[A] =>

  /**
    * Using HashMap to simulate MongoDB. MongoDB make sense here
    * 'cause we are not storing a lot of columns and we need to retrieve
    * quickly the addresses using the Deposit Address.
    *
    * Also, sharding by key is easier since we can use the DepositAddress to redirect
    * the DB requests to the correct shard.
    */
  final val memoryDB: MMap[String, A] = MMap.empty

  override def getById(key: String): Future[Option[A]] = async {
    memoryDB.get(key)
  }

  override def write(data: A): Future[A] = async {
    val possibleEntity = await(getById(data.key))
    possibleEntity match {
      case None =>
        memoryDB += (data.key -> data)
        data
      case Some(_) =>
        throw new KeyAlreadyExistsException(s"The key ${data.key} is already in use.")
    }
  }
}
