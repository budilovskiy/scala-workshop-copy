package election

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import election.step3.commands.Step3._
import election.step3.actor.ElectionActor
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FeatureSpec, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

// step 2: verteilung, hoffentlich schneller?
class Step3Test extends FeatureSpec with Matchers with ScalaFutures with IntegrationPatience {

  implicit val timeout = Timeout(FiniteDuration(10000, "ms"))
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  def sendVote(actor: ActorRef, vote: Vote): Future[VoteResponse] = {
    val future = actor ? vote
    future.mapTo[VoteResponse]
  }

  def registerVoter(actor: ActorRef, name: String, area: Area): Future[RegisterVoterResponse] = {
    val future = actor ? RegisterVoter(name, area)
    future.mapTo[RegisterVoterResponse]
  }

  def generateVotes(counts: Seq[(Candidate, Int)], assignedVoterId: Iterable[VoterId]): Seq[Vote] = {
    val missingVoter: Seq[(VoterId) => Vote] = counts.flatMap {
      case (candidate, count) => (1 to count).map(_ => Vote(_: VoterId, candidate))
    }

    missingVoter.zip(assignedVoterId).map {
      case (fun, voterId) => fun(voterId)
    }
  }


  def uniqueNames(firstNames: Iterable[String], lastNames: Iterable[String], count: Int): Iterable[String] = {
    if (count > firstNames.size * lastNames.size) throw new RuntimeException(s"cannot supply $count random names")

    val allNames = for {
      fn <- firstNames
      ln <- lastNames
    } yield (fn, ln)

    Random.shuffle(allNames).take(count).map {
      case (firstName, lastName) => s"$firstName $lastName"
    }
  }

  feature("voter registration") {
    scenario("disallow duplicate registration") {
      val system = ActorSystem("test1")
      val electionActor = system.actorOf(ElectionActor.randomElectionProps)

      val name = "Diego Duplicate"

      val r1 = (electionActor ? RegisterVoter(name, 1)).mapTo[RegisterVoterResponse]
      val r2 = (electionActor ? RegisterVoter(name, 1)).mapTo[RegisterVoterResponse]
      r1.futureValue.voterId should not be (empty)
      r2.futureValue.voterId should be (empty)
    }
  }

