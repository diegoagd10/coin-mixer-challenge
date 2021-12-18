package com.gemini.jobcoin.repository

import com.gemini.jobcoin.db.Storable

import scala.concurrent.Future

trait Repository[A <: Storable] {
  def get(): Future[List[A]]
  def getById(key: String): Future[Option[A]]
  def write(data: A): Future[A]
  def delete(key: String): Future[Boolean]
  def update(key: String, entity: A): Future[A]
}
