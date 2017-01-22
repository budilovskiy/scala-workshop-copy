package election.step3.actor

import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedActor
import election.step3.commands.Step3.{Vote, VoteResponse, VoterId}
import election.step3.domain.{VoteRegistry, VoterRegistry}
import election.step3.events.{Registred, Voted}

import scala.util.{Failure, Success}

class RegistryActor(val eventLog: ActorRef) extends EventsourcedActor {
  println(s"started registry actor @ ${self.path}")

  var registry = VoteRegistry(Map.empty)

  var votersRegistry = VoterRegistry(Set())
  var votedIds = Set[VoterId]()

  def id: String = "registry"

  def onCommand: Receive = {
    case Vote(voterId, candidate) =>
      if (votersRegistry.voters.contains(voterId) && !votedIds.contains(voterId)) {
        votedIds = votedIds + voterId
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
    case Registred(voter) =>
      votersRegistry = votersRegistry.register(voter)
  }
}


