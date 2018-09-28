package services
import javax.inject._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import scala.concurrent.Future
import play.api.libs.ws._
import play.api.http.HttpEntity
import play.api.libs.json._

/**
  * List of available environment types in the latest OpenWhisk
  */
object TaskKind extends Enumeration {
  val php7 = Value("php:7.1")
  val swift4 = Value("swift:4.1")
  val node8 = Value("nodejs:8")
  val nodejs = Value("nodejs")
  val blackbox = Value("blackbox")
  val java = Value("java")
  val sequence = Value("sequence")
  val node6 = Value("nodejs:6")
  val python3 = Value("python:3")
  val python = Value("python")
  val python2 = Value("python:2")
  val swift3 = Value("swift:3.1.1")
}

/**
  * WskService.scala
  *
  * Context:
  * The class provides interface to OpenWhisk backend. Username and password are
  * mandatory security requirements and must be provided in order to operate this class.
  * Naming convention for each utility method follows:
  * [R] get...
  * [W] create..
  * [U] update..
  * [D] delete..
  *
  * @param ws
  * @param fileEncoder
  * @param executionContext
  */
@Singleton
class WskService @Inject() (
  ws: WSClient,
  fileEncoder: FileEncoder
)(implicit executionContext: ExecutionContext) {
  /**
    * Get available name spaces in the OpenWhisk instance
    * @return Future<String>
    */
  def getNamespaces(): Future[String] = {
    // TODO: String interpolation + abstracted value

    ws.url("https://localhost/api/v1/namespaces")
      .withAuth("23bc46b1-71f6-4ed5-8c54-816aa4f8c502", "123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP", WSAuthScheme.BASIC)
      .get()
      .map { response => response.body }
  }

  /**
    * Create an action using the OpenWhisk REST API.
    * It base64 encodes the zip file of an action and
    * post it to OpenWhisk using PUT method to create the action.
    * Note that the function is called createTask but it's technically
    * creating an Action from OpenWhisk's end
    * @param appName
    * @param taskType
    * @param taskName
    * @param kind
    * @return
    */
  def createTask(
    appName: String,
    taskType: String,
    taskName: String,
    kind: TaskKind.Value
  ): Future[String] = {
    val encodedAction = fileEncoder.getActionAsBase64(appName, taskType, taskName)
    val body: JsValue = JsObject(Seq(
      "exec" -> JsObject(Seq(
        "kind" -> JsString(kind.toString),
        "code" -> JsString(encodedAction)
      ))
    ))
    ws.url("https://localhost/api/v1/namespaces/guest/actions/hello")
      .withHeaders("Accept" -> "application/json")
      .withAuth("23bc46b1-71f6-4ed5-8c54-816aa4f8c502", "123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP", WSAuthScheme.BASIC)
      .put(body)
      .map { response => response.body }
  }
}