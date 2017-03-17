import io.gatling.core.Predef._
import com.ringcentral.gatling.mongo.Predef._

import scala.concurrent.duration._

class MongoSimulation extends Simulation {
  private val mongoProtocol = mongo
    .uri("mongodb://admin:admin@localhost:27017/messages?rm.tcpNoDelay=true")
    .nbChannelsPerNode(50)

  val scn = scenario("Insert document scenario")
    .exec(mongo("count before").collection("messages").count.check(count.greaterThan(6163515).saveAs("messages_count")))
    .exec(mongo("custom command").command.execute("{\"eval\": \"db\"}").check(jsonPath("$.ok").is("1.0")))
    .exec(mongo("find all").collection("messages").find("{}").check(jsonPath("$.._id").find.saveAs("_id")))
    .exec(mongo("find specific").collection("messages").find("{\"_id\": \"${_id}\"}").check(jsonPath("$.._id").find.is("${_id}")))
    .exec(mongo("insert empty").collection("messages").insert("{}"))
    .exec(mongo("update specific").collection("messages").update("{\"_id\": \"${_id}\"}", "{\"messages_count\": \"${messages_count}\"}"))
    .exec(mongo("remove specific").collection("messages").remove("{\"_id\": \"${_id}\"}"))
    .exec(mongo("count after").collection("messages").count.check(count.is("${messages_count}")))

  setUp(
    scn.inject(
      atOnceUsers(1)
    )
  ).protocols(mongoProtocol)

}
