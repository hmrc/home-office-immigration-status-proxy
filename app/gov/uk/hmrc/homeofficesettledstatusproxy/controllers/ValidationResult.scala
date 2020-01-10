package gov.uk.hmrc.homeofficesettledstatusproxy.controllers

import gov.uk.hmrc.homeofficesettledstatusproxy.models.StatusCheckByNinoRequest

sealed trait ValidationResult

case class ValidRequest(request: StatusCheckByNinoRequest) extends ValidationResult

case class MissingInputFields(fields: List[String]) extends ValidationResult

case class InvalidInputFields(fields: List[(String, String)]) extends ValidationResult
