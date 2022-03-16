package coldstorm

object UrlInfo {
  def parse(url: String): UrlInfoCase = {
    val parts: Array[String] = url.split('/')
    val method = parts.last
    val controller = parts(parts.length - 2)
    val controllerArray = controller.split('_')
    val controllerArray1 = controllerArray.map((value: String) => {
      value.capitalize
    })
    val controllerName: String = controllerArray1.mkString
    UrlInfoCase(controllerName, method)
  }
}

case class UrlInfoCase(controller: String, method: String)