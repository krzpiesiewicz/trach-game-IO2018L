package bot

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import akka.actor._
import akka.event.{Logging, DiagnosticLoggingAdapter}

import jvmapi._
import jvmapi.models._
import jvmapi.messages._
import scala.util.Random

import BotActor._

class BotActor(server: ActorRef, gamePlayId: Long, botPlayerId: Int, delay: FiniteDuration)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

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

      if (cardTrees.isEmpty) {
        // if the card tree is empty, then it is a beginning of a round.
        if (playerIdOnMove == botPlayerId)
          botsTurn
      } else
        checkCardTree(cardTrees.head)
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

      case None => // if there is no attack card, then exchange 3 cards from hand.
        sendHandExchange(botPlayer.hand.take(3))
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
      case _ => sendNoAction
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
      id = -1,
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

    CardNode(
      PlayedCardInTree(
        card = defenceCard,
        whoPlayedId = botPlayerId,
        parentCardId = attackCardId),
      childrenNodes = children)
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

  private def sendHandExchange(cardsToExchange: Seq[Card])(implicit msg: GameStateUpdateMsg) {
    sendMsgToServer(HandExchangeRequestMsg(
      gamePlayId = gamePlayId,
      updateId = msg.updateId,
      playerId = botPlayerId,
      cardsIdsToExchange = cardsToExchange.map(_.id)))
  }

  private def sendMsgToServer(msg: Any) {
    val msgToSend = MsgToSend(MsgFromPlayerDriver(BotDriver(self), msg))
    // send message with the @delay (for better users' impressions).
    if (delay > 0.seconds)
      context.system.scheduler.scheduleOnce(delay, self, msgToSend)
    else
      self ! msgToSend
  }
}

object BotActor {
  def props(server: ActorRef, gamePlayId: Long, botPlayerId: Int, delay: FiniteDuration)(implicit ec: ExecutionContext) =
    Props(new BotActor(server, gamePlayId, botPlayerId, delay))

  case class MsgToSend(msg: Any)
}