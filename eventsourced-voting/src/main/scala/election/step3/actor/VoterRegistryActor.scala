package election.step3.actor

import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedActor
import election.step3.commands.Step3._
import election.step3.domain.VoterRegistry
import election.step3.events.Registred

import scala.util.{Failure, Success}

class VoterRegistryActor(val eventLog: ActorRef) extends EventsourcedActor {
  println(s"started registry actor @ ${self.path}")

  var registry = VoterRegistry(Set())

  def id: String = "voterRegistry"

  def onCommand: Receive = {

    case RegisterVoter(name, area) =>
      val voterId = VoterId(area, name)
      if (!registry.voters.contains(voterId)) {
        persist(Registred(voterId)) {
          case Success(evt) => sender() ! RegisterVoterResponse(Some(voterId))
          case Failure(err) => sender() ! RegisterVoterResponse(None)
        }
      } else {
        sender() ! RegisterVoterResponse(None)
      }

  }

  def onEvent: Receive = {
    case Registred(voterId) =>
      registry = registry.register(voterId)
  }
}


