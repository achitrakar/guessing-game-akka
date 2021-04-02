package guess

import akka.actor.{Actor, FSM, Props}
import guess.Game.TryAgain

/**
 * Companion object to the `Game` class.
 */
object Game {
  case object Ready     // Message to send when the game is ready to be played.

  sealed trait Result
  case object Win       extends Result // Messsage to send to a player when the guess is correct
  case object TryAgain  extends Result // Message to send to a player actor when the guess is incorrect

  def props(): Props = Props(classOf[Game])
}

class Game extends Actor {
  var number = generate()

  // Initialize the player actor.
  val player = context.actorOf(Player.props(self))

  // The range of number is [1, 100].
  def generate(): Int = {
    val random = scala.util.Random
    val secretNumber = random.nextInt(100) + 1 // .nextInt(N) provides random number from 0 to N-1.
    println("***SECRET NUMBER***")
    println(s"********$secretNumber*********")
    println("*******************")
    secretNumber
  }

  override def preStart(): Unit = player ! Game.Ready

  override def receive: Receive = {
    case Player.Guess(n: Int) => {
      if (n == number) {
        player ! Game.Win
      } else {
        player ! Game.TryAgain
      }
    }

    case Player.Restart =>
      number = generate()
      player ! Game.Ready

    case Player.Leave => context.system.terminate()
  }
}
