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

import AnyOps.*
import tuples.space.*
import tuples.space.server.request.*
import tuples.space.server.response.*

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import java.util.UUID
import scala.annotation.tailrec

@SuppressWarnings(Array("org.wartremover.warts.Recursion"))
object TupleSpaceActor {

  private enum PendingRequestType {

    case In extends PendingRequestType

    case Rd extends PendingRequestType

    case No extends PendingRequestType
  }

  private type Request = (UUID, JsonTemplate, PendingRequestType)

  private def completeNoRequests(
                                  connections: Map[UUID, ActorRef[Response]],
                                  pendingTuples: Seq[JsonTuple],
                                  pendingRequests: Seq[Request]
  )(
    tuplesToRemove: Seq[JsonTuple]
  ): (Seq[JsonTuple], Seq[Request]) = {
    val newPendingTuples = pendingTuples.diff(tuplesToRemove)
    val newPendingRequests =
      pendingRequests
        .flatMap((id, template, tpe) =>
          if (tpe === PendingRequestType.No && newPendingTuples.forall(t => !template.matches(t))) {
            connections.get(id).foreach(_ ! TemplateResponse(template))
            None
          } else {
            Some((id, template, tpe))
          }
        )
    (newPendingTuples, newPendingRequests)
  }

  private def completeInRdRequests(
    connections: Map[UUID, ActorRef[Response]],
    pendingTuples: Seq[JsonTuple],
    pendingRequests: Seq[Request]
  )(
    tupleToAdd: JsonTuple
  ): (Seq[JsonTuple], Seq[Request]) = {
    @tailrec
    def _completeInRdRequests(remainingRequests: Seq[Request]): (Seq[JsonTuple], Seq[Request]) = remainingRequests match {
      case (id, template, tpe) +: t =>
        tpe match {
          case PendingRequestType.In =>
            connections.get(id).foreach(_ ! TemplateTupleResponse(template, TemplateTupleResponseType.In, tupleToAdd))
            (pendingTuples, t)
          case _ =>
            connections.get(id).foreach(_ ! TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tupleToAdd))
            _completeInRdRequests(t)
        }
      case e => (pendingTuples :+ tupleToAdd, e)
    }

    _completeInRdRequests(pendingRequests.filter((_, _, tpe) => tpe !== PendingRequestType.No))
  }

  private def main(
    connections: Map[UUID, ActorRef[Response]],
    pendingTuples: Seq[JsonTuple],
    pendingRequests: Seq[Request]
  ): Behavior[TupleSpaceActorCommand] = Behaviors.receiveMessage {
    case TupleSpaceActorCommand.Out(id, tuple) =>
      connections
        .get(id)
        .map(a => {
          a ! TupleResponse(tuple)
          val (newPendingTuples, newPendingRequests) = completeInRdRequests(connections, pendingTuples, pendingRequests)(tuple)
          main(connections, newPendingTuples, newPendingRequests)
        })
        .getOrElse(Behaviors.same)
    case TupleSpaceActorCommand.In(id, template) =>
      connections
        .get(id)
        .map(a =>
          pendingTuples.find(template.matches) match {
            case Some(tuple) =>
              a ! TemplateTupleResponse(template, TemplateTupleResponseType.In, tuple)
              val (newPendingTuples, newPendingRequests) =
                completeNoRequests(connections, pendingTuples, pendingRequests)(Seq(tuple))
              main(connections, newPendingTuples, newPendingRequests)
            case None => main(connections, pendingTuples, pendingRequests :+ (id, template, PendingRequestType.In))
          }
        )
        .getOrElse(Behaviors.same)
    case TupleSpaceActorCommand.Rd(id, template) =>
      connections
        .get(id)
        .map(a =>
          pendingTuples.find(template.matches) match {
            case Some(tuple) =>
              a ! TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple)
              Behaviors.same
            case None => main(connections, pendingTuples, pendingRequests :+ (id, template, PendingRequestType.Rd))
          }
        )
        .getOrElse(Behaviors.same)
    case TupleSpaceActorCommand.No(id, template) =>
      connections
        .get(id)
        .map(a =>
          if (pendingTuples.forall(t => !template.matches(t))) {
            a ! TemplateResponse(template)
            Behaviors.same
          } else {
            main(connections, pendingTuples, pendingRequests :+ (id, template, PendingRequestType.No))
          }
        )
        .getOrElse(Behaviors.same)
    case TupleSpaceActorCommand.Inp(id, template) =>
      connections
        .get(id)
        .map(a =>
          pendingTuples.find(template.matches) match {
            case t @ Some(tuple) =>
              a ! TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Inp, t)
              val (newPendingTuples, newPendingRequests) =
                completeNoRequests(connections, pendingTuples, pendingRequests)(Seq(tuple))
              main(connections, newPendingTuples, newPendingRequests)
            case _ =>
              a ! TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Inp, None)
              Behaviors.same
          }
        )
        .getOrElse(Behaviors.same)
    case TupleSpaceActorCommand.Rdp(id, template) =>
      connections
        .get(id)
        .foreach(
          _ ! TemplateMaybeTupleResponse(
            template,
            TemplateMaybeTupleResponseType.Rdp,
            pendingTuples.find(template.matches)
          )
        )
      Behaviors.same
    case TupleSpaceActorCommand.Nop(id, template) =>
      connections.get(id).foreach(_ ! TemplateBooleanResponse(template, pendingTuples.forall(t => !template.matches(t))))
      Behaviors.same
    case TupleSpaceActorCommand.OutAll(id, tuples) =>
      connections
        .get(id)
        .map(a => {
          a ! SeqTupleResponse(tuples)
          val (newPendingTuples, newPendingRequests) =
            tuples.foldLeft((pendingTuples, pendingRequests))((a, t) => completeInRdRequests(connections, a._1, a._2)(t))
          main(connections, newPendingTuples, newPendingRequests)
        })
        .getOrElse(Behaviors.same)
    case TupleSpaceActorCommand.InAll(id, template) =>
      connections
        .get(id)
        .map(a => {
          val tuples = pendingTuples.filter(template.matches)
          a ! TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.InAll, tuples)
          val (newPendingTuples, newPendingRequests) = completeNoRequests(connections, pendingTuples, pendingRequests)(tuples)
          main(connections, newPendingTuples, newPendingRequests)
        })
        .getOrElse(Behaviors.same)
    case TupleSpaceActorCommand.RdAll(id, template) =>
      connections
        .get(id)
        .foreach(
          _ ! TemplateSeqTupleResponse(
            template,
            TemplateSeqTupleResponseType.RdAll,
            pendingTuples.filter(template.matches)
          )
        )
      Behaviors.same
    case TupleSpaceActorCommand.Enter(id, actor) =>
      actor ! ConnectionSuccessResponse(id)
      main(connections + (id -> actor), pendingTuples, pendingRequests)
    case TupleSpaceActorCommand.MergeIds(id, oldId) =>
      connections
        .get(id)
        .map(a => {
          a ! MergeSuccessResponse(id, oldId)
          main(connections - id + (oldId -> a), pendingTuples, pendingRequests)
        })
        .getOrElse(Behaviors.same)
    case TupleSpaceActorCommand.Exit(id, success) =>
      main(connections - id, pendingTuples, if (success) pendingRequests.filter(_._1 !== id) else pendingRequests)
  }

  def apply(root: ActorRef[Unit]): Behavior[TupleSpaceActorCommand] = Behaviors.setup(ctx => {
    root ! ()
    main(Map.empty, Seq.empty, Seq.empty)
  })
}
