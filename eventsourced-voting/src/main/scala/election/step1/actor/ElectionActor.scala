package election.step1.actor

import akka.actor.{Actor, Props}
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import election.step1.commands.Step1.{GetWinner, Vote}

import scala.util.Random

class ElectionActor(electionName: String) extends Actor {

  private val eventLog = context.actorOf(LeveldbEventLog.props(electionName))

  private val registryActor = context.actorOf(Props(new RegistryActor(eventLog)))

  private val tallyActor = context.actorOf(Props(new TallyActor(eventLog)))

  def receive: Receive = {
    case vote: Vote => registryActor.forward(vote)
    case getWinner: GetWinner => tallyActor.forward(getWinner)
  }
}

object ElectionActor {
  def randomElectionProps: Props = {
    val randomName = Random.alphanumeric.take(10).mkString
    Props(new ElectionActor(randomName))
  }
}