package election.step2.actor

import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedActor
import election.step2.commands.Step2.{Vote, VoteResponse}
import election.step2.domain.VoteRegistry
import election.step2.events.Voted

import scala.util.{Failure, Success}

class RegistryActor(val eventLog: ActorRef) extends EventsourcedActor {
  println(s"started registry actor @ ${self.path}")

  var registry = VoteRegistry(Map.empty)

  private var ids: Set[Long] = Set.empty

  def id: String = "registry"

  def onCommand: Receive = {
    case Vote(voterId, candidate) =>
      if (!ids.contains(voterId)) {
        ids = ids + voterId
        persist(Voted(candidate)) {
          case Success(evt) => sender() ! VoteResponse(true)
          case Failure(err) => sender() ! VoteResponse(false)
        }
      } else {
        sender() ! VoteResponse(false)
      }

  }

  def onEvent: Receive = {
    case Voted(candidate) =>
      registry = registry.withVote(candidate)
  }
}


