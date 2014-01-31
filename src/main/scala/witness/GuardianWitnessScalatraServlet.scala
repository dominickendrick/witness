package witness

import org.scalatra.scalate
import scalate.ScalateSupport
import dispatch._ 
import Defaults._
import play.api.libs.json._

class GuardianWitnessScalatraServlet extends GuardianwitnessWidgetStack {

    get("/") {

        val serviceUrl = "http://n0ticeapis.com/2/search"        
        val validWitnessParams = List("group", "location", "q")
        val contentType = "text/html"
                
        def getWitnessParams(q: List[String]): Map[String, String] = {
            val list = for {   
                param <- q
                val value = params(param.toString)
                if value != None
            } yield {
                    (value.toString, value)
            }
            list.toMap
        }
        
        def getWitnessData = {
            dispatch.Http(
                dispatch.url(serviceUrl) <<? getWitnessParams(validWitnessParams) OK as.String
            )
        }
    
        val json = Json.parse(getWitnessData())
        
        val data = (json \ "results").as[List[JsObject]].map { 
            witnessValues => {
                val data = WitnessData.apply( witnessValues )
            }
        }
        
        mustache("witness", ("results", data) )

    }    
    
    case class WitnessData(
       webUrl: String,
       image: String,
       headline: String,
       user: List[Map[String,String]]
    )
    
    object WitnessData{
        def apply(json:JsObject) = { 
            val webUrl = (json \ "webUrl").as[String]
            val image = (json \ "updates" \ "0" \ "image" \ "extralarge").asInstanceOf[String]
            val headline = (json \ "updates")
            val user = List(
                Map("profileUrl" -> (json \ "user" \ "0" \ "image" \ "extralarge").asInstanceOf[String]),
                Map("username" -> (json \ "user" \ "0" \ "image" \ "extralarge").asInstanceOf[String])
            )
            List(webUrl, image, headline, user)
        }
    }   
}
