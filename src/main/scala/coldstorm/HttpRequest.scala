package coldstorm

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.nio.channels.SocketChannel
import scala.collection.mutable
import scala.util.control.Breaks.break

case class HttpHeader(method: String, url: String, protocolVer: String)

class HttpRequest(connect: SocketChannel) {
  val inputStream: InputStream = connect.socket().getInputStream
  val inputStreamReader: InputStreamReader = new InputStreamReader(inputStream, HttpServer.config("defaultCharEncoding").asInstanceOf[String])
  val bufferedReader: BufferedReader = new BufferedReader(inputStreamReader)
  def parseHeader(): HttpHeader = {
    val httpHeader: String = bufferedReader.readLine()
    val httpHeaderStrArray: Array[String] = httpHeader.split(' ')
    var httpMethod: Option[String] = None
    try {
      httpMethod = Some(httpHeaderStrArray(0).toUpperCase)
    } catch {
      case ex: ArrayIndexOutOfBoundsException =>
        throw new HttpHeaderFormatNotCorrectException
    }
    if (!List("GET", "POST", "PUT", "DELETE", "TRACE", "CONNECT", "OPTIONS", "HEAD").contains(httpMethod.get)) {
      throw new HttpHeaderFormatNotCorrectException
    }
    HttpHeader(httpMethod.get, httpHeaderStrArray(1), httpHeaderStrArray(2))
  }
  def parseAttrs(): Map[String, String] = {
    var line: String = ""
    val listResult: mutable.HashMap[String, String] = new mutable.HashMap[String, String]()
    do {
      line = bufferedReader.readLine()
      if (line == "" || line == null) {
        break
      }
      val keyValuePair: Array[String] = line.split(':').map(_.trim)
      listResult.put(keyValuePair(0).toLowerCase(), keyValuePair(1))
    } while (true)
    listResult.toMap
  }
  def readBody(): Array[Byte] = {
    if (!attributes.contains("content-length")) {
      return inputStream.readAllBytes()
    }
    val contentLen: Int = attributes.get("content-length").asInstanceOf[String].toInt
    if (contentLen == -1) {
      return inputStream.readAllBytes()
    }
    val buffer: Array[Byte] = new Array[Byte](contentLen)
    inputStream.read(buffer, 0, contentLen)
    buffer
  }
  val header: HttpHeader = parseHeader()
  val attributes: Map[String, String] = parseAttrs()
  val body: Array[Byte] = readBody()
}
