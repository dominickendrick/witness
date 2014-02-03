package witness

import org.scalatra.scalate
import scalate.ScalateSupport
import dispatch._ 
import Defaults._
import net.liftweb.json._

class GuardianWitnessScalatraServlet extends GuardianwitnessWidgetStack {

    get("/") {

        val serviceUrl = "http://n0ticeapis.com/2/search"        
//        val serviceUrl = "http://localhost:8000/witness.json"
        val validWitnessParams = List("group", "location", "q")
        contentType = "text/html"
                
        def getWitnessParams(q: List[String]): Map[String, String] = {
            q.flatMap { key =>
                params.get(key).map { v => (key, v) }
            }.toMap
        }

        def getWitnessData = {
            dispatch.Http(
                dispatch.url(serviceUrl) <<? getWitnessParams(validWitnessParams) OK as.lift.Json
            )
        }

        val json = getWitnessData()
        
        val data = for (child <- (json \ "results").children) {
           WitnessData(child.asInstanceOf[JObject]) 
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
        def apply(json:JObject) = { 
            implicit val formats = net.liftweb.json.DefaultFormats

            val webUrl = (json \ "webUrl").extract[String]
            val image = (json \ "updates" \ "0" \ "image" \ "extralarge")
            val headline = (json \ "updates")
            val user = List(
                Map("profileUrl" -> (json \ "user" \ "0" \ "image" \ "extralarge")),
                Map("username" -> (json \ "user" \ "0" \ "image" \ "extralarge"))
            )
            List(webUrl, image, headline, user)
        }
    }   
}
