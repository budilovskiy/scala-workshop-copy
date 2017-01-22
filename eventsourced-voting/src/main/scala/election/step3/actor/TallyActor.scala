package election.step3.actor

import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedView
import election.step3.commands.Step3.{GetWinner, GetWinnerResponse}
import election.step3.domain.VoteRegistry
import election.step3.events.Voted

class TallyActor(val eventLog: ActorRef) extends EventsourcedView {
  println(s"started tally actor @ ${self.path}")

  def id: String = "tally"

  var registry = VoteRegistry(Map.empty)

  def onCommand: Receive = {
    case GetWinner() => sender ! GetWinnerResponse(registry.winner)
  }

  def onEvent: Receive = {
    case Voted(candidate) =>
      registry = registry.withVote(candidate)
  }
}
