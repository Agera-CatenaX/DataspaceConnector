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
package io.dataspaceconnector.service;

import io.dataspaceconnector.exception.MessageException;
import io.dataspaceconnector.exception.MessageResponseException;
import io.dataspaceconnector.service.message.type.DescriptionRequestService;
import io.dataspaceconnector.exception.UnexpectedResponseException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.PersistenceException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Downloads metadata via IDS.
 */
@Component
@RequiredArgsConstructor
public class MetadataDownloader {

    /**
     * Service for description request message handling.
     */
    private final @NonNull DescriptionRequestService descReqSvc;

    /**
     * Service for persisting entities.
     */
    private final @NonNull EntityPersistenceService persistenceSvc;

    /**
     * Download metadata from another connector.
     *
     * @param recipient The recipient connector.
     * @param resources The resources.
     * @param artifacts The artifacts.
     * @param download  If auto-downloading is enabled.
     * @throws UnexpectedResponseException if the response type is not as expected.
     * @throws MessageResponseException    if the response is invalid.
     * @throws PersistenceException        if the data could not be persisted.
     * @throws MessageException            if message handling failed.
     */
    public void download(final URI recipient, final List<URI> resources,
                         final List<URI> artifacts, final boolean download)
            throws UnexpectedResponseException, PersistenceException, MessageResponseException,
            MessageException {
        Map<String, String> response;
        for (final var resource : resources) {
            response = descReqSvc.sendMessage(recipient, resource);
            persistenceSvc.saveMetadata(response, artifacts, download, recipient);
        }
    }

    /**
     * @param recipient The recipient connector.
     * @param appResource The app resource.
     * @throws UnexpectedResponseException if the response type is not as expected.
     */
    public void downloadAppResource(final URI recipient,
                                    final URI appResource) throws UnexpectedResponseException {
        Map<String, String> response;
        response = descReqSvc.sendMessage(recipient, appResource);
        persistenceSvc.saveAppResource(response, recipient);
    }
}