  feature("basic election") {

    // src: https://de.wikipedia.org/wiki/Liste_von_Persönlichkeiten_der_Stadt_Dortmund#Stadtoberh.C3.A4upter
    val firstNames: Seq[String] = "Hermann" :: "Wilhelm" :: "Herbert" :: "Fritz" :: "Dietrich" :: "Heinrich" ::
      "Günter" :: "Gerhard" :: "Ullrich" :: Nil
    val lastNames: Seq[String] = "Ostrop" :: "Hansmann" :: "Scholtissek" :: "Henßler" :: "Keuning" :: "Sondermann" ::
      "Samtlebe" :: "Langemeyer" :: "Sierau" :: Nil

    // src: http://www.dastelefonbuch.de/Postleitzahlen/Dortmund
    val areas = Set(44135, 44137, 44139, 44141, 44143, 44145, 44147, 44149, 44225, 44227, 44263, 44329, 44339, 44369)
    def randomArea() = Random.shuffle(areas).head

    def uniqueMayors(count: Int): Iterable[String] = {
      if (count > firstNames.size * lastNames.size) throw new RuntimeException(s"cannot supply $count random names")

      val allNames = for {
        fn <- firstNames
        ln <- lastNames
      } yield (fn, ln)

      Random.shuffle(allNames).take(count).map {
        case (firstName, lastName) => s"$firstName $lastName"
      }
    }

    val voteCounts = Map("c2" -> 10, "c1" -> 30)
    val voterCount = voteCounts.values.sum

    scenario("register voters and cast votes") {
      val system = ActorSystem("test1")
      val electionActor = system.actorOf(ElectionActor.randomElectionProps)

      val registerResponses = uniqueMayors(voterCount).map(name => registerVoter(electionActor, name, randomArea()))

      val sequencedResponses = Future.sequence(registerResponses).futureValue
      sequencedResponses.forall(_.voterId.nonEmpty) should be (true)

      val voterIds = sequencedResponses.flatMap(_.voterId)
      val votes = generateVotes(voteCounts.toSeq, voterIds)

      val voteResponses = votes.map(sendVote(electionActor, _))
      Future.sequence(voteResponses).futureValue.forall(_.voteRegistered) should be(true)

      val fWinner = (electionActor ? GetWinner()).mapTo[GetWinnerResponse]
      fWinner.futureValue.winner should be(Some("c1"))
    }

    scenario("don't count double votes") {
      val system = ActorSystem("test1")
      val electionActor = system.actorOf(ElectionActor.randomElectionProps)

      val registerResponses = uniqueMayors(voterCount).map(name => registerVoter(electionActor, name, randomArea()))

      val sequencedResponses = Future.sequence(registerResponses).futureValue
      sequencedResponses.forall(_.voterId.nonEmpty) should be (true)

      val voterIds = sequencedResponses.flatMap(_.voterId)
      val votes = generateVotes(voteCounts.toSeq, voterIds)

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

      val registerResponses = uniqueMayors(voterCount).map(name => registerVoter(electionActor, name, randomArea()))

      val sequencedResponses = Future.sequence(registerResponses).futureValue
      sequencedResponses.forall(_.voterId.nonEmpty) should be (true)

      val voterIds = sequencedResponses.flatMap(_.voterId)
      val votes = generateVotes(voteCounts.toSeq, voterIds)

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
    val voterCount = electoralVoteCounts.unzip._2.sum

    def uniqueNames(count: Int): Iterable[String] = {
      (1 to count * 2).grouped(2).toSeq.map {
        case Seq(firstNum, secondNum) => s"$firstNum $secondNum"
      }
    }

    def randomArea() = Random.nextInt(100)

    scenario("cast votes") {
      val system = ActorSystem("test1")

      val electionActor = system.actorOf(ElectionActor.randomElectionProps)

      val registerResponses = uniqueNames(voterCount).map(name => registerVoter(electionActor, name, randomArea()))

      val sequencedResponses = Future.sequence(registerResponses).futureValue
      sequencedResponses.forall(_.voterId.nonEmpty) should be (true)

      val voterIds = sequencedResponses.flatMap(_.voterId)

      val votes = generateVotes(electoralVoteCounts, voterIds)

      val voteResponses = votes.map(sendVote(electionActor, _))

      Future.sequence(voteResponses).futureValue.forall(_.voteRegistered) should be(true)

      val fWinner = (electionActor ? GetWinner()).mapTo[GetWinnerResponse]
      fWinner.futureValue.winner should be(Some("Thomas Jefferson"))
    }
  }


  feature("even bigger election") {
    // src: https://en.wikipedia.org/wiki/United_States_presidential_election,_1800
    val voteCounts: Seq[(Candidate, Int)] = ("John Adams", 25952) :: ("Thomas Jefferson", 41330) :: Nil
    val voterCount = voteCounts.unzip._2.sum

    def uniqueNames(count: Int): Iterable[String] = {
      (1 to count * 2).grouped(2).toSeq.map {
        case Seq(firstNum, secondNum) => s"$firstNum $secondNum"
      }
    }

    def randomArea() = Random.nextInt(100)

    scenario("cast votes") {
      val system = ActorSystem("test1")

      val electionActor = system.actorOf(ElectionActor.randomElectionProps)

      val registerResponses = uniqueNames(voterCount).map(name => registerVoter(electionActor, name, randomArea()))

      val sequencedResponses = Future.sequence(registerResponses).futureValue
      sequencedResponses.forall(_.voterId.nonEmpty) should be (true)

      val voterIds = sequencedResponses.flatMap(_.voterId)

      val votes = generateVotes(voteCounts, voterIds)

      val voteResponses = votes.map(sendVote(electionActor, _))

      Future.sequence(voteResponses).futureValue.forall(_.voteRegistered) should be(true)

      val fWinner = (electionActor ? GetWinner()).mapTo[GetWinnerResponse]
      fWinner.futureValue.winner should be(Some("Thomas Jefferson"))
    }
  }
}
