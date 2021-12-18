package com.gemini.jobcoin.repository

import com.gemini.jobcoin.db.Storable
import javax.management.openmbean.KeyAlreadyExistsException

import scala.async.Async._
import scala.collection.mutable.{Map => MMap}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case object KeyNotFoundException extends RuntimeException("Key not found.")

trait MemoryRepository[A <: Storable] {
  this: Repository[A] =>

  /**
    * Using HashMap to simulate MongoDB.
    */
  final private val memoryDB: MMap[String, A] = MMap.empty

  override def get(): Future[List[A]] =
    Future.successful(memoryDB.values.toList)

  override def getById(key: String): Future[Option[A]] = Future.successful {
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

  override def delete(key: String): Future[Boolean] = Future.successful {
    memoryDB.remove(key).isDefined
  }

  override def update(key: String, entity: A): Future[A] = async {
    if (!await(delete(key))) {
      throw KeyNotFoundException
    }
    await(write(entity))
  }
}
