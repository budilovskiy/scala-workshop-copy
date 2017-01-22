package election.step2.events

import election.Base.Event
import election.step2.commands.Step2.Candidate

case class Voted(candidate: Candidate) extends Event