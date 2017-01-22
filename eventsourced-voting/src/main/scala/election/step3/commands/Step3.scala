package election.step3.commands

import election.Base.Command

object Step3 {

  type Area = Int

  case class VoterId(area: Area, uuid: String)

  type Candidate = String

  case class RegisterVoter(name: String, area: Area) extends Command

  case class RegisterVoterResponse(voterId: Option[VoterId]) extends Command

  case class Vote(voterId: VoterId, candidate: Candidate) extends Command

  case class VoteResponse(voteRegistered: Boolean) extends Command

  case class CloseElection() extends Command

  case class CloseElectionResponse() extends Command

  case class GetWinner() extends Command

  case class GetWinnerResponse(winner: Option[Candidate]) extends Command
}
