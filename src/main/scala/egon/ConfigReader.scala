package egon

case class Configuration(pageMarginTop:Float, pageMarginBottom:Float, pageMarginLeft:Float, pageMarginRight:Float)

abstract class ConfigReader[A] {
  def apply(config: Configuration): A

  def map[B](f: A => B): ConfigReader[B] =
    new ConfigReader[B] {
      def apply(c: Configuration) =
        f(ConfigReader.this.apply(c))
    }

  def flatMap[B](f: A => ConfigReader[B]): ConfigReader[B] =
    new ConfigReader[B] {
      def apply(c: Configuration) =
        f(ConfigReader.this.apply(c))(c)
    }
}

/*
object ConfigReader {
  def lift3ConfigReader[A, B, C, D](f: A => B => C => D):
    ConfigReader[A] =>
    ConfigReader[B] =>
    ConfigReader[C] =>
    ConfigReader[D] =
      a => b => c =>
        for{
          aa <- a
          bb <- b
          cc <- c
        } yield f(aa)(bb)(cc)
}
*/

object Main {
  //import ConfigReader._
  def main(args: Array[String]) {
    // utility construction
    def configReader[A](k: Configuration => A): ConfigReader[A] =
      new ConfigReader[A] {
        def apply(c: Configuration) = k(c)
      }

    val pageMarginBottom = configReader(_.pageMarginBottom)
    val pageMarginTop = configReader(_.pageMarginTop)

    val r =
      for {
        b <- pageMarginBottom
        t <- pageMarginTop
      } yield {
        var str = ""
        for(i <- 1 to 10) {
          str += "."
        }
        str + "Page margin top: " + t + " bottom: " + b
      }

    val conf = Configuration(pageMarginBottom = 4.5f, pageMarginTop = 4.5f, pageMarginLeft = 1, pageMarginRight = 1)

    println(r)
    println(r(conf))
  }
}