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
package io.dataspaceconnector.service.message.builder.type;

import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.DescriptionResponseMessageImpl;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import io.dataspaceconnector.common.exception.ErrorMessage;
import io.dataspaceconnector.common.exception.MessageException;
import io.dataspaceconnector.common.exception.MessageResponseException;
import io.dataspaceconnector.common.exception.UnexpectedResponseException;
import io.dataspaceconnector.common.util.Utils;
import io.dataspaceconnector.model.message.DescriptionRequestMessageDesc;
import io.dataspaceconnector.service.message.builder.type.base.AbstractMessageService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;

/**
 * Message service for ids description request messages.
 */
@Service
public final class DescriptionRequestService
        extends AbstractMessageService<DescriptionRequestMessageDesc> {

    /**
     * @throws IllegalArgumentException     if desc is null.
     * @throws ConstraintViolationException if security tokes is null or another error appears
     *                                      when building the message.
     */
    @Override
    public Message buildMessage(final DescriptionRequestMessageDesc desc)
            throws ConstraintViolationException {
        Utils.requireNonNull(desc, ErrorMessage.DESC_NULL);

        final var connectorId = getConnectorService().getConnectorId();
        final var modelVersion = getConnectorService().getOutboundModelVersion();
        final var token = getConnectorService().getCurrentDat();

        final var recipient = desc.getRecipient();
        final var elementId = desc.getRequestedElement();

        final var message = new DescriptionRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(modelVersion)
                ._issuerConnector_(connectorId)
                ._senderAgent_(connectorId)
                ._requestedElement_(elementId)
                ._securityToken_(token)
                ._recipientConnector_(Util.asList(recipient))
                .build();
        message.setProperty("ids:depth", "10");

        return message;
    }

    @Override
    protected Class<?> getResponseMessageType() {
        return DescriptionResponseMessageImpl.class;
    }

    /**
     * Build and send a description request message and then validate the response.
     *
     * @param recipient The recipient.
     * @param elementId The requested element.
     * @return The response map.
     * @throws MessageException            if message handling failed.
     * @throws MessageResponseException    if the response could not be processed.
     * @throws UnexpectedResponseException if the response is not as expected.
     */
    public Map<String, String> sendMessage(final URI recipient, final URI elementId)
            throws MessageException, MessageResponseException, UnexpectedResponseException {
        final var desc = new DescriptionRequestMessageDesc(recipient, elementId);
        final var response = send(desc, "");
        try {
            if (!validateResponse(response)) {
                throw new UnexpectedResponseException(getResponseContent(response));
            }
        } catch (MessageResponseException e) {
            throw new UnexpectedResponseException(getResponseContent(response), e);
        }

        return response;
    }

    /**
     * Check if the response message is of type description response.
     *
     * @param response The response as map.
     * @return True if the response type is as expected.
     * @throws MessageResponseException If the response could not be read.
     */
    public boolean validateResponse(final Map<String, String> response)
            throws MessageResponseException {
        return isValidResponseType(response);
    }
}
