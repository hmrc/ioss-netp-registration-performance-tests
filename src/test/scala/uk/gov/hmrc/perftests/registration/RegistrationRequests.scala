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
import uk.gov.hmrc.performance.conf.ServicesConfiguration

object RegistrationRequests extends ServicesConfiguration {

  val baseUrl: String = baseUrlFor("ioss-netp-registration-frontend")
  val route: String   = "/pay-clients-vat-on-eu-sales/register-new-ioss-client"

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
      .formParam("enrolment[1].taxIdentifier[0].value", "IN9001234567")
      .formParam("enrolment[1].state", "Activated")
      .check(status.in(200, 303))
      .check(headerRegex("Set-Cookie", """mdtp=(.*)""").saveAs("mdtpCookie"))

  def getClientUkBased =
    http("Get Client UK Based page")
      .get(s"$baseUrl$route/client-uk-based")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientUkBased(answer: Boolean) =
    http("Post Client UK Based page")
      .post(s"$baseUrl$route/client-uk-based")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", answer)
      .check(status.in(303))
      .check(header("Location").is(s"$route/client-has-vat-number"))

  def getClientHasVatNumber =
    http("Get Client Has Vat Number page")
      .get(s"$baseUrl$route/client-has-vat-number")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testClientHasVatNumber(answer: Boolean) =
    http("Post Client Has Vat Number page")
      .post(s"$baseUrl$route/client-has-vat-number")
      .formParam("csrfToken", "#{csrfToken}")
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
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientVatNumber =
    http("Enter Client Vat Number")
      .post(s"$baseUrl$route/client-vat-number")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "111222333")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/confirm-vat-details"))

  def getClientCountryBased =
    http("Get Client Country Based page")
      .get(s"$baseUrl$route/client-country-based")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientCountryBased =
    http("Enter Client Country Based")
      .post(s"$baseUrl$route/client-country-based")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "NZ")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/client-tax-reference"))

  def getClientBusinessName =
    http("Get Client Business Name page")
      .get(s"$baseUrl$route/client-business-name")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientBusinessName =
    http("Enter Client Business Name")
      .post(s"$baseUrl$route/client-business-name")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "Company Name")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/client-address"))

  def getClientTaxReference =
    http("Get Client Tax Reference page")
      .get(s"$baseUrl$route/client-tax-reference")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientTaxReference =
    http("Enter Client Tax Reference")
      .post(s"$baseUrl$route/client-tax-reference")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "ABC123DEF1")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/client-business-name"))

  def getClientAddress =
    http("Get Client address page")
      .get(s"$baseUrl$route/client-address")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postClientAddress =
    http("Enter Client Address")
      .post(s"$baseUrl$route/client-address")
      .formParam("csrfToken", "#{csrfToken}")
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
      .formParam("csrfToken", "#{csrfToken}")
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
      .formParam("csrfToken", "#{csrfToken}")
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
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", tradingName)
      .check(status.in(303))
      .check(header("Location").is(s"$route/add-uk-trading-name"))

  def getAddTradingName =
    http("Get Add Trading Name page")
      .get(s"$baseUrl$route/add-uk-trading-name")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testAddTradingName(answer: Boolean) =
    http("Add Trading Name")
      .post(s"$baseUrl$route/add-uk-trading-name")
      .formParam("csrfToken", "#{csrfToken}")
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
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-country/$index"))

  def getPreviousCountry(index: Int) =
    http("Get previous country page")
      .get(s"$baseUrl$route/previous-country/$index")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postPreviousCountry(countryIndex: Int, schemeIndex: Int, countryCode: String) =
    http("Enter previous country")
      .post(s"$baseUrl$route/previous-country/$countryIndex")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", countryCode)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-scheme/$countryIndex/$schemeIndex"))

  def getPreviousScheme(countryIndex: Int, schemeIndex: Int) =
    http("Get Previous Scheme page")
      .get(s"$baseUrl$route/previous-scheme/$countryIndex/$schemeIndex")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testPreviousScheme(countryIndex: Int, schemeIndex: Int, schemeType: String) =
    http("Answer Previous Scheme")
      .post(s"$baseUrl$route/previous-scheme/$countryIndex/$schemeIndex")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", schemeType)
      .check(status.in(200, 303))

  def postPreviousScheme(countryIndex: Int, schemeIndex: Int, schemeType: String) =
    if (schemeType == "oss") {
      testPreviousScheme(countryIndex, schemeIndex, schemeType)
        .check(header("Location").is(s"$route/previous-oss-scheme-number/$countryIndex/$schemeIndex"))
    } else {
      testPreviousScheme(countryIndex, schemeIndex, schemeType)
        .check(header("Location").is(s"$route/previous-scheme-intermediary/$countryIndex/$schemeIndex"))
    }

  def getPreviousOssSchemeNumber(countryIndex: Int, schemeIndex: Int) =
    http("Get Previous Oss Scheme number page")
      .get(s"$baseUrl$route/previous-oss-scheme-number/$countryIndex/$schemeIndex")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postPreviousOssSchemeNumber(countryIndex: Int, schemeIndex: Int, registrationNumber: String) =
    http("Enter Previous Oss Scheme Number")
      .post(s"$baseUrl$route/previous-oss-scheme-number/$countryIndex/$schemeIndex")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", registrationNumber)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-scheme-answers/$countryIndex"))

  def getPreviousSchemeAnswers(index: Int) =
    http("Get Previous Scheme Answers page")
      .get(s"$baseUrl$route/previous-scheme-answers/$index")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postPreviousSchemeAnswers(index: Int, answer: Boolean) =
    http("Post Previous Scheme Answers page")
      .post(s"$baseUrl$route/previous-scheme-answers/$index")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", answer)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-schemes-overview"))

  def getPreviousSchemesOverview =
    http("Get Previous Schemes Overview page")
      .get(s"$baseUrl$route/previous-schemes-overview")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testPreviousSchemesOverview(answer: Boolean) =
    http("Previous Schemes Overview")
      .post(s"$baseUrl$route/previous-schemes-overview?incompletePromptShown=false")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", answer)
      .check(status.in(200, 303))

  def postPreviousSchemesOverview(answer: Boolean, index: Option[Int]) =
    if (answer) {
      testPreviousSchemesOverview(answer)
        .check(header("Location").is(s"$route/previous-country/${index.get}"))
    } else {
      testPreviousSchemesOverview(answer)
        .check(header("Location").is(s"$route/eu-fixed-establishment"))
    }

  def getPreviousSchemeIntermediary(countryIndex: Int, schemeIndex: Int) =
    http("Get Previous Scheme Intermediary page")
      .get(s"$baseUrl$route/previous-scheme-intermediary/$countryIndex/$schemeIndex")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postPreviousSchemeIntermediary(countryIndex: Int, schemeIndex: Int) =
    http("Post Previous Scheme Intermediary page")
      .post(s"$baseUrl$route/previous-scheme-intermediary/$countryIndex/$schemeIndex")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", false)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-ioss-number/$countryIndex/$schemeIndex"))

  def getPreviousIossNumber(countryIndex: Int, schemeIndex: Int) =
    http("Get Previous IOSS number page")
      .get(s"$baseUrl$route/previous-ioss-number/$countryIndex/$schemeIndex")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postPreviousIossNumber(countryIndex: Int, schemeIndex: Int, iossNumber: String) =
    http("Previous IOSS Number")
      .post(s"$baseUrl$route/previous-ioss-number/$countryIndex/$schemeIndex")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", iossNumber)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/previous-scheme-answers/$countryIndex"))

  def getEuFixedEstablishment =
    http("Get Eu Fixed Establishment page")
      .get(s"$baseUrl$route/eu-fixed-establishment")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postEuFixedEstablishment(index: Int) =
    http("Answer Eu Fixed Establishment")
      .post(s"$baseUrl$route/eu-fixed-establishment")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "true")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/vat-registered-eu-country/$index"))

  def getVatRegisteredEuCountry(index: Int) =
    http("Get Vat Registered Eu Country page")
      .get(s"$baseUrl$route/vat-registered-eu-country/$index")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postVatRegisteredEuCountry(index: Int, countryCode: String) =
    http("Enter Vat Registered Eu Country State")
      .post(s"$baseUrl$route/vat-registered-eu-country/$index")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", countryCode)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/trading-name-business-address/$index"))

  def getTradingNameBusinessAddress(index: Int) =
    http("Get Trading Name Business Address page")
      .get(s"$baseUrl$route/trading-name-business-address/$index")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postTradingNameBusinessAddress(index: Int) =
    http("Enter Trading Name Business Address")
      .post(s"$baseUrl$route/trading-name-business-address/$index")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("tradingName", "Trading Name")
      .formParam("line1", "1 Street Name")
      .formParam("line2", "Suburb")
      .formParam("townOrCity", "City")
      .formParam("postCode", "ABC123")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/registration-tax-type/$index"))

  def getRegistrationType(index: Int) =
    http("Get Registration Type page")
      .get(s"$baseUrl$route/registration-tax-type/$index")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testRegistrationType(index: Int, registrationType: String) =
    http("Answer Registration Type Page")
      .post(s"$baseUrl$route/registration-tax-type/$index")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", registrationType)
      .check(status.in(200, 303))

  def postRegistrationType(index: Int, registrationType: String) =
    if (registrationType == "vatNumber") {
      testRegistrationType(index, registrationType)
        .check(header("Location").is(s"$route/eu-vat-number/$index"))
    } else {
      testRegistrationType(index, registrationType)
        .check(header("Location").is(s"$route/eu-tax-identification-number/$index"))
    }

  def getEuVatNumber(index: Int) =
    http("Get EU VAT Number page")
      .get(s"$baseUrl$route/eu-vat-number/$index")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postEuVatNumber(index: Int, euVatNumber: String) =
    http("Enter EU VAT Number")
      .post(s"$baseUrl$route/eu-vat-number/$index")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", euVatNumber)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/check-tax-details/$index"))

  def getEuTaxReference(index: Int) =
    http("Get EU Tax Reference page")
      .get(s"$baseUrl$route/eu-tax-identification-number/$index")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postEuTaxReference(index: Int, taxReference: String) =
    http("Enter EU Tax Reference")
      .post(s"$baseUrl$route/eu-tax-identification-number/$index")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", taxReference)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/check-tax-details/$index"))

  def getCheckTaxDetails(index: Int) =
    http("Get Check Tax Details page")
      .get(s"$baseUrl$route/check-tax-details/$index")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postCheckTaxDetails(index: Int) =
    http("Submit Check EU VAT Details")
      .post(s"$baseUrl$route/check-tax-details/$index?incompletePromptShown=false")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/add-tax-details"))

  def getAddTaxDetails =
    http("Get Add VAT Details page")
      .get(s"$baseUrl$route/add-tax-details")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testAddTaxDetails(answer: Boolean) =
    http("Answer Add EU VAT Details")
      .post(s"$baseUrl$route/add-tax-details?incompletePromptShown=false")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", answer)
      .check(status.in(200, 303))

  def postAddTaxDetails(answer: Boolean, index: Option[Int]) =
    if (answer) {
      testAddTaxDetails(answer)
        .check(header("Location").is(s"$route/vat-registered-eu-country/${index.get}"))
    } else {
      testAddTaxDetails(answer)
        .check(header("Location").is(s"$route/website-address/1"))
    }

  def getWebsite(index: Int) =
    http(s"Get Website page $index")
      .get(s"$baseUrl$route/website-address/$index")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postWebsite(index: Int, website: String) =
    http(s"Enter website $index")
      .post(s"$baseUrl$route/website-address/$index")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", website)
      .check(status.in(303))
      .check(header("Location").is(s"$route/add-website-address"))

  def getAddWebsite =
    http("Get Add Website page")
      .get(s"$baseUrl$route/add-website-address")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testAddWebsite(answer: Boolean) =
    http("Add Website")
      .post(s"$baseUrl$route/add-website-address")
      .formParam("csrfToken", "#{csrfToken}")
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
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postBusinessContactDetails =
    http("Enter Business Contact Details")
      .post(s"$baseUrl$route/business-contact-details")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("fullName", "Trader Name")
      .formParam("telephoneNumber", "012301230123")
      .formParam("emailAddress", "trader@testemail.com")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/check-your-answers"))

  def getCheckYourAnswers =
    http("Get Check Your Answers page")
      .get(s"$baseUrl$route/check-your-answers")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postCheckYourAnswers =
    http("Post Check Your Answers page")
      .post(s"$baseUrl$route/check-your-answers/false?waypoints=check-your-answers")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/declaration"))

  def getDeclaration =
    http("Get Declaration page")
      .get(s"$baseUrl$route/declaration")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postDeclaration =
    http("Post Declaration page")
      .post(s"$baseUrl$route/declaration")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("declaration", "true")
      .check(status.in(303))
      .check(header("Location").is(s"$route/client-application-complete"))

  def getClientApplicationComplete =
    http("Get Client Application Complete page")
      .get(s"$baseUrl$route/client-application-complete")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css("a", "id").saveAs("codeId"))
      .check(status.in(200))

  def postAuthorityWizardClient =
    http("Authority Wizard for Client Login")
      .post(loginUrl + s"/auth-login-stub/gg-sign-in")
      .formParam("authorityId", "")
      .formParam("gatewayToken", "")
      .formParam("credentialStrength", "strong")
      .formParam("confidenceLevel", "50")
      .formParam("affinityGroup", "Organisation")
      .formParam("email", "user@test.com")
      .formParam("credentialRole", "User")
      .formParam("redirectionUrl", baseUrl + route + "/client-code-start/#{codeId}")
      .check(status.in(200, 303))
      .check(headerRegex("Set-Cookie", """mdtp=(.*)""").saveAs("mdtpCookie"))
      .check(header("Location").is(s"$baseUrl$route/client-code-start/#{codeId}"))

  def getCodeStart =
    http("Enter Activation Code")
      .get(s"$baseUrl$route/client-code-start/#{codeId}")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/client-code-entry/#{codeId}"))

  def getUniqueCodeTestOnly =
    http("Trigger test-only endpoint")
      .get(s"$baseUrl$route/test-only/get-client-code/#{codeId}")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(status.in(200, 303))
      .check(css("div", "id").saveAs("activationCode"))

  def getClientCodeEntry =
    http("Get Activation Code")
      .get(s"$baseUrl$route/client-code-entry/#{codeId}")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200, 303))

  def postClientCodeEntry =
    http("Post Activation Code")
      .post(s"$baseUrl$route/client-code-entry/#{codeId}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "#{activationCode}")
      .check(status.in(303))
      .check(header("Location").is(s"$route/declaration-client"))

  def getClientDeclaration =
    http("Get Client Declaration")
      .get(s"$baseUrl$route/declaration-client")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200, 303))

  def postClientDeclaration =
    http("Post Client Declaration")
      .post(s"$baseUrl$route/declaration-client")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("declaration", "true")
      .check(status.in(303))
      .check(header("Location").is(s"$route/successful-registration"))

  def getSuccessfulRegistration =
    http("Get Successful Registration")
      .get(s"$baseUrl$route/successful-registration")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(status.in(200))

}
