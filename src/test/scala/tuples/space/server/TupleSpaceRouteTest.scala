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
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import tuples.space.*
import tuples.space.server.request.*
import tuples.space.server.request.Serializers.given
import tuples.space.server.response.*
import tuples.space.server.response.Serializers.given

import java.util.UUID

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
          wsProbe.sendMessage(TupleRequest(tuple).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.Out(enterMessage.id, tuple))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a rd request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage(TemplateRequest(template, TemplateRequestType.Rd).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.Rd(enterMessage.id, template))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when an in request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage(TemplateRequest(template, TemplateRequestType.In).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.In(enterMessage.id, template))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a no request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage(TemplateRequest(template, TemplateRequestType.No).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.No(enterMessage.id, template))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when an inp request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage(TemplateRequest(template, TemplateRequestType.Inp).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.Inp(enterMessage.id, template))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a rdp request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage(TemplateRequest(template, TemplateRequestType.Rdp).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.Rdp(enterMessage.id, template))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a nop request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage(TemplateRequest(template, TemplateRequestType.Nop).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.Nop(enterMessage.id, template))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when an inAll request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage(TemplateRequest(template, TemplateRequestType.InAll).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.InAll(enterMessage.id, template))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a rdAll request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage(TemplateRequest(template, TemplateRequestType.RdAll).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.RdAll(enterMessage.id, template))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when an outAll request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage(SeqTupleRequest(Seq(tuple)).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.OutAll(enterMessage.id, Seq(tuple)))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a merge ids request is received") {
      it("should notify the json tuple space actor") {
        val wsProbe: WSProbe = WSProbe()
        val oldUUID = UUID.randomUUID()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          wsProbe.sendMessage(MergeRequest(enterMessage.id, oldUUID).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.MergeIds(enterMessage.id, oldUUID))
          wsProbe.sendMessage(TupleRequest(tuple).asJson.noSpaces)
          tupleSpace.expectMessage(TupleSpaceActorCommand.Out(oldUUID, tuple))
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when an out response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! TupleResponse(tuple)
          wsProbe.expectMessage(TupleResponse(tuple).asJson.noSpaces)
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when an in response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! TemplateTupleResponse(template, TemplateTupleResponseType.In, tuple)
          wsProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.In, tuple).asJson.noSpaces)
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a rd response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple)
          wsProbe.expectMessage(TemplateTupleResponse(template, TemplateTupleResponseType.Rd, tuple).asJson.noSpaces)
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a no response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! TemplateResponse(template)
          wsProbe.expectMessage(TemplateResponse(template).asJson.noSpaces)
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when an inp response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Inp, Some(tuple))
          wsProbe.expectMessage(
            TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Inp, Some(tuple)).asJson.noSpaces
          )
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a rdp response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Rdp, Some(tuple))
          wsProbe.expectMessage(
            TemplateMaybeTupleResponse(template, TemplateMaybeTupleResponseType.Rdp, Some(tuple)).asJson.noSpaces
          )
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when an outAll response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! SeqTupleResponse(Seq(tuple))
          wsProbe.expectMessage(SeqTupleResponse(Seq(tuple)).asJson.noSpaces)
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a inAll response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.InAll, Seq(tuple))
          wsProbe.expectMessage(
            TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.InAll, Seq(tuple)).asJson.noSpaces
          )
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a rdAll response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.RdAll, Seq(tuple))
          wsProbe.expectMessage(
            TemplateSeqTupleResponse(template, TemplateSeqTupleResponseType.RdAll, Seq(tuple)).asJson.noSpaces
          )
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a nop response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! TemplateBooleanResponse(template, true)
          wsProbe.expectMessage(TemplateBooleanResponse(template, true).asJson.noSpaces)
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a connection success response is needed") {
      it("should send it") {
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! ConnectionSuccessResponse(enterMessage.id)
          wsProbe.expectMessage(ConnectionSuccessResponse(enterMessage.id).asJson.noSpaces)
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }

    describe("when a merge success response is needed") {
      it("should send it") {
        val oldClientId = UUID.randomUUID()
        val wsProbe: WSProbe = WSProbe()
        WS("/tuples", wsProbe.flow) ~> route ~> check {
          val enterMessage = tupleSpace.expectMessageType[TupleSpaceActorCommand.Enter]
          enterMessage.actor ! MergeSuccessResponse(enterMessage.id, oldClientId)
          wsProbe.expectMessage(MergeSuccessResponse(enterMessage.id, oldClientId).asJson.noSpaces)
          wsProbe.sendCompletion()
          tupleSpace.expectMessage(TupleSpaceActorCommand.Exit(enterMessage.id, success = true))
        }
      }
    }
  }
}
