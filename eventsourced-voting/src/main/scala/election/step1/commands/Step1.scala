package election.step1.commands

import election.Base.Command

object Step1 {
  type Candidate = String

  case class Vote(candidate: Candidate) extends Command

  case class VoteResponse(success: Boolean) extends Command

  case class GetWinner() extends Command

  case class GetWinnerResponse(winner: Option[Candidate]) extends Command
}
