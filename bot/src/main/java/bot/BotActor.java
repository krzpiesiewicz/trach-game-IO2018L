package bot;

import java.util.ArrayList;

import scala.collection.JavaConverters;
import scala.collection.Seq;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import jvmapi.models.*;
import scala.collection.JavaConverters;
import jvmapi.messages.*;

public class BotActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    
    final long gamePlayId;
    final int playerId;
    
    public BotActor(long gamePlayId, int playerId) {
        this.gamePlayId = gamePlayId;
        this.playerId = playerId;
    }

    public static Props props(long gamePlayId, int playerId) {
        return Props.create(BotActor.class, () -> new BotActor(gamePlayId, playerId));
    }

    @Override
    public void preStart() {
        log.info("BotActor(gamePlayId={}, playerId={}) started", gamePlayId, playerId);
        //TODO all things to do before starting the actor
    }

    @Override
    public void postStop() {
        log.info("BotActor(gamePlayId={}, playerId={}) stopped", gamePlayId, playerId);
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
//        			log.info(msg.toString());
        			
        			var myMsg = new PlayedCardsRequestMsg(
        					"PlayedCardRequest",
        					gamePlayId,
        					msg.updateId(),
        					playerId,
        					new CardTree(new PlayedStartingCardAtPlayer(
        							"PlayedStartingCardAtPlayer",
        							new Card(1, "attack"),
        							playerId,
        							2),
        							JavaConverters.collectionAsScalaIterable(new ArrayList<CardNode>()).toSeq()
        							));
        			
        			getSender().tell(myMsg, getSelf());
        		}
        ).build();
    }
}
