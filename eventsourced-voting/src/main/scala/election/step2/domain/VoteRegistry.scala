package election.step2.domain

import election.step2.commands.Step2.Candidate

case class VoteRegistry(tally: Map[Candidate, Int]) {

  def withVote(candidate: Candidate): VoteRegistry = {
    val updCount = tally.getOrElse(candidate, 0) + 1
    copy(tally = tally + (candidate -> updCount))
  }

  def winner: Option[Candidate] = {
    if (tally.isEmpty) {
      None
    } else {
      val max = tally.toSeq.maxBy(_._2)
      Some(max._1)
    }
  }

  def tallyPercentages = {
    tally.map {
      case (candidate, count) => (candidate, (100d * count)/voteCount)
    }
  }

  lazy val voteCount = tally.values.sum
}
