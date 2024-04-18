package module4.phoneBook.dao.repositories

import io.getquill.context.ZioJdbc._
import module4.phoneBook.dao.entities._
import module4.phoneBook.db
import zio.{Has, ULayer, ZLayer}
import io.getquill.Ord

object PhoneRecordRepository {
  val ctx = db.Ctx
  import ctx._

  type PhoneRecordRepository = Has[Service]

  trait Service{
      def find(phone: String): QIO[Option[PhoneRecord]]
      def list(): QIO[List[PhoneRecord]]
      def insert(phoneRecord: PhoneRecord): QIO[Unit]
      def update(phoneRecord: PhoneRecord): QIO[Unit]
      def delete(id: String): QIO[Unit]
  }

  class ServiceImpl extends Service{

    val phoneRecordSchema = quote{
      querySchema[PhoneRecord](""""PhoneRecord"""")
    }

    val addressSchema = quote{
      querySchema[Address](""""Address"""")
    }

    // SELECT x1."id", x1."phone", x1."fio", x1."addressId" FROM "PhoneRecord" x1 WHERE x1."phone" = ?
    override def find(phone: String): QIO[Option[PhoneRecord]] = 
      ctx.run(phoneRecordSchema.filter(_.phone == lift(phone)).take(1).drop(10)).map(_.headOption)

    // SELECT x."id", x."phone", x."fio", x."addressId" FROM "PhoneRecord" x
    override def list(): QIO[List[PhoneRecord]] = ctx.run(phoneRecordSchema)

    // INSERT INTO "PhoneRecord" ("id","phone","fio","addressId") VALUES (?, ?, ?, ?)b
    override def insert(phoneRecord: PhoneRecord): QIO[Unit] = 
      ctx.run( phoneRecordSchema.insert(lift(phoneRecord))).unit

      def insert(phoneRecords: List[PhoneRecord]): QIO[Unit] = 
      ctx.run(liftQuery(phoneRecords).foreach(r => phoneRecordSchema.insert(lift(r)))).unit
      
      // UPDATE "PhoneRecord" SET "id" = ?, "phone" = ?, "fio" = ?, "addressId" = ? WHERE "id" = ?
    override def update(phoneRecord: PhoneRecord): QIO[Unit] = 
      ctx.run(phoneRecordSchema.filter(_.id == lift(phoneRecord.id)).update(lift(phoneRecord))).unit

    override def delete(id: String): QIO[Unit] = 
      ctx.run(phoneRecordSchema.filter(_.id == lift(id)).delete).unit


      // SELECT phoneRecord."id", phoneRecord."phone", phoneRecord."fio", phoneRecord."addressId", address."id", address."zipCode", address."streetAddress" 
      // FROM "PhoneRecord" phoneRecord, "Address" address WHERE address."id" = phoneRecord."addressId"
      ctx.run(
        for{
          phoneRecord <- phoneRecordSchema
          address <- addressSchema if (address.id == phoneRecord.addressId)
        } yield(phoneRecord, address)
      )

      // applicative join

      ctx.run(
        phoneRecordSchema
        .join(addressSchema).on(_.addressId == _.id)
        .join(phoneRecordSchema).on({case (((ph1, ad1), ad2)) => ph1.addressId == ad2.id})
      )

      // flat join

      ctx.run(
        for{
          phoneRecord <- phoneRecordSchema
          address <- addressSchema.join(_.id == phoneRecord.addressId)
          address2 <- addressSchema.join(_.id == phoneRecord.addressId)
          address3 <- addressSchema.join(_.id == phoneRecord.addressId)
        } yield (phoneRecord, address)
      )
  }



  
 
  val live: ULayer[PhoneRecordRepository] = ???
}
