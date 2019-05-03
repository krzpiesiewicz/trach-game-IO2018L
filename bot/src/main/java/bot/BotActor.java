package bot;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.Option;

import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.DiagnosticLoggingAdapter;
import akka.event.Logging;

import jvmapi.*;
import jvmapi.models.*;
import jvmapi.messages.*;

public class BotActor extends AbstractActor {

    private final DiagnosticLoggingAdapter log = Logging.getLogger(this);
    
	final ActorRef out;
    final long gamePlayId;
    final int playerId;
    
    public BotActor(ActorRef out, long gamePlayId, int playerId) {
        this.out = out;
		this.gamePlayId = gamePlayId;
        this.playerId = playerId;
    }

    public static Props props(ActorRef out, long gamePlayId, int playerId) {
        return Props.create(BotActor.class, () -> new BotActor(out, gamePlayId, playerId));
    }

    @Override
    public void preStart() {
    	Map<String, Object> mdc;
        mdc = new HashMap<String, Object>();
        mdc.put("actorSufix", "[gamePlayId=" + gamePlayId + ", playerId=" + playerId + "]");
        log.setMDC(mdc);
        log.info("started");
		//TODO all things to do before starting the actor
    }

    @Override
    public void postStop() {
        log.info("stopped");
        //TODO all things to do before stopping the actor
    }

    /**
     * TODO create a sensible receive object.
     * @return
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(
        		GameStateUpdateMsg.class,
        		msg -> {
       				var noActionMsg = new MsgFromPlayerDriver(new BotDriver(getSelf()), new NoActionRequestMsg(
        					"NoActionRequest",
        					gamePlayId,
        					msg.updateId(),
        					playerId));
					
					var botsTurn = (msg.gameState().playerIdOnMove() == playerId);
					List<Player> players = scala.collection.JavaConverters.seqAsJavaList(msg.gameState().players());
					Card attack = null;
					Card defence = null;
					Card priority_inc = null;
					for (Player p : players) {
						if (p.id() == playerId) {
							List<Card> hand = scala.collection.JavaConverters.seqAsJavaList(p.hand());
							for (Card c : hand) {
								switch (c.type()) {
									case "attack": attack = c; break;
									case "defence": defence = c; break;
									case "priority_inc": priority_inc = c; break;
								}
							}
							break;
						}
					}
					
					//CardTree ct = msg.gameState().cardTree().getOrElse(null);
					
					
					if (botsTurn) {
						if (attack != null) {
							var attackMsg = new MsgFromPlayerDriver(new BotDriver(getSelf()), new PlayedCardsRequestMsg(
								"PlayedCardRequest",
								gamePlayId,
								msg.updateId(),
								playerId,
								new CardTree(new PlayedStartingCardAtPlayer(
									"PlayedStartingCardAtPlayer",
        							attack,
        							playerId,
        							3-playerId),
        						JavaConverters.collectionAsScalaIterable(new ArrayList<CardNode>()).toSeq()
        						)));
							out.tell(attackMsg, getSelf());
							if (priority_inc != null) {
								var priority_incMsg = new MsgFromPlayerDriver(new BotDriver(getSelf()), new PlayedCardsRequestMsg(
									"PlayedCardRequest",
									gamePlayId,
									msg.updateId(),
									playerId,
									new CardNode(new PlayedCardInTree(
										"PlayedCardInTree",
										priority_inc,
										playerId,
										attack.id()),
									JavaConverters.collectionAsScalaIterable(new ArrayList<CardNode>()).toSeq()
									)));
								out.tell(priority_incMsg, getSelf());
							}
						} else {
							out.tell(noActionMsg, getSelf());
						}	
					} else if (!msg.gameState().cardTree().isEmpty()) {
						//CardTreeOrNode ct = msg.gameState().cardTree().getOrElse(null);
						Option<CardTree> cto = msg.gameState().cardTree();
						CardTreeOrNode ct = cto.getOrElse(null);
						var attackingCard = ct.playedCard().card();
						var defenceMsg = new MsgFromPlayerDriver(new BotDriver(getSelf()), new PlayedCardsRequestMsg(
							"PlayedCardRequest",
							gamePlayId,
							msg.updateId(),
							playerId,
							new CardNode(new PlayedCardInTree(
								"PlayedCardInTree",
								defence,
								playerId,
								attackingCard.id()
								),
							JavaConverters.collectionAsScalaIterable(new ArrayList<CardNode>()).toSeq()
							)));
						out.tell(defenceMsg, getSelf());
					} else {
						out.tell(noActionMsg, getSelf());
					}
				}
        ).build();
    }
}
