import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.Option;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

import jvmapi.models.*;
import jvmapi.messages.*;

import bot.BotActor;

public class BotActorTest {
	static ActorSystem system;

	@BeforeClass
	public static void setup() {
		system = ActorSystem.create();
	}

	@AfterClass
	public static void teardown() {
		TestKit.shutdownActorSystem(system);
		system = null;
	}

	@Test
	public void exampleTest() {
		final TestKit probe = new TestKit(system);

		final long gamePlayId = 42;
		
		final ActorRef bot = system.actorOf(BotActor.props(gamePlayId, 9));

		var p1 = new Player(1, "player A", 5, JavaConverters
				.collectionAsScalaIterable(Arrays.asList(new Card(1, "attack"), new Card(2, "priority_inc"))).toSeq(),
				JavaConverters.collectionAsScalaIterable(new ArrayList<Card>()).toSeq());

		var p2 = new Player(1, "player A", 5,
				JavaConverters.collectionAsScalaIterable(Arrays.asList(new Card(3, "defence"))).toSeq(),
				JavaConverters.collectionAsScalaIterable(new ArrayList<Card>()).toSeq());

		var state = new GameState(JavaConverters.collectionAsScalaIterable(Arrays.asList(p1, p2)).toSeq(),
				JavaConverters.collectionAsScalaIterable(new ArrayList<Card>()).toSeq(),
				JavaConverters.collectionAsScalaIterable(new ArrayList<Card>()).toSeq(),
				JavaConverters.collectionAsScalaIterable(new ArrayList<Card>()).toSeq(), Option.apply(null));

		var msg = new GameStateUpdateMsg("GameStateUpdate", gamePlayId, 1, state);

		bot.tell(msg, probe.getRef());
		
		var reply = probe.expectMsgClass(PlayedCardsRequestMsg.class);
	}
}
