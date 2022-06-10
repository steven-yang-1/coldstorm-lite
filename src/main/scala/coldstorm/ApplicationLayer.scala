package coldstorm

import java.nio.channels.SocketChannel

abstract class ApplicationLayer(val socketChannel: SocketChannel) {
  def launch(): Unit
}
