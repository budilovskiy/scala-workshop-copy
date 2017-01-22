package election.step1.actor

import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedActor
import election.step1.commands.Step1.{Vote, VoteResponse}
import election.step1.domain.VoteRegistry
import election.step1.events.Voted

import scala.util.{Failure, Success}

class RegistryActor(val eventLog: ActorRef) extends EventsourcedActor {
  println(s"started registry actor @ ${self.path}")

  var registry = VoteRegistry(Map.empty)

  def id: String = "registry"

  def onCommand: Receive = {
    case Vote(candidate) =>
      persist(Voted(candidate)) {
        case Success(evt) => sender() ! VoteResponse(true)
        case Failure(err) => sender() ! VoteResponse(false)
      }
  }

  def onEvent: Receive = {
    case Voted(candidate) =>
      registry = registry.withVote(candidate)
  }
}


