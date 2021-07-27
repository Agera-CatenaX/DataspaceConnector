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
package io.dataspaceconnector.model.app;

import io.dataspaceconnector.model.endpoint.AppEndpoint;
import io.dataspaceconnector.model.named.NamedEntity;
import io.dataspaceconnector.model.util.UriConverter;
import io.dataspaceconnector.service.usagecontrol.PolicyPattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.Version;

import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Column;
import java.net.URI;
import java.util.List;

import static io.dataspaceconnector.model.config.DatabaseConstants.URI_COLUMN_LENGTH;

/**
 * Data app, which is distributed via the App Store and can be deployed inside the Connector.
 */
@javax.persistence.Entity
@Table(name = "app")
@SQLDelete(sql = "UPDATE app SET deleted=true WHERE id=?")
@Where(clause = "deleted = false")
@Getter
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class App extends NamedEntity {

    /**
     * Serial version uid.
     **/
    private static final long serialVersionUID = 1L;

    //App attributes

    /**
     * Text documentation of the data app.
     */
    private String appDocumentation;

    /**
     * Endpoints provided by the data app.
     */
    @OneToMany
    private List<AppEndpoint> appEndpoints;

    /**
     * Environment variables of the data app.
     */
    private String appEnvironmentVariables;

    /**
     * Storage configuration of the data app (e.g. path in the file system or volume name).
     */
    private String appStorageConfiguration;

    /**
     * Usage policy patterns supported by the data app.
     */
    @ElementCollection
    private List<PolicyPattern> supportedUsagePolicies;

    //Resource attributes

    /**
     * The keywords of the resource.
     */
    @ElementCollection
    private List<String> keywords;

    /**
     * The publisher of the resource.
     */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI publisher;

    /**
     * The owner of the resource.
     */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI sovereign;

    /**
     * The language of the resource.
     */
    private String language;

    /**
     * The license of the resource.
     */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI license;

    /**
     * The endpoint of the resource.
     */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI endpointDocumentation;

    /**
     * The version of the resource.
     */
    @Version
    private long version;

    //Representation attributes

    /**
     * Distribution service, where the represented app can be downloaded.
     */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI dataAppDistributionService;

    /**
     * "Runtime environment of a data app, e.g., software (or hardware) required to run the app.
     */
    private String dataAppRuntimeEnvironment;

    //Artifact attributes

    /**
     * The artifact id on provider side.
     */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI remoteId;

    /**
     * The provider's address for artifact request messages.
     */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI remoteAddress;
}
