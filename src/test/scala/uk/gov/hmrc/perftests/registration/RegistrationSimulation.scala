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
      postClientHasVatNumber,
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
      getClientCountryBased,
      postClientCountryBased,
      getClientCountryBased,
      postClientCountryBased,
      getClientBusinessName,
      postClientBusinessName,
      getClientAddress,
      postClientAddress,
      getConfirmVatDetails,
      postConfirmVatDetailsContinue,
      getHaveUkTradingName,
      postHaveUkTradingName,
      getUkTradingName(1),
      postUkTradingName(1, "trading name"),
      getAddTradingName,
      postAddTradingName(false, None),
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
