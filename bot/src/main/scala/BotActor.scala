package bot

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import akka.actor._
import akka.event.{ Logging, DiagnosticLoggingAdapter }

import jvmapi._
import jvmapi.models._
import jvmapi.messages._
import scala.util.Random

import BotActor._

class BotActor(server: ActorRef, gamePlayId: Long, botPlayerId: Int)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override val log: DiagnosticLoggingAdapter = Logging(this)

  override def preStart() = {
    log.mdc(Map("actorSufix" -> s"[gamePlayId=$gamePlayId, playerId=$botPlayerId]"))
    log.info("started")
    //TODO all things to do before starting the actor
  }

  override def postStop() = {
    log.info("stopped");
    //TODO all things to do before stopping the actor
  }

  def receive: Receive = defaultReceive orElse playing

  private def defaultReceive: Receive = {
    case MsgToSend(msg) =>
      server ! msg
  }

  private def playing: Receive = {
    case msg: GameStateUpdateMsg => if (msg.gamePlayId == gamePlayId) {
      implicit val gameStateUpdate = msg
      import msg.gameState._

      implicit val botPlayer = players.find(_.id == botPlayerId) match {
        case Some(player) => player
        case None =>
          val message = s"My player (of id=$botPlayerId) is missing in gameState from msg"
          log.info(message)
          throw new Exception(message)
      }

      cardTree match {
        case None => // if the card tree is empty, then it is a beginning of a round.
          if (playerIdOnMove == botPlayerId)
            botsTurn
        case Some(tree) =>
          checkCardTree(tree)
      }
    }
  }

  private def botsTurn(implicit botPlayer: Player, msg: GameStateUpdateMsg) {
    import msg.gameState._

    /* The bot tries to attack another, random player (uses priority_inc if possible) */
    botPlayer.hand.find(_.`type` == "attack") match {
      case Some(attackCard) =>
        anotherRandomPlayer(players) match {
          case None => {}
          case Some(targetPlayer) =>
            val priorityIncCardOpt = botPlayer.hand.find(_.`type` == "priority_inc")
            sendPlayedCards(attackTree(attackCard, targetPlayer, priorityIncCardOpt))
        }
      case None => {}
    }
  }

  private def checkCardTree(tree: CardTree)(implicit botPlayer: Player, msg: GameStateUpdateMsg) {
    import msg.gameState._

    /* The bot checks if it is a target of an attack. If so, it tries to defend itself. */
    tree.playedCard match {
      case PlayedStartingCardAtPlayer(_, Card(attackId, "attack"), _, `botPlayerId`) =>

        /* check if bot has defence card */
        botPlayer.hand.find(_.`type` == "defence") match {
          case None => sendNoAction
          case Some(defenceCard) =>
            /* check the number of priority_incs of the attack */
            val cnt = tree.childrenNodes.count(_.playedCard.card.`type` == "priority_inc")
            /* get priority_incs from hand and take @cnt of them*/
            val priorityIncCards = botPlayer.hand.filter(_.`type` == "priority_inc").take(cnt)
            /* send defence */
            sendPlayedCards(defenceCardNode(defenceCard, priorityIncCards, attackId))
            /* send no action (in case of the defence failure) */
            sendNoAction
        }
    }
  }

  private def anotherRandomPlayer(players: Seq[Player]): Option[Player] = {
    players.filterNot(_.id == botPlayerId) match {
      case Seq() => None
      case seq => Some(seq(new Random().nextInt(seq.length)))
    }
  }

  private def attackTree(attackCard: Card, targetPlayer: Player, priorityIncCardOpt: Option[Card]): CardTree = {
    val children = priorityIncCardOpt match {
      case None => Seq.empty
      case Some(priorityIncCard) => Seq(CardNode(PlayedCardInTree(
        card = priorityIncCard,
        whoPlayedId = botPlayerId,
        parentCardId = attackCard.id)))
    }
    CardTree(
      playedCard = PlayedStartingCardAtPlayer(
        card = attackCard,
        whoPlayedId = botPlayerId,
        targetPlayerId = targetPlayer.id),
      childrenNodes = children)
  }

  private def defenceCardNode(defenceCard: Card, priorityIncCards: Seq[Card], attackCardId: Int): CardNode = {
    val children = priorityIncCards map {
      case pic =>
        CardNode(PlayedCardInTree(card = pic, whoPlayedId = botPlayerId, parentCardId = defenceCard.id))
    }

    CardNode(PlayedCardInTree(card = defenceCard, whoPlayedId = botPlayerId, parentCardId = attackCardId))
  }

  private def sendPlayedCards(tree: CardTreeOrNode)(implicit msg: GameStateUpdateMsg) {
    sendMsgToServer(PlayedCardsRequestMsg(
      gamePlayId = gamePlayId,
      updateId = msg.updateId,
      playerId = botPlayerId,
      played = tree))
  }

  private def sendNoAction(implicit msg: GameStateUpdateMsg) {
    sendMsgToServer(NoActionRequestMsg(
      gamePlayId = gamePlayId,
      updateId = msg.updateId,
      playerId = botPlayerId))
  }

  private def sendMsgToServer(msg: Any) {
    // send message with 1.5s delay (for better users' impressions).
    context.system.scheduler.scheduleOnce(1500.milliseconds, self, MsgToSend(MsgFromPlayerDriver(BotDriver(self), msg)))
  }
}

object BotActor {
  def props(server: ActorRef, gamePlayId: Long, botPlayerId: Int)(implicit ec: ExecutionContext) = Props(new BotActor(server, gamePlayId, botPlayerId))

  case class MsgToSend(msg: Any)
}