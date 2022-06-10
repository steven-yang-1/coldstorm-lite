package coldstorm

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class PlainTextProcessor(override val socketChannel: SocketChannel) extends ApplicationLayer(socketChannel) {
  override def launch(): Unit = {
    val buffer: ByteBuffer = ByteBuffer.allocate(102400)
    socketChannel.read(buffer)
    buffer.flip
    socketChannel.write(ByteBuffer.wrap("HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: 4\r\n\r\nTEST".getBytes()))
  }
}
