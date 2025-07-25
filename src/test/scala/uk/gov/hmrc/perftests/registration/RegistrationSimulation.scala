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

import uk.gov.hmrc.performance.simulation.PerformanceTestRunner
import uk.gov.hmrc.perftests.registration.RegistrationRequests._

class RegistrationSimulation extends PerformanceTestRunner {

  setup("registrationUkVrn", "IOSS NETP Registration Journey - UK Based VRN") withRequests
    (
      getAuthorityWizard,
      postAuthorityWizard,
      getClientUkBased,
      postClientUkBased(true),
      getClientHasVatNumber,
      postClientHasVatNumber(true, true),
      getClientVatNumber,
      postClientVatNumber,
      getConfirmVatDetails,
      postConfirmVatDetails,
      getHaveUkTradingName,
      postHaveUkTradingName,
      getUkTradingName(1),
      postUkTradingName(1, "1st trading name"),
      getAddTradingName,
      postAddTradingName(true, Some(2)),
      getUkTradingName(2),
      postUkTradingName(2, "another-trading-name"),
      getAddTradingName,
      postAddTradingName(false, None),
      postPreviousOss(1),
      getPreviousCountry(1),
      postPreviousCountry(1, 1, "CY"),
      getPreviousScheme(1, 1),
      postPreviousScheme(1, 1, "oss"),
      getPreviousOssSchemeNumber(1, 1),
      postPreviousOssSchemeNumber(1, 1, "CY11145678X"),
      getPreviousSchemeAnswers(1),
      postPreviousSchemeAnswers(1, false),
      getPreviousSchemesOverview,
      postPreviousSchemesOverview(true, Some(2)),
      getPreviousCountry(2),
      postPreviousCountry(2, 1, "SI"),
      getPreviousScheme(2, 1),
      postPreviousScheme(2, 1, "oss"),
      getPreviousOssSchemeNumber(2, 1),
      postPreviousOssSchemeNumber(2, 1, "SI44332211"),
      getPreviousSchemeAnswers(2),
      postPreviousSchemeAnswers(2, false),
      getPreviousSchemesOverview,
      postPreviousSchemesOverview(true, Some(3)),
      getPreviousCountry(3),
      postPreviousCountry(3, 1, "MT"),
      getPreviousScheme(3, 1),
      postPreviousScheme(3, 1, "ioss"),
      getPreviousSchemeIntermediary(3, 1),
      postPreviousSchemeIntermediary(3, 1),
      getPreviousIossNumber(3, 1),
      postPreviousIossNumber(3, 1, "IM4707744112"),
      getPreviousSchemeAnswers(3),
      postPreviousSchemeAnswers(3, false),
      getPreviousSchemesOverview,
      postPreviousSchemesOverview(false, None),
      getEuFixedEstablishment,
      postEuFixedEstablishment(1),
      getVatRegisteredEuCountry(1),
      postVatRegisteredEuCountry(1, "AT"),
      getTradingNameBusinessAddress(1),
      postTradingNameBusinessAddress(1),
      getRegistrationType(1),
      postRegistrationType(1, "vatNumber"),
      getEuVatNumber(1),
      postEuVatNumber(1, "ATU88882211"),
      getCheckTaxDetails(1),
      postCheckTaxDetails(1),
      getAddTaxDetails,
      postAddTaxDetails(true, Some(2)),
      getVatRegisteredEuCountry(2),
      postVatRegisteredEuCountry(2, "NL"),
      getTradingNameBusinessAddress(2),
      postTradingNameBusinessAddress(2),
      getRegistrationType(2),
      postRegistrationType(2, "taxId"),
      getEuTaxReference(2),
      postEuTaxReference(2, "NL12345678ABC"),
      getCheckTaxDetails(2),
      postCheckTaxDetails(2),
      getAddTaxDetails,
      postAddTaxDetails(false, None),
      getWebsite(1),
      postWebsite(1, "www.websiteone.com"),
      getAddWebsite,
      postAddWebsite(true, Some(2)),
      getWebsite(2),
      postWebsite(2, "www.anotherwebsite.com"),
      getAddWebsite,
      postAddWebsite(false, None),
      getBusinessContactDetails,
      postBusinessContactDetails,
      getCheckYourAnswers,
      postCheckYourAnswers,
      getDeclaration,
      postDeclaration,
      getClientApplicationComplete
    )

  setup("registrationNonUk", "IOSS NETP Registration Journey - Non-UK based") withRequests
    (
      getAuthorityWizard,
      postAuthorityWizard,
      getClientUkBased,
      postClientUkBased(false),
      getClientHasVatNumber,
      postClientHasVatNumber(false, false),
      getClientCountryBased,
      postClientCountryBased,
      getClientTaxReference,
      postClientTaxReference,
      getClientBusinessName,
      postClientBusinessName,
      getClientAddress,
      postClientAddress,
      getConfirmVatDetails,
      postConfirmVatDetails,
      getHaveUkTradingName,
      postHaveUkTradingName,
      getUkTradingName(1),
      postUkTradingName(1, "trading name"),
      getAddTradingName,
      postAddTradingName(false, None),
      postPreviousOss(1),
      getPreviousCountry(1),
      postPreviousCountry(1, 1, "CY"),
      getPreviousScheme(1, 1),
      postPreviousScheme(1, 1, "oss"),
      getPreviousOssSchemeNumber(1, 1),
      postPreviousOssSchemeNumber(1, 1, "CY11145678X"),
      getPreviousSchemeAnswers(1),
      postPreviousSchemeAnswers(1, false),
      getPreviousSchemesOverview,
      postPreviousSchemesOverview(false, None),
      getEuFixedEstablishment,
      postEuFixedEstablishment(1),
      getVatRegisteredEuCountry(1),
      postVatRegisteredEuCountry(1, "AT"),
      getTradingNameBusinessAddress(1),
      postTradingNameBusinessAddress(1),
      getRegistrationType(1),
      postRegistrationType(1, "vatNumber"),
      getEuVatNumber(1),
      postEuVatNumber(1, "ATU88882211"),
      getCheckTaxDetails(1),
      postCheckTaxDetails(1),
      getAddTaxDetails,
      postAddTaxDetails(false, None),
      getWebsite(1),
      postWebsite(1, "website1.com"),
      getAddWebsite,
      postAddWebsite(true, Some(2)),
      getWebsite(2),
      postWebsite(2, "http://another-website.test"),
      getAddWebsite,
      postAddWebsite(false, None),
      getBusinessContactDetails,
      postBusinessContactDetails,
      getCheckYourAnswers,
      postCheckYourAnswers,
      getDeclaration,
      postDeclaration,
      getClientApplicationComplete
    )

  runSimulation()
}
