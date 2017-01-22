package lesson

import org.scalatest.{FeatureSpec, GivenWhenThen}

import scala.collection.immutable.TreeSet

class MatchDaySpec extends FeatureSpec with GivenWhenThen {

  feature("A new Matchday") {

    scenario("A new matchday") {
      Given("A new matchday")
      val newMatchDay = MatchDay(1)

      Then("Number of matches")
      val numberOfMatches = newMatchDay.numberOfMatches
      assert(numberOfMatches == 0)
    }

    scenario("Number of participants") {
      Given("Matchday1")
      val matchDay = MatchDay.MatchDay1

      Then("Number of teams")
      val numberOfParticipants = matchDay.numberOfTeams
      assert(numberOfParticipants == 6)
    }

    scenario("Number of goals") {
      Given("Matchday1")
      val matchDay = MatchDay.MatchDay1

      Then("Number of goals")
      val numberOfGoals = matchDay.numberOfGoals
      assert(numberOfGoals == 10)
    }

    scenario("Add matches to a new matchday") {
      Given("A new matchday")
      val newMatchDay = MatchDay(1)

      When("Add 3 matches")
      val updatedMatchDay = newMatchDay.addMatches(Match.match1, Match.match2, Match.match3)

      Then("Number of matches - equal to Matchday1")
      assert(updatedMatchDay.numberOfMatches == 3)
    }

    scenario("Sorted by Participant name") {
      Given("Two matchdays1")
      val matchDay = MatchDay.MatchDay1

      Then("participantsSortedByName")
      val participantsSortedByName = matchDay.teamsSortedByName
      assert(participantsSortedByName.indexOf(Team.Bayern) == 0)
      assert(participantsSortedByName.indexOf(Team.Dortmund) == 1)
      assert(participantsSortedByName.indexOf(Team.Kaiserslautern) == 2)
      assert(participantsSortedByName.indexOf(Team.Koeln) == 3)
      assert(participantsSortedByName.indexOf(Team.Leverkusen) == 4)
      assert(participantsSortedByName.indexOf(Team.Schalke) == 5)
    }

    scenario("Set of exciting matches") {
      Given("Two matchdays1")
      val matchDay = MatchDay.MatchDay1
      val matchDay2 = MatchDay.MatchDay2

      Then("Check number of redcards in section")
      val excitingMatches = MatchDay.excitingMatches(matchDay :: matchDay2 :: Nil)
      assert(excitingMatches == List(Match.match1))
    }

    scenario("Winning teams") {
      Given("Two matchdays1")
      val matchDay = MatchDay.MatchDay1

      Then("Winner is called ")
      val winnersOfDay = MatchDay.winners(matchDay)
      assert(winnersOfDay == Set(Team.Koeln, Team.Leverkusen))
    }

    scenario("Number of redcards in sections") {
      Given("Two matchdays1")
      val matchDay = MatchDay.MatchDay1
      val matchDay2 = MatchDay.MatchDay2

      Then("Check number of redcards in section")
      val redcardsInSections = MatchDay.redCardsPerSection(matchDay :: matchDay2 :: Nil)
      assert(redcardsInSections == (2, 1))
    }

    scenario("Plays on all Matchdays") {
      Given("Two matchdays")
      val matchDay = MatchDay.MatchDay1
      val matchDay2 = MatchDay.MatchDay2

      Then("Some are present, some not..")
      val playsOnAllMatchDays = MatchDay.playsOnAllMatchdays(matchDay :: matchDay2 :: Nil)
      assert(playsOnAllMatchDays.contains(Team.Bayern))
      assert(playsOnAllMatchDays.contains(Team.Dortmund))
      assert(playsOnAllMatchDays.contains(Team.Koeln))
      assert(playsOnAllMatchDays.contains(Team.Leverkusen))
      assert(playsOnAllMatchDays.contains(Team.Schalke))
      assert(!playsOnAllMatchDays.contains(Team.Hamburg))
      assert(!playsOnAllMatchDays.contains(Team.Kaiserslautern))
    }

    scenario("Mapped by Participant") {
      Given("Two matchdays")
      val matchDay = MatchDay.MatchDay1
      val matchDay2 = MatchDay.MatchDay2

      val mappedByParticipant: Map[Team, Set[MatchDay]] = MatchDay.mappedByParticipant(matchDay :: matchDay2 :: Nil)

      Then("mappedByParticipant")
      assert(mappedByParticipant.size == 7)
      assert(mappedByParticipant(Team.Bayern) == TreeSet(matchDay, matchDay2))
      assert(mappedByParticipant(Team.Dortmund) == TreeSet(matchDay, matchDay2))
      assert(mappedByParticipant(Team.Koeln) == TreeSet(matchDay, matchDay2))
      assert(mappedByParticipant(Team.Leverkusen) == TreeSet(matchDay, matchDay2))
      assert(mappedByParticipant(Team.Schalke) == TreeSet(matchDay, matchDay2))
      assert(mappedByParticipant(Team.Hamburg) == TreeSet(matchDay2))
      assert(mappedByParticipant(Team.Kaiserslautern) == TreeSet(matchDay))
    }
  }

}
