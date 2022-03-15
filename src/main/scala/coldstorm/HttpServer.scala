package coldstorm

import java.net.InetSocketAddress
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}
import java.util
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HttpServer {
  def main(args: Array[String]): Unit = {
    val selector: Selector = Selector.open()
    val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open()
    serverSocketChannel.bind(new InetSocketAddress("localhost", 8000))
    serverSocketChannel.configureBlocking(false)
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
    val maxWorkers = 10
    var currentWorkers = 0
    val lock: Object = new Object()
    val lock2: Object = new Object()
    while (true) {
      selector.select()
      val selectedKeys: util.Set[SelectionKey] = selector.selectedKeys()
      val iterator: util.Iterator[SelectionKey] = selectedKeys.iterator
      while (iterator.hasNext) {
        val key: SelectionKey = iterator.next()
        lock.synchronized {
          lock.wait()
          currentWorkers = currentWorkers + 1
          lock.notify()
        }
        Future {
          if (key.isAcceptable) {
            val client: SocketChannel = serverSocketChannel.accept()
            client.configureBlocking(false)
            client.register(selector, SelectionKey.OP_READ)
          }
          if (key.isReadable) {
            val connect: SocketChannel = key.channel().asInstanceOf[SocketChannel]
            val request: HttpRequest = new HttpRequest(connect)
            val urlInfo: UrlInfoCase = UrlInfo.parse("/")

            iterator.remove()
            lock2.synchronized {
              lock2.wait()
              currentWorkers = currentWorkers - 1
              lock2.notify()
            }
          }
        }
      }
    }
  }
}