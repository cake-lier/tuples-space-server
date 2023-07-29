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

/** The enum representing all the different types of operations that can be carried on using a [[TemplateRequest]]. */
private[server] enum TemplateRequestType {

  /** The enum instance representing the "in" operation. */
  case In extends TemplateRequestType

    /** The enum instance representing the "rd" operation. */
  case Rd extends TemplateRequestType

    /** The enum instance representing the "no" operation. */
  case No extends TemplateRequestType

    /** The enum instance representing the "inp" operation. */
  case Inp extends TemplateRequestType

    /** The enum instance representing the "rdp" operation. */
  case Rdp extends TemplateRequestType

    /** The enum instance representing the "nop" operation. */
  case Nop extends TemplateRequestType

    /** The enum instance representing the "inAll" operation. */
  case InAll extends TemplateRequestType

    /** The enum instance representing the "rdAll" operation. */
  case RdAll extends TemplateRequestType
}