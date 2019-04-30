package bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import scala.collection.JavaConverters;
import scala.collection.Seq;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.DiagnosticLoggingAdapter;
import akka.event.Logging;

import jvmapi.*;
import jvmapi.models.*;
import jvmapi.messages.*;

public class BotActor extends AbstractActor {

    private final DiagnosticLoggingAdapter log = Logging.getLogger(this);
    
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
//        			log.info(msg.toString());
        			
        			var myMsg = new MsgFromPlayerDriver(new BotDriver(getSelf()), new PlayedCardsRequestMsg(
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
        							)));
        			
        			getSender().tell(myMsg, getSelf());
        		}
        ).build();
    }
}
