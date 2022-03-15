package coldstorm

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class HttpRequest(connect: SocketChannel) {
  val byteBuffer: ByteBuffer = ByteBuffer.allocate(10240)
}