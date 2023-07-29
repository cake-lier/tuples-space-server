/*
 * Copyright (c) 2023 Matteo Castellucci
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.cakelier
package tuples.space.server

import io.circe.syntax.*
import io.circe.parser.*
import akka.NotUsed
import akka.actor.ActorSystem as ClassicActorSystem
import akka.actor.typed.{ActorRef, ActorSystem, DispatcherSelector}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import tuples.space.*
import tuples.space.server.response.*
import tuples.space.server.response.Serializers.given
import tuples.space.server.request.*
import tuples.space.server.request.Serializers.given

import java.util.UUID
import scala.concurrent.{ExecutionContextExecutor, Future}

@SuppressWarnings(Array("org.wartremover.warts.Null", "org.wartremover.warts.Var"))
object TupleSpaceRoute {

  def apply(servicePath: String, tupleSpaceActor: ActorRef[TupleSpaceActorCommand], actorSystem: ActorSystem[Nothing]): Route =
    given ClassicActorSystem = actorSystem.classicSystem
    given ExecutionContextExecutor = actorSystem.dispatchers.lookup(DispatcherSelector.default())

    path(servicePath) {
      handleWebSocketMessages {
        var id: UUID = UUID.randomUUID()

        Flow[Message]
          .mapAsync(1) {
            case m: TextMessage.Strict => Future.successful(m)
            case m: TextMessage.Streamed => m.textStream.runFold("")(_ + _).map(TextMessage.apply)
            case m: BinaryMessage => m.dataStream.runWith(Sink.ignore).map(_ => null)
          }
          .flatMapConcat(t =>
            (for {
              j <- parse(t.text)
              m <- j.as[Request]
              r = m match {
                case r: MergeRequest =>
                  id = r.oldClientId
                  TupleSpaceActorCommand.MergeIds(r.clientId, r.oldClientId)
                case r: TupleRequest => TupleSpaceActorCommand.Out(id, r.content)
                case r: TemplateRequest =>
                  r.tpe match {
                    case TemplateRequestType.In => TupleSpaceActorCommand.In(id, r.content)
                    case TemplateRequestType.Rd => TupleSpaceActorCommand.Rd(id, r.content)
                    case TemplateRequestType.No => TupleSpaceActorCommand.No(id, r.content)
                    case TemplateRequestType.InAll => TupleSpaceActorCommand.InAll(id, r.content)
                    case TemplateRequestType.RdAll => TupleSpaceActorCommand.RdAll(id, r.content)
                    case TemplateRequestType.Inp => TupleSpaceActorCommand.Inp(id, r.content)
                    case TemplateRequestType.Rdp => TupleSpaceActorCommand.Rdp(id, r.content)
                    case TemplateRequestType.Nop => TupleSpaceActorCommand.Nop(id, r.content)
                  }
                case r: SeqTupleRequest => TupleSpaceActorCommand.OutAll(id, r.content)
              }
            } yield r).map(Source.single[TupleSpaceActorCommand]).getOrElse(Source.empty[TupleSpaceActorCommand])
          )
          .via(
            Flow.fromSinkAndSourceCoupled(
              ActorSink.actorRef[TupleSpaceActorCommand](
                tupleSpaceActor,
                TupleSpaceActorCommand.Exit(id, success = true),
                _ => TupleSpaceActorCommand.Exit(id, success = false)
              ),
              ActorSource
                .actorRef[Response](PartialFunction.empty, PartialFunction.empty, 1, OverflowStrategy.dropHead)
                .mapMaterializedValue(a => tupleSpaceActor ! TupleSpaceActorCommand.Enter(id, a))
            )
          )
          .map(r => TextMessage(r.asJson.noSpaces))
      }
    }
}
