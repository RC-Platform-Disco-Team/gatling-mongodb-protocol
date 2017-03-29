import io.gatling.core.Predef._
import com.ringcentral.gatling.mongo.Predef._

import scala.concurrent.duration._

class MongoSimulation extends Simulation {
  private val mongoProtocol = mongo
    .uri("mongodb://admin:admin@localhost:27017/messages?rm.tcpNoDelay=true&rm.nbChannelsPerNode=10")

  val feeder = mongoFeeder("mongodb://admin:admin@localhost:27017/messages?rm.tcpNoDelay=true&rm.nbChannelsPerNode=10", "messages", "{}", limit=100000)

  val scn = scenario("Mongo scenario")
      .feed(feeder)
    .exec(mongo("count before").collection("messages").count().skip(5).limit(7).hint("{\"_id\": -1}").check(count.greaterThan(1).saveAs("messages_count")))
    .exec(mongo("custom command").command.execute("{\"aggregate\": \"messages\", \"pipeline\": [{\"$match\": {\"_acc\": \"1\"}}]}").check(jsonPath("$.ok").is("1")))
    .exec(mongo("find all").collection("messages").find("{}").sort("{\"_id\": 1}").check(jsonPath("$.._id").find.saveAs("_id")))
    .exec(mongo("find specific").collection("messages").find("{\"_id\": ${_id}}").check(jsonPath("$.._id").find.is("${_id}")))
    .exec(mongo("insert").collection("messages").insert("{\"_acc\": \"1\"}"))
    .exec(mongo("update specific").collection("messages").update("{\"_id\": ${_id}}", "{\"messages_count\": \"${messages_count}\"}"))
    .exec(mongo("remove specific").collection("messages").remove("{\"_id\": ${_id}}"))
    .exec(mongo("count after").collection("messages").count().check(count.greaterThan(1)))

  setUp(
    scn.inject(
      atOnceUsers(1)
    )
  ).protocols(mongoProtocol)

}
