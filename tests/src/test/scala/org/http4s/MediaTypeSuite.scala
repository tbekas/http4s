/*
 * Copyright 2013 http4s.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.http4s

import org.http4s.syntax.all._
import cats.syntax.show._
import cats.kernel.laws.discipline.EqTests
import org.http4s.laws.discipline.ArbitraryInstances._
import org.http4s.laws.discipline.HttpCodecTests

class MediaTypeSuite extends Http4sSuite {
  checkAll("Eq[MediaType]", EqTests[MediaType].eqv)
  checkAll("HttpCodec[MediaType]", HttpCodecTests[MediaType].httpCodec)

  test("MediaType should Render itself") {
    assertEquals(MediaType.text.html.show, "text/html")
  }

  test("MediaType should Quote extension strings") {
    assertEquals(
      MediaType.text.html
        .withExtensions(Map("foo" -> "bar"))
        .show,
      """text/html; foo="bar"""")
  }

  test("MediaType should Encode extensions with special characters") {
    assertEquals(
      MediaType.text.html
        .withExtensions(Map("foo" -> ";"))
        .show,
      """text/html; foo=";"""")
  }

  test("MediaType should Escape special chars in media range extensions") {
    assertEquals(
      MediaType.text.html
        .withExtensions(Map("foo" -> "\\"))
        .show,
      """text/html; foo="\\"""")
    assertEquals(
      MediaType.text.html
        .withExtensions(Map("foo" -> "\""))
        .show,
      """text/html; foo="\""""")
  }

  test("MediaType should Do a round trip through the Accept header") {
    val raw = Header(
      "Accept",
      """text/csv;charset=UTF-8;columnDelimiter=" "; rowDelimiter=";"; quoteChar='; escapeChar="\\"""")
    assert(raw.parsed.isInstanceOf[headers.Accept])
    assertEquals(Header("Accept", raw.parsed.value).parsed, raw.parsed)
  }

  test("MediaType should reject invalid literals") {
    assertNoDiff(
      compileErrors {
        """mediaType"not valid""""
      },
      """|error: invalid MediaType
              |mediaType"not valid"
              |^
              |""".stripMargin
    )
  }

}
