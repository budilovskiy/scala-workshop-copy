package election.step1.events

import election.Base.Event
import election.step1.commands.Step1.Candidate

case class Voted(candidate: Candidate) extends Event