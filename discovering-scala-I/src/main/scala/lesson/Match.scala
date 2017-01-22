package lesson

object Match {
  // 10
  val match1 = Match(Team.Bayern, Team.Dortmund, 2, 2)
  val match2 = Match(Team.Kaiserslautern, Team.Koeln, 1, 2, Some(RedCard(50)))
  val match3 = Match(Team.Leverkusen, Team.Schalke, 3, 0, Some(RedCard(10)))

  // 7
  val match4 = Match(Team.Bayern, Team.Schalke, 2, 0)
  val match5 = Match(Team.Hamburg, Team.Leverkusen, 0, 2)
  val match6 = Match(Team.Koeln, Team.Dortmund, 0, 3, Some(RedCard(44)))
}

case class Match(home: Team, away: Team, goalsHome: Int, goalsAway: Int, redCard: Option[RedCard] = None)