/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.perftests.registration

import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.performance.conf.ServicesConfiguration
import uk.gov.hmrc.perftests.registration.RegistrationRequests.{inputSelectorByName, loginUrl}

object RegistrationRequests extends ServicesConfiguration {

  val baseUrl: String = baseUrlFor("ioss-netp-registration-frontend")
  val route: String   = "/intermediary-netp"

  val loginUrl = baseUrlFor("auth-login-stub")

  def inputSelectorByName(name: String): Expression[String] = s"input[name='$name']"

  def getAuthorityWizard =
    http("Get Authority Wizard page")
      .get(loginUrl + s"/auth-login-stub/gg-sign-in")
      .check(status.in(200, 303))

  def postAuthorityWizard =
    http("Enter Auth login credentials ")
      .post(loginUrl + s"/auth-login-stub/gg-sign-in")
      .formParam("authorityId", "")
      .formParam("gatewayToken", "")
      .formParam("credentialStrength", "strong")
      .formParam("confidenceLevel", "50")
      .formParam("affinityGroup", "Organisation")
      .formParam("email", "user@test.com")
      .formParam("credentialRole", "User")
      .formParam("redirectionUrl", baseUrl + route)
      .formParam("enrolment[0].name", "HMRC-IOSS-INT")
      .formParam("enrolment[0].taxIdentifier[0].name", "IntNumber")
      .formParam("enrolment[0].taxIdentifier[0].value", "IN2501234567")
      .formParam("enrolment[0].state", "Activated")
      .check(status.in(200, 303))
      .check(headerRegex("Set-Cookie", """mdtp=(.*)""").saveAs("mdtpCookie"))

  def getClientUkBased =
    http("Get Client UK Based page")
      .get(s"$baseUrl$route/client-uk-based")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testClientUkBased(answer: Boolean) =
    http("Post Client UK Based page")
      .post(s"$baseUrl$route/client-uk-based")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", answer)
      .check(status.in(303))

  def postClientUkBased(answer: Boolean) =
    if (answer) {
      testClientUkBased(answer)
        .check(header("Location").is(s"$route/client-has-vat-number"))
    } else {
      testClientUkBased(answer)
        .check(header("Location").is(s"$route/client-country-based"))
    }

  def getClientHasVatNumber =
    http("Get Client Has Vat Number page")
      .get(s"$baseUrl$route/client-has-vat-number")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientHasVatNumber =
    http("Post Client Has Vat Number page")
      .post(s"$baseUrl$route/client-has-vat-number")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", true)
      .check(status.in(303))
      .check(header("Location").is(s"$route/client-vat-number"))

  def getClientVatNumber =
    http("Get Client Vat Number page")
      .get(s"$baseUrl$route/client-vat-number")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientVatNumber =
    http("Enter Client Vat Number")
      .post(s"$baseUrl$route/client-vat-number")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", "111222333")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/confirm-vat-details"))

  def getClientCountryBased =
    http("Get Client Country Based page")
      .get(s"$baseUrl$route/client-country-based")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientCountryBased =
    http("Enter Client Country Based")
      .post(s"$baseUrl$route/client-country-based")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", "NZ")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/client-business-name"))

  def getClientBusinessName =
    http("Get Client Business Name page")
      .get(s"$baseUrl$route/client-business-name")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientBusinessName =
    http("Enter Client Business Name")
      .post(s"$baseUrl$route/client-business-name")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", "Company Name")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/client-tax-reference"))

  def getClientTaxReference =
    http("Get Client Tax Reference page")
      .get(s"$baseUrl$route/client-tax-reference")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientTaxReference =
    http("Enter Client Tax Reference")
      .post(s"$baseUrl$route/client-tax-reference")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", "ABC123DEF1")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/client-address"))

  def getClientAddress =
    http("Get Client address page")
      .get(s"$baseUrl$route/client-address")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientAddress =
    http("Enter Client Address")
      .post(s"$baseUrl$route/client-address")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("line1", "line1")
      .formParam("townOrCity", "townOrCity")
      .formParam("postCode", "ABC")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/confirm-vat-details"))

  def getConfirmVatDetails =
    http("Get Confirm VAT Details page")
      .get(s"$baseUrl$route/confirm-vat-details")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postConfirmVatDetails =
    http("Post Confirm VAT Details page")
      .post(s"$baseUrl$route/confirm-vat-details")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", "yes")
      .check(status.in(303))
//      rest of the journey is not developed yet
//      .check(header("Location").is(s"$route/have-uk-trading-name"))

  def postConfirmVatDetailsContinue =
    http("Post Confirm VAT Details page")
      .post(s"$baseUrl$route/confirm-vat-details")
      .formParam("csrfToken", "${csrfToken}")
      .check(status.in(303))
  //      rest of the journey is not developed yet
  //      .check(header("Location").is(s"$route/have-uk-trading-name"))

  def getWebsite(index: Int) =
    http(s"Get Website page $index")
      .get(s"$baseUrl$route/website-address/$index")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postWebsite(index: Int, website: String) =
    http(s"Enter website $index")
      .post(s"$baseUrl$route/website-address/$index")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", website)
      .check(status.in(303))
      .check(header("Location").is(s"$route/add-website-address"))

  def getAddWebsite =
    http("Get Add Website page")
      .get(s"$baseUrl$route/add-website-address")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testAddWebsite(answer: Boolean) =
    http("Add Website")
      .post(s"$baseUrl$route/add-website-address")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", answer)
      .check(status.in(200, 303))

  def postAddWebsite(answer: Boolean, index: Option[Int]) =
    if (answer) {
      testAddWebsite(answer)
        .check(header("Location").is(s"$route/website-address/${index.get}"))
    } else {
      testAddWebsite(answer)
        .check(header("Location").is(s"$route/business-contact-details"))
    }

  def getBusinessContactDetails =
    http("Get Business Contact Details page")
      .get(s"$baseUrl$route/business-contact-details")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postBusinessContactDetails =
    http("Enter Business Contact Details")
      .post(s"$baseUrl$route/business-contact-details")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("fullName", "Trader Name")
      .formParam("telephoneNumber", "012301230123")
      .formParam("emailAddress", "trader@testemail.com")
      .check(status.in(200, 303))
  //      rest of the journey is not developed yet
//      .check(header("Location").is(s"$route/check-your-answers"))

  def getDeclaration =
    http("Get Declaration page")
      .get(s"$baseUrl$route/declaration")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postDeclaration =
    http("Post Declaration page")
      .post(s"$baseUrl$route/declaration")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("declaration", "true")
      .check(status.in(303))
  //      rest of the journey is not developed yet
  //      .check(header("Location").is(s"$route/pending-registration"))

}
