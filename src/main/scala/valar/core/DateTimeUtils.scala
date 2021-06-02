package valar.core

import java.sql.{Date, Time, Timestamp}
import java.time._
import java.time.format.DateTimeFormatter
import java.util.Calendar

object DateTimeUtils {

  def timestamp(): Timestamp = new Timestamp(Calendar.getInstance().getTime.getTime)

  val gmt: ZoneId = ZoneId.of("GMT")
  val utc: ZoneId = ZoneId.of("UTC")
  val pst: ZoneId = ZoneId.of("America/Los_Angeles")
  val est: ZoneId = ZoneId.of("America/New_York")
  val localZone: ZoneId = ZoneId.systemDefault
  val dbZone: ZoneId = ValarConfig.instance.timezone

  val standardDate: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val monthFirst: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
  val standardTime: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

  def toDbZone(zonedDateTime: ZonedDateTime): ZonedDateTime =
    zonedDateTime.withZoneSameInstant(dbZone)

  def toSqlTimestamp(zonedDateTime: ZonedDateTime): Timestamp =
    new Timestamp(toDbZone(zonedDateTime).toEpochSecond * 1000)

  def fromSqlTimestamp(sqlTimestamp: Timestamp, timeZone: ZoneId): ZonedDateTime =
    Instant.ofEpochMilli(sqlTimestamp.getTime).atZone(timeZone)

  def toUnadjustedSqlDate(date: LocalDate): Date =
    Date.valueOf(date)

  def toUnadjustedSqlTime(time: LocalTime): Time =
    Time.valueOf(time)

  def toLocalZone(sqlTimestamp: Timestamp): ZonedDateTime =
    Instant.ofEpochMilli(sqlTimestamp.getTime).atZone(localZone)

}
