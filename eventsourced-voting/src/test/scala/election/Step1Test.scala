package election

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import election.step1.commands.Step1._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FeatureSpec, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import akka.pattern.ask
import election.step1.actor.ElectionActor

class Step1Test extends FeatureSpec with Matchers with ScalaFutures with IntegrationPatience {

  implicit val timeout = Timeout(FiniteDuration(50000, "ms"))
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  def sendVote(actor: ActorRef, vote: Vote): Future[VoteResponse] = {
    val future = actor ? vote
    future.mapTo[VoteResponse]
  }

  def generateVotes(counts: Seq[(Candidate, Int)]): Seq[Vote] = {
    counts.flatMap {
      case (candidate, count) => (1 to count).map(_ => Vote(candidate))
    }
  }

  feature("basic election") {

    val voteCounts = Map("c2" -> 6, "c1" -> 10)

    scenario("cast votes") {
      // create actor system
      val system = ActorSystem("test1")
      // create actor ElectionActor
      val electionActor: ActorRef = system.actorOf(ElectionActor.randomElectionProps)

      // generate votes
      val votes: Seq[Vote] = generateVotes(voteCounts.toSeq)

      // submit votes and gather (future) responses
      val voteResponses: Seq[Future[VoteResponse]] = votes.map { vote =>
        sendVote(electionActor, vote)
      }
      // turn seq[future[response]] inside out: future[seq[response]]
      val sequenced: Future[Seq[VoteResponse]] = Future.sequence(voteResponses)
      // have all votes been successfully submitted?
      sequenced.futureValue.forall(_.success) should be(true)

      // ask for winner
      val fWinnerResponseAny: Future[Any] = electionActor ? GetWinner()
      val fWinnerResponse: Future[GetWinnerResponse] = fWinnerResponseAny.mapTo[GetWinnerResponse]
      // winner should be c1
      fWinnerResponse.futureValue.winner should be(Some("c1"))
    }
  }

  feature("bigger election") {
    // src: https://en.wikipedia.org/wiki/United_States_presidential_election,_1800
    val electoralVoteCounts: Seq[(Candidate, Int)] = ("John Adams", 65) :: ("Thomas Jefferson", 73) :: Nil

    scenario("cast votes") {
      val system = ActorSystem("test1")
      val electionActor = system.actorOf(ElectionActor.randomElectionProps)

      val votes = generateVotes(electoralVoteCounts)

      val voteResponses = votes.map(sendVote(electionActor, _))
      Future.sequence(voteResponses).futureValue.forall(_.success) should be(true)

      val fWinnerResponse: Future[GetWinnerResponse] = (electionActor ? GetWinner()).mapTo[GetWinnerResponse]
      fWinnerResponse.futureValue.winner should be(Some("Thomas Jefferson"))
    }
  }


  feature("even bigger election") {
    // src: https://en.wikipedia.org/wiki/United_States_presidential_election,_1800
    val voteCounts: Seq[(Candidate, Int)] = ("John Adams", 25952) :: ("Thomas Jefferson", 41330) :: Nil

    scenario("cast votes") {
      val system = ActorSystem("test1")
      val electionActor = system.actorOf(ElectionActor.randomElectionProps)

      val votes = generateVotes(voteCounts)

      val voteResponses = votes.map(sendVote(electionActor, _))
      Future.sequence(voteResponses).futureValue.forall(_.success) should be(true)

      val fWinnerResponse: Future[GetWinnerResponse] = (electionActor ? GetWinner()).mapTo[GetWinnerResponse]
      fWinnerResponse.futureValue.winner should be(Some("Thomas Jefferson"))
    }
  }
}
