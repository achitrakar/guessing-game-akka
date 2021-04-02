package guess

import akka.actor.ActorSystem

object AppMain extends App {
  val system = ActorSystem("GuessingGame")
  val game = system.actorOf(Game.props())
}
