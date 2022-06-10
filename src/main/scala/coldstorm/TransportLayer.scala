package coldstorm

abstract class TransportLayer {
  var port: Int = 8080
  def launch(): Unit
}