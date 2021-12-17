package com.gemini.jobcoin.repository

import com.gemini.jobcoin.db.Storable

import scala.concurrent.Future

trait Repository[A <: Storable] {
  def getById(key: String): Future[Option[A]]
  def write(data: A): Future[A]
}
