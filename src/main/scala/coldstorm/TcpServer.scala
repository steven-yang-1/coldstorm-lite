package coldstorm

import java.net.InetSocketAddress
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}
import java.util
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TcpServer extends TransportLayer {
  var hostName: String = "localhost"
  override def launch(): Unit = {
    val selector: Selector = Selector.open()
    val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open()
    serverSocketChannel.bind(new InetSocketAddress(hostName, port))
    serverSocketChannel.configureBlocking(false)
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
    while (true) {
      selector.select()
      val selectedKeys: util.Set[SelectionKey] = selector.selectedKeys()
      val iterator: util.Iterator[SelectionKey] = selectedKeys.iterator
      while (iterator.hasNext) {
        val key: SelectionKey = iterator.next()
        if (key.isAcceptable) {
          val client: SocketChannel = serverSocketChannel.accept()
          client.configureBlocking(false)
          client.register(selector, SelectionKey.OP_READ)
        }
        if (key.isReadable) {
          Future {
            val connect: SocketChannel = key.channel().asInstanceOf[SocketChannel]
            val appLayer: ApplicationLayer = new ApplicationServer.applicationLayer(connect)
            appLayer.launch()
          }
        }
        iterator.remove()
      }
    }
  }
}
