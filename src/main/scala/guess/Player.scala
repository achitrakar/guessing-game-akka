package guess

import akka.actor.{ActorRef, FSM, Props}
import guess.Game.Ready

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Player {
  sealed trait State
  case object Initializing extends State
  case object Idle extends State
  case object WaitingForRoundResult extends State
  case object Terminating extends State

  sealed trait Data
  case object EmptyData extends Data

  case class Guess(int: Int)
  case object Restart
  case object Leave

  def props(game: ActorRef): Props = Props(classOf[Player], game)
}

class Player(game: ActorRef) extends FSM[Player.State, Player.Data] {
  import Player._

  startWith(Initializing, EmptyData)

  when(Initializing) {
    case Event(Game.Ready, _) =>
      println("Pick a number between 1 and 100 (inclusive)")
      UserInputUtils.readNumericResponse.map {
        case Some(n) => self ! n
        case None => self ! Leave
      }
      goto(Idle)
  }

  // handle User Input in this FSM state.
  when(Idle) {
    case Event(number: Int, _) =>
      game ! Player.Guess(number)
      goto(WaitingForRoundResult)

    case Event(msg @ Leave, _) =>
      self forward msg
      goto(Terminating)

    case Event(restart: Boolean, _) =>
      restart match {
        case true =>
          game ! Player.Restart
          goto(Initializing)
        case false =>
          println("Goodbye!")
          game ! Player.Leave
          goto(Terminating)
      }
  }

  when(WaitingForRoundResult) {
    case Event(Game.Win, _) =>
      println("YOU WIN!!! \nPlay another round? (y/n)")
      UserInputUtils.readBooleanResponse.map(self ! _)
      goto(Idle)

    case Event(Game.TryAgain, _) =>
      println("WRONG GUESS. \nTry again? (y/n)")
      UserInputUtils.readBooleanResponse.map(self ! _)
      goto(Idle)
  }

  when(Terminating) {
    case Event(Leave, _) =>
      context.stop(self)
      stay()
  }

  whenUnhandled {
    case msg =>
      println(s"Unhandled message (${msg}) received.")
      stay()
  }
}
