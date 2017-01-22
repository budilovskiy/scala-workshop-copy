package election.step3.domain

import election.step3.commands.Step3._

case class VoterRegistry(voters: Set[VoterId]) {

  def register(voterId: VoterId): VoterRegistry = {
    copy(voters = voters + voterId)
  }

  def getAreas: Set[Area] = voters.map(_.area)

}