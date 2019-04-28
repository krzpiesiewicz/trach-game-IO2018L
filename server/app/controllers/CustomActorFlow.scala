package controllers

import akka.actor._
import akka.stream.scaladsl.{Keep, Sink, Source, Flow}
import akka.stream.{Materializer, OverflowStrategy}

/**
 * from: https://stackoverflow.com/questions/37281559/play-scala-akka-websockets-change-actor-path
 */
object CustomActorFlow {

  def actorRef[In, Out](props: ActorRef => Props, bufferSize: Int = 16, overflowStrategy: OverflowStrategy = OverflowStrategy.dropNew, name: String)(implicit factory: ActorRefFactory, mat: Materializer): Flow[In, Out, _] = {

    val (outActor, publisher) = Source.actorRef[Out](bufferSize, overflowStrategy)
                        .toMat(Sink.asPublisher(false))(Keep.both).run()

    def flowActorProps: Props = {
      Props(new Actor {
        val flowActor = context.watch(context.actorOf(props(outActor), s"$name"))

        def receive = {
          case Status.Success(_) | Status.Failure(_) => flowActor ! PoisonPill
          case Terminated(_) => context.stop(self)
          case other => flowActor ! other
        }

        override def supervisorStrategy = OneForOneStrategy() { case _ => SupervisorStrategy.Stop }
      })
    }

    def actorRefForSink = factory.actorOf(flowActorProps)

    Flow.fromSinkAndSource(Sink.actorRef(actorRefForSink, Status.Success(())), Source.fromPublisher(publisher))

  }
}