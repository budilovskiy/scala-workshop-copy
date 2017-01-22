package election.step2.commands

import election.Base.Command

object Step2 {
  type VoterId = Long

  type Candidate = String

  case class Vote(voterId: VoterId, candidate: Candidate) extends Command

  case class VoteResponse(voteRegistered: Boolean) extends Command

  case class GetWinner() extends Command

  case class GetWinnerResponse(winner: Option[Candidate]) extends Command
}
