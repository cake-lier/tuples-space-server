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

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.testkit.WSProbe
import io.circe.syntax.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

import tuples.space.*
import tuples.space.server.request.*
import tuples.space.server.request.RequestSerializer.given
import tuples.space.server.response.*
import tuples.space.server.response.ResponseSerializer.given

class TupleSpaceRouteTest extends AnyFunSpec with ScalatestRouteTest with BeforeAndAfterAll {

  private val testKit = ActorTestKit()
  private val tupleSpace = testKit.createTestProbe[TupleSpaceActorCommand]()
  private val route = TupleSpaceRoute("tuples", tupleSpace.ref, testKit.system)
  private val tuple = JsonTuple(1, "Example")
  private val template = tuples.space.complete(int, string)

  override def afterAll(): Unit = testKit.shutdownTestKit()

  describe("A json tuple space server") {
    describe("when an out request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((TupleRequest(tuple): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.Out]
          message.tuple shouldBe tuple
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a rd request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((TemplateRequest(template, TemplateRequestType.Rd): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.Rd]
          message.template shouldBe template
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when an in request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((TemplateRequest(template, TemplateRequestType.In): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.In]
          message.template shouldBe template
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a no request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((TemplateRequest(template, TemplateRequestType.No): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.No]
          message.template shouldBe template
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when an inp request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((TemplateRequest(template, TemplateRequestType.Inp): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.Inp]
          message.template shouldBe template
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a rdp request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((TemplateRequest(template, TemplateRequestType.Rdp): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.Rdp]
          message.template shouldBe template
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a nop request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((TemplateRequest(template, TemplateRequestType.Nop): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.Nop]
          message.template shouldBe template
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when an inAll request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((TemplateRequest(template, TemplateRequestType.InAll): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.InAll]
          message.template shouldBe template
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a rdAll request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((TemplateRequest(template, TemplateRequestType.RdAll): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.RdAll]
          message.template shouldBe template
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when an outAll request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((SeqTupleRequest(Seq(tuple)): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.OutAll]
          message.tuples shouldBe Seq(tuple)
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a merge ids request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        val oldUUID = UUID.randomUUID()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage((MergeRequest(oldUUID): Request).asJson.noSpaces)
          val message = tupleSpace.expectMessageType[TupleSpaceActorCommand.MergeIds]
          message.oldId shouldBe oldUUID
          wsProbe.sendMessage((TupleRequest(tuple): Request).asJson.noSpaces)
          val outMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Out]
          outMessage.tuple shouldBe tuple
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when an out response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! TupleResponse(tuple)
          wsProbe.expectMessage((TupleResponse(tuple): Response).asJson.noSpaces)
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when an in response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! TemplateTupleResponse(template, TemplateTupleResponseType.In, tuple)
          wsProbe.expectMessage((TemplateTupleResponse(template, TemplateTupleResponseType.In, tuple): Response).asJson.noSpaces)
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a rd response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple)
          wsProbe.expectMessage((TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple): Response).asJson.noSpaces)
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a no response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! TemplateResponse(template)
          wsProbe.expectMessage((TemplateResponse(template): Response).asJson.noSpaces)
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when an inp response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Inp, Some(tuple))
          wsProbe.expectMessage(
            (TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Inp, Some(tuple)): Response).asJson.noSpaces
          )
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a rdp response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Rdp, Some(tuple))
          wsProbe.expectMessage(
            (TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Rdp, Some(tuple)): Response).asJson.noSpaces
          )
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when an outAll response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! SeqTupleResponse(Seq(tuple))
          wsProbe.expectMessage((SeqTupleResponse(Seq(tuple)): Response).asJson.noSpaces)
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a inAll response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.InAll, Seq(tuple))
          wsProbe.expectMessage(
            (TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.InAll, Seq(tuple)): Response).asJson.noSpaces
          )
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a rdAll response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.RdAll, Seq(tuple))
          wsProbe.expectMessage(
            (TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.RdAll, Seq(tuple)): Response).asJson.noSpaces
          )
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a nop response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! TemplateBooleanResponse(template, true)
          wsProbe.expectMessage((TemplateBooleanResponse(template, true): Response).asJson.noSpaces)
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a connection success response is needed") {
      it("should send it") {
        val id = UUID.randomUUID()
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! ConnectionSuccessResponse(id)
          wsProbe.expectMessage((ConnectionSuccessResponse(id): Response).asJson.noSpaces)
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }

    describe("when a merge success response is needed") {
      it("should send it") {
        val oldClientId = UUID.randomUUID()
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.replyTo ! MergeSuccessResponse(oldClientId)
          wsProbe.expectMessage((MergeSuccessResponse(oldClientId): Response).asJson.noSpaces)
          wsProbe.sendCompletion()
          val exitMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Exit]
          exitMessage.success shouldBe true
        }
      }
    }
  }
}
