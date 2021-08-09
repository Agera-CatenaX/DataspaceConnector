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
import io.dataspaceconnector.exception.ResourceNotFoundException;
import io.dataspaceconnector.exception.UnexpectedResponseException;
import io.dataspaceconnector.service.message.type.ArtifactRequestService;
import io.dataspaceconnector.service.resource.AgreementService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Downloads artifacts data.
 */
@Log4j2
@RequiredArgsConstructor
@Component
public class ArtifactDataDownloader {

    /**
     * Service for artifact request message handling.
     */
    private final @NonNull ArtifactRequestService artifactReqSvc;

    /**
     * Used for gaining access to agreements.
     */
    private final @NonNull AgreementService agreementService;

    /**
     * Service for persisting entities.
     */
    private final @NonNull EntityPersistenceService persistenceSvc;

    /**
     * Download artifact data.
     * @param recipient   The provider connector.
     * @param artifacts   The artifact whose data should be downloaded.
     * @param agreementId The agreement allowing the transfer.
     * @throws UnexpectedResponseException if the response type is not as expected.
     * @throws MessageResponseException    if the response is invalid.
     * @throws MessageException            if message handling failed.
     */
    public void download(final URI recipient, final List<URI> artifacts, final UUID agreementId)
            throws UnexpectedResponseException, MessageResponseException, MessageException {
        // Iterate over list of resource ids to send artifact request messages for each.
        for (final var artifact : artifacts) {
            // Send and validate artifact request/response message.
            final var response = artifactReqSvc.sendMessage(recipient, artifact,
                    agreementService.get(agreementId).getRemoteId());

            // Read and process the response message.
            try {
                persistenceSvc.saveData(response, artifact);
            } catch (IOException | ResourceNotFoundException
                    | MessageResponseException e) {
                // Ignore that the data saving failed. Another try can take place later.
                if (log.isWarnEnabled()) {
                    log.warn("Could not save data for artifact. [artifact=({}), "
                            + "exception=({})]", artifact, e.getMessage());
                }
                throw new PersistenceException(e);
            }
        }
    }

    /**
     * Request the App Artifact and update the app.
     * @param recipient The app store where the artifact shall be downloaded.
     * @param app The app artifact id.
     * @throws UnexpectedResponseException When response is not an ArtifactResponseMessage.
     */
    public void downloadAppArtifact(final URI recipient, final URI app)
            throws UnexpectedResponseException {
        var response = artifactReqSvc.sendMessage(recipient, app, null);

        // Read and process the response message.
        try {
            persistenceSvc.saveAppData(response, app);
        } catch (IOException | ResourceNotFoundException
                | MessageResponseException e) {
            // Ignore that the data saving failed. Another try can take place later.
            if (log.isWarnEnabled()) {
                log.warn("Could not save data for app artifact. [app artifact=({}), "
                        + "exception=({})]", app, e.getMessage());
            }
            throw new PersistenceException(e);
        }
    }
}
