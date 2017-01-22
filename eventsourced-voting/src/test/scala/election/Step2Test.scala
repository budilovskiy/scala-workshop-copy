package election

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import election.step2.commands.Step2._
import election.step2.actor.ElectionActor
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FeatureSpec, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class Step2Test extends FeatureSpec with Matchers with ScalaFutures with IntegrationPatience {

  implicit val timeout = Timeout(FiniteDuration(10000, "ms"))
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  def sendVote(actor: ActorRef, vote: Vote): Future[VoteResponse] = {
    val future = actor ? vote
    future.mapTo[VoteResponse]
  }

  def generateVotes(counts: Seq[(Candidate, Int)]): Seq[Vote] = {
    val missingVoter: Seq[(VoterId) => Vote] = counts.flatMap {
      case (candidate, count) => (1 to count).map(_ => Vote(_: VoterId, candidate))
    }
    missingVoter.zipWithIndex.map {
      case (fun, voterInt) => fun(voterInt)
    }
  }

  feature("basic election") {

    val voteCounts = Map("c2" -> 6, "c1" -> 10)

    scenario("cast votes") {
      val system = ActorSystem("test1")
      val electionActor = system.actorOf(ElectionActor.randomElectionProps)

      val votes = generateVotes(voteCounts.toSeq)

      val voteResponses = votes.map(sendVote(electionActor, _))
      Future.sequence(voteResponses).futureValue.forall(_.voteRegistered) should be(true)

      val fWinner = (electionActor ? GetWinner()).mapTo[GetWinnerResponse]
      fWinner.futureValue.winner should be(Some("c1"))
    }

    scenario("don't count double votes") {
      val system = ActorSystem("test1")
      val electionActor = system.actorOf(ElectionActor.randomElectionProps)

      val votes = generateVotes(voteCounts.toSeq)

      val voteResponses = votes.map(sendVote(electionActor, _))
      Future.sequence(voteResponses).futureValue.forall(_.voteRegistered) should be(true)

      val repeatResponses = votes.take(10).map(sendVote(electionActor, _))
      Future.sequence(repeatResponses).futureValue.forall(_.voteRegistered) should be(false)

      val fWinner = (electionActor ? GetWinner()).mapTo[GetWinnerResponse]
      fWinner.futureValue.winner should be(Some("c1"))
    }

    scenario("don't count consecutive double votes") {
      val system = ActorSystem("test1")
      val electionActor = system.actorOf(ElectionActor.randomElectionProps)

      val votes = generateVotes(voteCounts.toSeq)

      val voteResponses = votes.map { vote =>
        val f1 = (1 to 5).map(_ => sendVote(electionActor, vote))
        Future.sequence(f1)
      }

      Future.sequence(voteResponses).futureValue.foreach { duplicateVotes =>
        duplicateVotes.filter(_.voteRegistered).size should be(1)
      }

      val fWinner = (electionActor ? GetWinner()).mapTo[GetWinnerResponse]
      fWinner.futureValue.winner should be(Some("c1"))
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

      Future.sequence(voteResponses).futureValue.forall(_.voteRegistered) should be(true)

      val fWinner = (electionActor ? GetWinner()).mapTo[GetWinnerResponse]
      fWinner.futureValue.winner should be(Some("Thomas Jefferson"))
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

      Future.sequence(voteResponses).futureValue.forall(_.voteRegistered) should be(true)

      val fWinner = (electionActor ? GetWinner()).mapTo[GetWinnerResponse]
      fWinner.futureValue.winner should be(Some("Thomas Jefferson"))
    }
  }
}
