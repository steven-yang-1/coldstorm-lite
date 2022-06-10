package coldstorm

object ApplicationServer {
  val transportLayer: TransportLayer = new TcpServer()
  type applicationLayer = PlainTextProcessor
  def main(args: Array[String]): Unit = {
    System.out.println("COLDSTORM Lite 1.0.2 is now running...");
    transportLayer.launch();
  }
}
