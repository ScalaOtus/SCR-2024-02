package module4.phoneBook.dao.repositories

import zio.Has
import io.getquill.context.ZioJdbc._
import module4.phoneBook.dao.entities.Address
import module4.phoneBook.db
import zio.{ULayer, ZLayer}

object AddressRepository {
  type AddressRepository = Has[Service]
  
  import db.Ctx._

  trait Service{
      def findBy(id: String): QIO[Option[Address]]
      def insert(phoneRecord: Address): QIO[Unit]
      def update(phoneRecord: Address): QIO[Unit]
      def delete(id: String): QIO[Unit]
  }

  val live: ULayer[AddressRepository] = ???
}
