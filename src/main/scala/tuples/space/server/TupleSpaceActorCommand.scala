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

import tuples.space.*
import tuples.space.server.response.Response

import akka.actor.typed.ActorRef

import java.util.UUID

enum TupleSpaceActorCommand {

  case Out(id: UUID, tuple: JsonTuple) extends TupleSpaceActorCommand

  case In(id: UUID, template: JsonTemplate) extends TupleSpaceActorCommand

  case Rd(id: UUID, template: JsonTemplate) extends TupleSpaceActorCommand

  case No(id: UUID, template: JsonTemplate) extends TupleSpaceActorCommand

  case OutAll(id: UUID, tuples: Seq[JsonTuple]) extends TupleSpaceActorCommand

  case InAll(id: UUID, template: JsonTemplate) extends TupleSpaceActorCommand

  case RdAll(id: UUID, template: JsonTemplate) extends TupleSpaceActorCommand

  case Inp(id: UUID, template: JsonTemplate) extends TupleSpaceActorCommand

  case Rdp(id: UUID, template: JsonTemplate) extends TupleSpaceActorCommand

  case Nop(id: UUID, template: JsonTemplate) extends TupleSpaceActorCommand

  case Enter(id: UUID, actor: ActorRef[Response]) extends TupleSpaceActorCommand

  case MergeIds(id: UUID, oldId: UUID) extends TupleSpaceActorCommand

  case Exit(id: UUID, success: Boolean) extends TupleSpaceActorCommand
}
