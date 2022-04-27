package coldstorm

import java.net.InetSocketAddress
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}
import java.util
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TcpServer {
  val config: Map[String, Any] = Map(
    "maxWorkers" -> 10,
    "port" -> 8000
  )
  var currentWorkers: Int = 0
  def launchWithLogic(proc : SocketChannel => Unit) {
    val selector: Selector = Selector.open()
    val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open()
    serverSocketChannel.bind(new InetSocketAddress("localhost", config.get("port").asInstanceOf[Int]))
    serverSocketChannel.configureBlocking(false)
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
    val lock: Object = new Object()
    while (true) {
      selector.select()
      val selectedKeys: util.Set[SelectionKey] = selector.selectedKeys()
      val iterator: util.Iterator[SelectionKey] = selectedKeys.iterator
      while (iterator.hasNext) {
        val key: SelectionKey = iterator.next()
        var workerCount: Int = currentWorkers
        while (workerCount >= config.get("maxWorkers")) {
          workerCount = TcpServer.currentWorkers
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
            proc(connect);
            iterator.remove()
            lock.synchronized {
              currentWorkers = currentWorkers - 1
            }
          }
        }
      }
    }
  }
  def main(args: Array[String]): Unit = {
    launchWithLogic((socketChannel: SocketChannel) => {
      new Controller(socketChannel).index()
    });
  }
}