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
      .formParam("enrolment[0].name", "HMRC-MTD-VAT")
      .formParam("enrolment[0].taxIdentifier[0].name", "VRN")
      .formParam("enrolment[0].taxIdentifier[0].value", "100000001")
      .formParam("enrolment[0].state", "Activated")
      .formParam("enrolment[1].name", "HMRC-IOSS-INT")
      .formParam("enrolment[1].taxIdentifier[0].name", "IntNumber")
      .formParam("enrolment[1].taxIdentifier[0].value", "IN2501234567")
      .formParam("enrolment[1].state", "Activated")
      .check(status.in(200, 303))
      .check(headerRegex("Set-Cookie", """mdtp=(.*)""").saveAs("mdtpCookie"))

  def getClientUkBased =
    http("Get Client UK Based page")
      .get(s"$baseUrl$route/client-uk-based")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientUkBased(answer: Boolean) =
    http("Post Client UK Based page")
      .post(s"$baseUrl$route/client-uk-based")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", answer)
      .check(status.in(303))
      .check(header("Location").is(s"$route/client-has-vat-number"))

  def getClientHasVatNumber =
    http("Get Client Has Vat Number page")
      .get(s"$baseUrl$route/client-has-vat-number")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testClientHasVatNumber(answer: Boolean) =
    http("Post Client Has Vat Number page")
      .post(s"$baseUrl$route/client-has-vat-number")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", answer)
      .check(status.in(200, 303))

  def postClientHasVatNumber(answer: Boolean, ukRoute: Boolean) =
    if (answer) {
      testClientHasVatNumber(answer)
        .check(header("Location").is(s"$route/client-vat-number"))
    } else if (!ukRoute) {
      testClientHasVatNumber(answer)
        .check(header("Location").is(s"$route/client-country-based"))
    } else {
      testClientHasVatNumber(answer)
        .check(header("Location").is(s"$route/client-business-name"))
    }

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
      .check(header("Location").is(s"$route/client-tax-reference"))

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
      .check(header("Location").is(s"$route/client-address"))

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
      .check(header("Location").is(s"$route/client-business-name"))

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
      .check(status.in(303))
      .check(header("Location").is(s"$route/have-uk-trading-name"))

  def getHaveUkTradingName =
    http("Get Have UK Trading Name page")
      .get(s"$baseUrl$route/have-uk-trading-name")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postHaveUkTradingName =
    http("Post Have UK Trading Name page")
      .post(s"$baseUrl$route/have-uk-trading-name")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", true)
      .check(status.in(303))
      .check(header("Location").is(s"$route/uk-trading-name/1"))

  def getUkTradingName(index: Int) =
    http("Get UK Trading Name page")
      .get(s"$baseUrl$route/uk-trading-name/$index")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postUkTradingName(index: Int, tradingName: String) =
    http("Post UK Trading Name page")
      .post(s"$baseUrl$route/uk-trading-name/$index")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", tradingName)
      .check(status.in(303))
      .check(header("Location").is(s"$route/add-uk-trading-name"))

  def getAddTradingName =
    http("Get Add Trading Name page")
      .get(s"$baseUrl$route/add-uk-trading-name")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testAddTradingName(answer: Boolean) =
    http("Add Trading Name")
      .post(s"$baseUrl$route/add-uk-trading-name")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", answer)
      .check(status.in(200, 303))

  def postAddTradingName(answer: Boolean, index: Option[Int]) =
    if (answer) {
      testAddTradingName(answer)
        .check(header("Location").is(s"$route/uk-trading-name/${index.get}"))
    } else {
      testAddTradingName(answer)
        .check(header("Location").is(s"$route/previous-oss"))
    }

  def postPreviousOss(index: Int) =
    http("Answer Previous Oss Page")
      .post(s"$baseUrl$route/previous-oss")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", "true")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-country/$index"))

  def getPreviousCountry(index: Int) =
    http("Get previous country page")
      .get(s"$baseUrl$route/previous-country/$index")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postPreviousCountry(countryIndex: Int, schemeIndex: Int, countryCode: String) =
    http("Enter previous country")
      .post(s"$baseUrl$route/previous-country/$countryIndex")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", countryCode)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-scheme/$countryIndex/$schemeIndex"))

  def getPreviousScheme(countryIndex: Int, schemeIndex: Int) =
    http("Get Previous Scheme page")
      .get(s"$baseUrl$route/previous-scheme/$countryIndex/$schemeIndex")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testPreviousScheme(countryIndex: Int, schemeIndex: Int, schemeType: String) =
    http("Answer Previous Scheme")
      .post(s"$baseUrl$route/previous-scheme/$countryIndex/$schemeIndex")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", schemeType)
      .check(status.in(200, 303))

  def postPreviousScheme(countryIndex: Int, schemeIndex: Int, schemeType: String) =
    if (schemeType == "oss") {
      testPreviousScheme(countryIndex, schemeIndex, schemeType)
        .check(header("Location").is(s"$route/previous-oss-scheme-number/$countryIndex/$schemeIndex"))
    } else {
      testPreviousScheme(countryIndex, schemeIndex, schemeType)
        .check(header("Location").is(s"$route/previous-ioss-number/$countryIndex/$schemeIndex"))
    }

  def getPreviousOssSchemeNumber(countryIndex: Int, schemeIndex: Int) =
    http("Get Previous Oss Scheme number page")
      .get(s"$baseUrl$route/previous-oss-scheme-number/$countryIndex/$schemeIndex")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postPreviousOssSchemeNumber(countryIndex: Int, schemeIndex: Int, registrationNumber: String) =
    http("Enter Previous Oss Scheme Number")
      .post(s"$baseUrl$route/previous-oss-scheme-number/$countryIndex/$schemeIndex")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", registrationNumber)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-scheme-answers/$countryIndex"))

  def getPreviousSchemeAnswers(index: Int) =
    http("Get Previous Scheme Answers page")
      .get(s"$baseUrl$route/previous-scheme-answers/$index")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postPreviousSchemeAnswers(index: Int, answer: Boolean) =
    http("Post Previous Scheme Answers page")
      .post(s"$baseUrl$route/previous-scheme-answers/$index")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", answer)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-schemes-overview"))

  def getPreviousSchemesOverview =
    http("Get Previous Schemes Overview page")
      .get(s"$baseUrl$route/previous-schemes-overview")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testPreviousSchemesOverview(answer: Boolean) =
    http("Previous Schemes Overview")
