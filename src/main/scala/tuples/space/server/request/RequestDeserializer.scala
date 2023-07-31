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
package tuples.space.server.request

import io.circe.Decoder
import io.circe.DecodingFailure
import io.circe.Json
import io.circe.syntax.*

import AnyOps.*
import tuples.space.*
import tuples.space.JsonSerializable.given
import tuples.space.server.request.*

/** This object contains all deserializers for the [[Request]] sub-types. */
private[server] object RequestDeserializer {

  /* The Decoder given instance for the TupleRequest trait. */
  private given Decoder[TupleRequest] = c =>
    for {
      content <- c.downField("content").as[JsonTuple]
      - <- c
        .downField("type")
        .as[String]
        .filterOrElse(
          _ === "out",
          DecodingFailure(
            DecodingFailure.Reason.CustomReason("The value for the type field was not valid"),
            c.downField("type")
          )
        )
    } yield TupleRequest(content)

    /* The Decoder given instance for the SeqTupleRequest trait. */
  private given Decoder[SeqTupleRequest] = c =>
    for {
      content <- c.downField("content").as[Seq[JsonTuple]]
      - <- c
        .downField("type")
        .as[String]
        .filterOrElse(
          _ === "outAll",
          DecodingFailure(
            DecodingFailure.Reason.CustomReason("The value for the type field was not valid"),
            c.downField("type")
          )
        )
    } yield SeqTupleRequest(content)

    /* The Decoder given instance for the TemplateRequest trait. */
  private given Decoder[TemplateRequest] = c =>
    for {
      content <- c.downField("content").as[JsonTemplate]
      tpe <- c.downField("type").as[String].flatMap {
        case "in" => Right[DecodingFailure, TemplateRequestType](TemplateRequestType.In)
        case "rd" => Right[DecodingFailure, TemplateRequestType](TemplateRequestType.Rd)
        case "no" => Right[DecodingFailure, TemplateRequestType](TemplateRequestType.No)
        case "inp" => Right[DecodingFailure, TemplateRequestType](TemplateRequestType.Inp)
        case "rdp" => Right[DecodingFailure, TemplateRequestType](TemplateRequestType.Rdp)
        case "nop" => Right[DecodingFailure, TemplateRequestType](TemplateRequestType.Nop)
        case "inAll" => Right[DecodingFailure, TemplateRequestType](TemplateRequestType.InAll)
        case "rdAll" => Right[DecodingFailure, TemplateRequestType](TemplateRequestType.RdAll)
        case _ =>
          Left[DecodingFailure, TemplateRequestType](
            DecodingFailure(
              DecodingFailure.Reason.CustomReason("The value for the type field was not valid"),
              c.downField("type")
            )
          )
      }
    } yield TemplateRequest(content, tpe)

    /* The Decoder given instance for the MergeRequest trait. */
  private given Decoder[MergeRequest] = Decoder.forProduct1("oldClientId")(MergeRequest.apply)

  /** The [[Decoder]] given instance for the general [[Request]] trait, working for all of its sub-types. */
  given Decoder[Request] = r =>
    r.as[TupleRequest]
      .orElse[DecodingFailure, Request](r.as[SeqTupleRequest])
      .orElse[DecodingFailure, Request](r.as[MergeRequest])
      .orElse[DecodingFailure, Request](r.as[TemplateRequest])
}
