package election.step3.actor

import akka.actor.{Actor, Props}
import akka.routing._
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import election.step3.commands.Step3.{Area, GetWinner, RegisterVoter, Vote}

import scala.collection.immutable.IndexedSeq
import scala.util.Random

class ElectionActor(electionName: String) extends Actor {

  class AreasRoutingLogic(routeesMap: Map[Area, ActorRefRoutee]) extends RoutingLogic {
    override def select(message: Any, routees: IndexedSeq[Routee]): Routee =
      routeesMap(message.asInstanceOf[Vote].voterId.area)
  }

  private val eventLog = context.actorOf(LeveldbEventLog.props(electionName))

//  private val registryActor = context.actorOf(Props(new RegistryActor(eventLog)))

  private val voterRegistryActor = context.actorOf(Props(new VoterRegistryActor(eventLog)))

  private val tallyActor = context.actorOf(Props(new TallyActor(eventLog)))

  private var areas: Set[Area] = Set.empty

  def getRouter(areas: Set[Area]): Router = {
    def getRouteesMap(areas: Set[Area]): Map[Area, ActorRefRoutee] = {
      val routees = Vector.fill(areas.size) {
        val r = context.actorOf(Props(new RegistryActor(eventLog)))
        context watch r
        ActorRefRoutee(r)
      }
      (areas zip routees).toMap
    }

    val routeesMap = getRouteesMap(areas)
    val routees = routeesMap.values.toIndexedSeq

    Router(new AreasRoutingLogic(routeesMap), routees)
  }

  lazy val voteRouter = getRouter(areas)

  def receive: Receive = {
    case registerVoter: RegisterVoter =>
      areas = areas + registerVoter.area
      voterRegistryActor.forward(registerVoter)
//    case vote: Vote => registryActor.forward(vote)
    case vote: Vote => voteRouter.route(vote, sender())
    case getWinner: GetWinner => tallyActor.forward(getWinner)
  }

}

object ElectionActor {
  def randomElectionProps: Props = {
    val randomName = Random.alphanumeric.take(10).mkString
    Props(new ElectionActor(randomName))
  }
}