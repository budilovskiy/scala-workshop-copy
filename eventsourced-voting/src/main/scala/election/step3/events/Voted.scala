package election.step3.events

import election.Base.Event
import election.step3.commands.Step3.{Candidate, VoterId}

case class Voted(candidate: Candidate) extends Event

case class Registred(voter: VoterId) extends Event