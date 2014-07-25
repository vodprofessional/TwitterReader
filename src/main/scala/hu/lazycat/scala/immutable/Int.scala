package hu.lazycat.scala.immutable

/**
 * Case class for parsing Int and using it in matches
 */
object Int {
  def unapply(s : String) : Option[Int] = try {
    Some(s.toInt)
  } catch {
    case _ : java.lang.NumberFormatException => None
  }
}
