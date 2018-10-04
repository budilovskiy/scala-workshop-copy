package lesson

case class MatchDay(day: Int, matches: List[Match]) extends Comparable[MatchDay] {

  val teams: List[Team] = (matches.map(_.home) ++ matches.map(_.away)).distinct

  def numberOfMatches: Int = matches.size

  def numberOfTeams: Int = teams.size

  def numberOfGoals: Int = (matches.map(_.goalsAway) ++ matches.map(_.goalsHome)).sum

  def addMatches(matchesToAdd: Match*): MatchDay = new MatchDay(day, matches ++ matchesToAdd)

  def teamsSortedByName: List[Team] = teams.sortBy(_.name)

  def compareTo(o: MatchDay): Int = Integer.compare(day, o.day)

}

object MatchDay {

  val MatchDay1: MatchDay = MatchDay(1, List[Match](Match.match1, Match.match2, Match.match3))
  val MatchDay2: MatchDay = MatchDay(2, List[Match](Match.match4, Match.match5, Match.match6))

  def apply(day: Int): MatchDay = MatchDay(day, List.empty)

  /** Returns a set of matches with more than 3 goals total */
  def excitingMatches(matchDays: List[MatchDay]): List[Match] =
    matchDays.flatMap(_.matches.filter(m => m.goalsHome + m.goalsAway > 3))

  def excitingMatches1(matchDays: List[MatchDay]): List[Match] =
    matchDays.flatMap(_.matches).filter(_match => _match.goalsHome + _match.goalsAway > 3)

  def excitingMatches2(matchDays: List[MatchDay]): List[Match] =
    for {
      day <- matchDays
      m <- day.matches
      if m.goalsHome + m.goalsAway > 3
    } yield {
      m
    }

  /** Returns a set of all winning teams of the given match day */
  def winners(matchDay: MatchDay): Set[Team] =
    matchDay.matches
      .filter(m => m.goalsAway != m.goalsHome)
      .map(m => if (m.goalsAway > m.goalsHome) m.away else m.home)
      .toSet

  /**
    * Returns the number of red cards per section, assuming a football halftime has a fixed duration of 45 minutes
    */
  def redCardsPerSection(matchdays: List[MatchDay]): (Int, Int) =
    matchdays.flatMap(_.matches).foldRight((0, 0)) { (m, acc) =>
      m.redCard match {
        case Some(card) if card.minute < 45 => (acc._1 + 1, acc._2)
        case Some(card) if card.minute >= 45 => (acc._1, acc._2 + 1)
        case None => acc
      }
    }

  /**
    * Return a set of those teams which play on all given match days
    */
  def playsOnAllMatchdays(matchdays: List[MatchDay]): Set[Team] = {
    val allTeams = matchdays.flatMap(_.teams)
    allTeams.filter(team => allTeams.count(_ == team) == matchdays.size).toSet
  }

  /**
    * Returns a map,
    */
  def mappedByParticipant(matchdays: List[MatchDay]): Map[Team, Set[MatchDay]] =
    matchdays
      .flatMap(_.teams)
      .distinct
      .map(team =>
        team -> matchdays.filter(day => day.matches.map(_.away).contains(team) || day.matches.map(_.home).contains(team)).toSet
      )
      .toMap

  def mappedByParticipant1(matchdays: List[MatchDay]): Map[Team, Set[MatchDay]] = {
    val allTeams = matchdays.flatMap(_.teams)

    allTeams.map { team =>
      val teamDays = matchdays.filter { day =>
        (day.matches.map(_.away) ++ day.matches.map(_.home)).contains(team)
      }.toSet
      (team, teamDays)
    }.toMap
  }

}
