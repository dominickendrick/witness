package witness

import org.scalatra.scalate
import scalate.ScalateSupport
import dispatch._ 
import Defaults._
import net.liftweb.json._

class GuardianWitnessScalatraServlet extends GuardianwitnessWidgetStack {

    get("/") {
        
        val listLength = 5
        val serviceUrl = "http://n0ticeapis.com/2/search"        
        val validWitnessParams = List("group", "location", "q")
        contentType = "text/html"
                
        def getWitnessParams(q: List[String]): Map[String, String] = {
            q.flatMap { key =>
                params.get(key).map { v => (key, v) }
            }.toMap ++ Map("hasImages" -> "true")
        }

        def getWitnessData = {            
            dispatch.Http(
                dispatch.url(serviceUrl) <<? getWitnessParams(validWitnessParams) OK as.lift.Json
            )
        }

        val json = getWitnessData()
        
        val data = for {
                child <- (json \ "results").children
            } yield {
                WitnessData(child.asInstanceOf[JObject])
            }
            
        mustache("witness", ("results", data.take(listLength)) )
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
            val image = (json \ "updates" \ "image" \ "large").extractOrElse[String]("no image")
            val headline = (json \ "headline").extract[String]
            val userProfileUrl = (json \ "updates" \ "user" \"profileUrl" ).extractOrElse[String]("no profile Url")
            val username = (json \ "user" \ "username").extractOrElse[String]("no username")
            Map("webUrl" -> webUrl, "image" -> image, "headline" -> headline , "userProfileUrl" -> userProfileUrl, "username" -> username)
        }
    }   
}
