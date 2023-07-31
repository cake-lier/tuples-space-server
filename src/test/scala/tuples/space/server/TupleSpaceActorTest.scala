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

import java.util.UUID

import scala.concurrent.duration.DurationInt

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec

import tuples.space.*
import tuples.space.server.response.*
import tuples.space.server.request.*

class TupleSpaceActorTest extends AnyFunSpec with BeforeAndAfterAll {

  private val testKit = ActorTestKit()

  override def afterAll(): Unit = testKit.shutdownTestKit()

  describe("A json tuple space") {
    describe("when first booted") {
      it("should notify its root actor") {
        val rootProbe = testKit.createTestProbe[Unit]()
        val tupleSpace = testKit.spawn(TupleSpaceActor(rootProbe.ref))

        rootProbe.expectMessage(())

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an in request without a previous matching out request") {
      it("should return nothing at all") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.In(template, responseProbe.ref)
        responseProbe.expectNoMessage()

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving a rd request without a previous matching out request") {
      it("should return nothing at all") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectNoMessage()

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving a no request without a previous matching out request") {
      it("should return its completion") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.No(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateResponse(template))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request") {
      it("should return its completion") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request followed by a matching in request") {
      it("should return the inserted tuple and remove it from the tuple space") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.In(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.In, tuple))
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectNoMessage()

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request followed by a matching rd request") {
      it("should return the inserted tuple and keep it in the tuple space") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request followed by a matching no request") {
      it("should return nothing at all") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.No(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an in request followed by a matching out request") {
      it("should return the inserted tuple and remove it from the tuple space") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.In(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        responseProbe.expectMessage(TupleResponse(tuple))
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.In, tuple))
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectNoMessage()

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving a rd request followed by a matching out request") {
      it("should return the inserted tuple and keep it in the tuple space") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request followed by matching no and in requests") {
      it("should return the no request completion") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.No(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.In(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.In, tuple))
        responseProbe.expectMessage(TemplateResponse(template))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an inp request without a previous matching out request") {
      it("should return a None") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Inp(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Inp, None))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving a rdp request without a previous matching out request") {
      it("should return a None") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Rdp(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Rdp, None))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving a nop request without a previous matching out request") {
      it("should return a true") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Nop(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateBooleanResponse(template, true))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request followed by a matching inp request") {
      it("should return a Some with the inserted tuple and remove it from the tuple space") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Inp(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Inp, Some(tuple)))
        tupleSpace ! TupleSpaceActorCommand.Rdp(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Rdp, None))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request followed by a matching rdp request") {
      it("should return a Some with the inserted tuple and keep it in the tuple space") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Rdp(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Rdp, Some(tuple)))
        tupleSpace ! TupleSpaceActorCommand.Rdp(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Rdp, Some(tuple)))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request followed by a matching nop request") {
      it("should return a false") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Nop(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateBooleanResponse(template, false))
        tupleSpace ! TupleSpaceActorCommand.Rdp(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Rdp, Some(tuple)))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an inAll request without a previous matching out request") {
      it("should return an empty Seq") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.InAll(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.InAll, Seq.empty))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving a rdAll request without a previous matching out request") {
      it("should return a None") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.RdAll(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.RdAll, Seq.empty))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request followed by a matching inAll request") {
      it("should return a Seq with the inserted tuple and remove it from the tuple space") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.InAll(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.InAll, Seq(tuple)))
        tupleSpace ! TupleSpaceActorCommand.RdAll(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.RdAll, Seq.empty))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request followed by a matching rdAll request") {
      it("should return a Seq with the inserted tuple and keep it in the tuple space") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.RdAll(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.RdAll, Seq(tuple)))
        tupleSpace ! TupleSpaceActorCommand.RdAll(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.RdAll, Seq(tuple)))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an outAll request") {
      it("should return its completion") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.OutAll(Seq(tuple), responseProbe.ref)
        responseProbe.expectMessage(SeqTupleResponse(Seq(tuple)))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an in request followed by a matching outAll request") {
      it("should return the inserted tuple and remove it from the tuple space") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.In(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.OutAll(Seq(tuple), responseProbe.ref)
        responseProbe.expectMessage(SeqTupleResponse(Seq(tuple)))
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.In, tuple))
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectNoMessage()

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving a rd request followed by a matching outAll request") {
      it("should return the inserted tuple and keep it in the tuple space") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.OutAll(Seq(tuple), responseProbe.ref)
        responseProbe.expectMessage(SeqTupleResponse(Seq(tuple)))
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an out request after exiting") {
      it("should return nothing at all") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Exit(success = true, responseProbe.ref)
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectNoMessage()

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an in or a rd request after exiting") {
      it("should return nothing at all") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Exit(success = true, responseProbe.ref)
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.In(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving a no request after exiting") {
      it("should return nothing at all") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Exit(success = true, responseProbe.ref)
        tupleSpace ! TupleSpaceActorCommand.No(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.No(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an inp or a rdp or a nop request after exiting") {
      it("should return nothing at all") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Exit(success = true, responseProbe.ref)
        tupleSpace ! TupleSpaceActorCommand.Inp(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Rdp(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Nop(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving a rdAll or an inAll request after exiting") {
      it("should return nothing at all") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Exit(success = true, responseProbe.ref)
        tupleSpace ! TupleSpaceActorCommand.RdAll(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.InAll(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an outAll request after exiting") {
      it("should return nothing at all") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Exit(success = true, responseProbe.ref)
        tupleSpace ! TupleSpaceActorCommand.OutAll(Seq(tuple), responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessageType[ConnectionSuccessResponse]
        tupleSpace ! TupleSpaceActorCommand.Rd(template, responseProbe.ref)
        responseProbe.expectNoMessage()

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving a merge ids command") {
      it("should swap the new id for the old one") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val firstUUID = UUID.randomUUID()
        val secondUUID = UUID.randomUUID()
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessage(ConnectionSuccessResponse(firstUUID))
        tupleSpace ! TupleSpaceActorCommand.MergeIds(secondUUID, responseProbe.ref)
        responseProbe.expectMessage(MergeSuccessResponse(secondUUID))
        tupleSpace ! TupleSpaceActorCommand.Inp(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Inp(template, responseProbe.ref)
        responseProbe.expectMessage(TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Inp, None))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an exit with error command") {
      it("should retain the associated pending requests waiting for a reconnection") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]("example").ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val firstUUID = UUID.randomUUID()
        val secondUUID = UUID.randomUUID()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessage(ConnectionSuccessResponse(firstUUID))
        tupleSpace ! TupleSpaceActorCommand.In(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Exit(success = false, responseProbe.ref)
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessage(ConnectionSuccessResponse(secondUUID))
        tupleSpace ! TupleSpaceActorCommand.MergeIds(secondUUID, responseProbe.ref)
        responseProbe.expectMessage(MergeSuccessResponse(secondUUID))
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        responseProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.In, tuple))

        testKit.stop(tupleSpace, 10.seconds)
      }
    }

    describe("when receiving an exit with success command") {
      it("should discard the associated pending requests") {
        val tupleSpace = testKit.spawn(TupleSpaceActor(testKit.createTestProbe[Unit]().ref))
        val responseProbe = testKit.createTestProbe[Response]()
        val firstUUID = UUID.randomUUID()
        val secondUUID = UUID.randomUUID()
        val tuple = JsonTuple(1, "Example")
        val template = complete(int, string)

        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessage(ConnectionSuccessResponse(firstUUID))
        tupleSpace ! TupleSpaceActorCommand.In(template, responseProbe.ref)
        responseProbe.expectNoMessage()
        tupleSpace ! TupleSpaceActorCommand.Exit(success = true, responseProbe.ref)
        tupleSpace ! TupleSpaceActorCommand.Enter(responseProbe.ref)
        responseProbe.expectMessage(ConnectionSuccessResponse(secondUUID))
        tupleSpace ! TupleSpaceActorCommand.MergeIds(secondUUID, responseProbe.ref)
        responseProbe.expectMessage(10.seconds, MergeSuccessResponse(secondUUID))
        tupleSpace ! TupleSpaceActorCommand.Out(tuple, responseProbe.ref)
        responseProbe.expectMessage(TupleResponse(tuple))
        responseProbe.expectNoMessage()

        testKit.stop(tupleSpace, 10.seconds)
      }
    }
  }
}
