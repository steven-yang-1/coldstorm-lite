package coldstorm

import java.net.InetSocketAddress
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}
import java.util
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HttpServer {
  val config: Map[String, Any] = Map(
    "maxWorkers" -> 10,
    "defaultCharEncoding" -> "UTF-8"
  )
  var currentWorkers: Int = 0
  def main(args: Array[String]): Unit = {
    val selector: Selector = Selector.open()
    val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open()
    serverSocketChannel.bind(new InetSocketAddress("localhost", 8000))
    serverSocketChannel.configureBlocking(false)
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
    val lock: Object = new Object()
    val lock2: Object = new Object()
    while (true) {
      selector.select()
      val selectedKeys: util.Set[SelectionKey] = selector.selectedKeys()
      val iterator: util.Iterator[SelectionKey] = selectedKeys.iterator
      while (iterator.hasNext) {
        val key: SelectionKey = iterator.next()
        var workerCount: Int = currentWorkers
        while (workerCount > config("maxWorkers")) {
          workerCount = HttpServer.currentWorkers
          Thread.sleep(100)
        }
        lock.synchronized {
          currentWorkers = currentWorkers + 1
        }
        if (key.isAcceptable) {
          val client: SocketChannel = serverSocketChannel.accept()
          client.configureBlocking(false)
          client.register(selector, SelectionKey.OP_READ)
        }
        if (key.isReadable) {
          Future {
            val connect: SocketChannel = key.channel().asInstanceOf[SocketChannel]
            val request: HttpRequest = new HttpRequest(connect)
            val urlInfo: UrlInfoCase = UrlInfo.parse("/")
            val clazz = Class.forName(urlInfo.controller)
            val method = clazz.getMethod(urlInfo.method)
            method.invoke(clazz.getDeclaredConstructor().newInstance())
            iterator.remove()
            lock2.synchronized {
              currentWorkers = currentWorkers - 1
            }
          }
        }
      }
    }
  }
}