//      completion checks not done yet
//      .post(s"$baseUrl$route/previous-schemes-overview?incompletePromptShown=false")
      .post(s"$baseUrl$route/previous-schemes-overview")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", answer)
      .check(status.in(200, 303))

  def postPreviousSchemesOverview(answer: Boolean, index: Option[Int]) =
    if (answer) {
      testPreviousSchemesOverview(answer)
        .check(header("Location").is(s"$route/previous-country/${index.get}"))
    } else {
      testPreviousSchemesOverview(answer)
//        next section not developed yet
//        .check(header("Location").is(s"$route/tax-in-eu"))
    }

  def getPreviousIossNumber(countryIndex: Int, schemeIndex: Int) =
    http("Get Previous IOSS number page")
      .get(s"$baseUrl$route/previous-ioss-number/$countryIndex/$schemeIndex")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postPreviousIossNumber(countryIndex: Int, schemeIndex: Int, iossNumber: String) =
    http("Previous IOSS Number")
      .post(s"$baseUrl$route/previous-ioss-number/$countryIndex/$schemeIndex")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("value", iossNumber)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-scheme-answers/$countryIndex"))

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
      .check(header("Location").is(s"$route/check-your-answers"))

  def getCheckYourAnswers =
    http("Get Check Your Answers page")
      .get(s"$baseUrl$route/check-your-answers")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postCheckYourAnswers =
    http("Post Check Your Answers page")
      .post(s"$baseUrl$route/check-your-answers")
//      Completion checks not added yet
//      .post(s"$baseUrl$route/check-your-answers/false?waypoints=check-your-answers")
      .formParam("csrfToken", "${csrfToken}")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/declaration"))

  def getDeclaration =
    http("Get Declaration page")
      .get(s"$baseUrl$route/declaration")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postDeclaration =
    http("Post Declaration page")
      .post(s"$baseUrl$route/declaration")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("declaration", "true")
      .check(status.in(303))
      .check(header("Location").is(s"$route/client-application-complete"))

  def getClientApplicationComplete =
    http("Get Client Application Complete page")
      .get(s"$baseUrl$route/client-application-complete")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(status.in(200))

}
