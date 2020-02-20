package mypackage

import java.io.{File, PrintWriter}

import net.liftweb.json._
import scalaj.http.Http

import scala.concurrent.ExecutionContext
import scala.io.Source


object Main extends App {
  // Create file cookie.txt and type there your cookie from browser as HTTP-header
  val openCookie = Source.fromFile(s"${System.getProperty("user.dir")}/src/main/resources/cookie.txt")
  val cookie = openCookie.mkString
  openCookie.close

  val headers: Map[String, String] = Map(
    "accept" -> "application/json, text/plain, */*",
    "accept-encoding" -> "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
    "cookie" -> cookie,
    "user-agent" -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36"
  )

  val pw = new PrintWriter(new File(s"${System.getProperty("user.dir")}/src/main/resources/cars.csv"))
  implicit val formats: DefaultFormats.type = DefaultFormats

  def execute(body: => Unit): Thread = new Thread {

    override def run(): Unit = {
      body
    }
  }

  val result = Http("https://www.mos.ru/altmosmvc/api/v1/taxi/getInfo/" +
    "?Region=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0" +
    "&RegNum=&FullName=&LicenseNum=&Condition=&pagenumber=1")
    .headers(headers)
    .asString
    .body

  val count = parse(result).extract[Page].Count / 20 + 1

  val threads: Seq[Thread] = for (page <- 1 to count) yield new Thread {
    override def run(): Unit = {
      val result = Http("https://www.mos.ru/altmosmvc/api/v1/taxi/getInfo/" +
        "?Region=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0" +
        s"&RegNum=&FullName=&LicenseNum=&Condition=&pagenumber=${page}")
        .headers(headers)
        .asString
        .body
      print(s"Parsing page $page\n")
      val cars = parse(result).extract[Page].Infos
      cars.foreach(car => pw.write(car.toString + "\n"))
      this.interrupt()
    }
  }
  threads.foreach {
    thread =>
      thread.start()
      thread.join()
  }
}
