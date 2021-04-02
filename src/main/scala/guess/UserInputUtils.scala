package guess

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object UserInputUtils {

  def readNumericResponse:Future[Option[Int]] = {
    readResponse.map { s =>
      try {
        Some(s.toInt)
      } catch {
        case _: Throwable => None
      }
    }
  }

  def readBooleanResponse: Future[Boolean] = {
    readResponse.map {
      case "y" | "yes" | "1" | "" => true
      case _ => false
    }
  }

  // Read a line from the standard input asynchronously.
  private def readResponse: Future[String] = Future {
    scala.io.StdIn.readLine()
  }
}
