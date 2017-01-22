package lesson

object Team {
  val Bayern = Team("Bayern")
  val Dortmund = Team("Dortmund")
  val Hamburg = Team("Hamburg")
  val Kaiserslautern = Team("Kaiserslautern")
  val Koeln = Team("Köln")
  val Leverkusen = Team("Leverkusen")
  val Schalke = Team("Schalke")
}

case class Team(name: String) extends Comparable[Team] {
  override def compareTo(o: Team): Int = name.compareTo(o.name)
}