/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dataspaceconnector.controller.message.ids.validator;

import de.fraunhofer.iais.eis.ContractRequest;
import io.dataspaceconnector.common.routing.ParameterUtils;
import io.dataspaceconnector.controller.message.ids.validator.base.IdsValidator;
import io.dataspaceconnector.service.message.handler.dto.Response;
import io.dataspaceconnector.service.usagecontrol.ContractManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/**
 * Compares a received contract agreement to the initial contract request.
 */
@Component("ContractAgreementValidator")
@RequiredArgsConstructor
public class ContractAgreementValidator extends IdsValidator {

    /**
     * Service for managing contracts.
     */
    private final @NonNull ContractManager contractManager;

    /**
     * Compares the contract agreement to the contract request.
     * @param exchange the exchange.
     */
    @Override
    protected void processInternal(final Exchange exchange) {
        final var contractRequest = exchange
                .getProperty(ParameterUtils.CONTRACT_REQUEST_PARAM, ContractRequest.class);
        final var agreementString = exchange.getIn().getBody(Response.class).getBody();

        final var agreement = contractManager
                .validateContractAgreement(agreementString, contractRequest);

        exchange.setProperty(ParameterUtils.CONTRACT_AGREEMENT_PARAM, agreement);
    }

}
