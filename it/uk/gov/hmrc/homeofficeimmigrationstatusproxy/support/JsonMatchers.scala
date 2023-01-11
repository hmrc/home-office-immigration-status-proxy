/*
 * Copyright 2023 HM Revenue & Customs
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

package support

import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.libs.json.{JsArray, JsObject, JsValue, Reads}

import scala.reflect.ClassTag

trait JsonMatchers {

  def haveProperty[T: Reads](name: String, matcher: Matcher[T] = null)(implicit
    classTag: ClassTag[T]
  ): Matcher[JsObject] =
    (obj: JsObject) =>
      (obj \ name).asOpt[T] match {
        case Some(value) =>
          if (matcher != null) matcher(value) match {
            case x =>
              x.copy(
                rawNegatedFailureMessage = s"At `$name` ${x.rawNegatedFailureMessage}",
                rawMidSentenceNegatedFailureMessage = s"at `$name` ${x.rawMidSentenceNegatedFailureMessage}",
                rawFailureMessage = s"at `$name` ${x.rawFailureMessage}",
                rawMidSentenceFailureMessage = s"at `$name` ${x.rawMidSentenceFailureMessage}"
              )
          }
          else MatchResult(matches = true, "", s"JSON have property `$name`")
        case _ =>
          MatchResult(
            matches = false,
            s"JSON should have property `$name` of type ${classTag.runtimeClass.getSimpleName}, but had only ${obj.fields
              .map(f => s"${f._1}:${f._2.getClass.getSimpleName}")
              .mkString(", ")}",
            ""
          )
      }

  def havePropertyArrayOf[T: Reads](name: String, matcher: Matcher[T] = null)(implicit
    classTag: ClassTag[T]
  ): Matcher[JsObject] =
    (obj: JsObject) =>
      (obj \ name).asOpt[JsArray] match {
        case Some(array) =>
          if (matcher != null)
            array.value
              .map(_.as[T])
              .foldLeft(MatchResult(matches = true, "", ""))((a: MatchResult, v: T) => if (a.matches) matcher(v) else a)
          else MatchResult(matches = true, "", s"JSON have property `$name`")
        case _ =>
          MatchResult(
            matches = false,
            s"JSON should have array property `$name` of item type ${classTag.runtimeClass.getSimpleName}, but had only ${obj.fields
              .map(f => s"${f._1}:${f._2.getClass.getSimpleName}")
              .mkString(", ")}",
            ""
          )
      }

  def notHaveProperty(name: String): Matcher[JsObject] =
    (obj: JsObject) =>
      (obj \ name).asOpt[JsValue] match {
        case Some(value) =>
          MatchResult(matches = false, s"JSON should not have property `$name` but we got value $value", s"")
        case None =>
          MatchResult(matches = true, "", s"JSON does not have property `$name`")
      }

  def eachElement[T](matcher: Matcher[T]): Matcher[Seq[T]] = (left: Seq[T]) =>
    left.foldLeft(MatchResult(matches = true, "", ""))((a: MatchResult, v: T) => if (a.matches) matcher(v) else a)

  def oneOfValues[T](values: T*): Matcher[T] = (left: T) =>
    MatchResult(
      values.contains(left),
      s"$left is an unexpected value, should be one of ${values.mkString("[", ",", "]")}",
      s"$left was expected"
    )

}
