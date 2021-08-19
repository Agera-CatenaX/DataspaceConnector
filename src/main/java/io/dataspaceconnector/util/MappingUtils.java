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
package io.dataspaceconnector.util;

import de.fraunhofer.iais.eis.AppEndpoint;
import de.fraunhofer.iais.eis.AppRepresentation;
import de.fraunhofer.iais.eis.AppResource;
import de.fraunhofer.iais.eis.Artifact;
import de.fraunhofer.iais.eis.Catalog;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorEndpoint;
import de.fraunhofer.iais.eis.ConnectorEndpointImpl;
import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.Proxy;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.Rule;
import de.fraunhofer.iais.eis.UsagePolicyClass;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import io.dataspaceconnector.model.app.AppDesc;
import io.dataspaceconnector.model.artifact.ArtifactDesc;
import io.dataspaceconnector.model.auth.AuthenticationDesc;
import io.dataspaceconnector.model.catalog.CatalogDesc;
import io.dataspaceconnector.model.configuration.ConfigurationDesc;
import io.dataspaceconnector.model.configuration.DeployMode;
import io.dataspaceconnector.model.configuration.LogLevel;
import io.dataspaceconnector.model.configuration.SecurityProfile;
import io.dataspaceconnector.model.contract.ContractDesc;
import io.dataspaceconnector.model.endpoint.AppEndpointDesc;
import io.dataspaceconnector.model.keystore.KeystoreDesc;
import io.dataspaceconnector.model.proxy.ProxyDesc;
import io.dataspaceconnector.model.representation.RepresentationDesc;
import io.dataspaceconnector.model.resource.OfferedResourceDesc;
import io.dataspaceconnector.model.resource.RequestedResourceDesc;
import io.dataspaceconnector.model.resource.ResourceDesc;
import io.dataspaceconnector.model.rule.ContractRuleDesc;
import io.dataspaceconnector.model.template.AppEndpointTemplate;
import io.dataspaceconnector.model.template.AppTemplate;
import io.dataspaceconnector.model.template.ArtifactTemplate;
import io.dataspaceconnector.model.template.CatalogTemplate;
import io.dataspaceconnector.model.template.ContractTemplate;
import io.dataspaceconnector.model.template.RepresentationTemplate;
import io.dataspaceconnector.model.template.ResourceTemplate;
import io.dataspaceconnector.model.template.RuleTemplate;
import io.dataspaceconnector.model.truststore.TruststoreDesc;
import io.dataspaceconnector.service.usagecontrol.PolicyPattern;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Maps ids resources to internal resources.
 */
public final class MappingUtils {

    /**
     * Default constructor.
     */
    private MappingUtils() {
        // not used
    }

    /**
     * Map ids catalog to connector catalog.
     * @param catalog The ids catalog.
     * @return The connector catalog.
     * @throws IllegalArgumentException if the passed resource is null.
     */
    public static CatalogTemplate fromIdsCatalog(final Catalog catalog) {
        Utils.requireNonNull(catalog, ErrorMessage.ENTITY_NULL);

        final var additional = new HashMap<String, String>();
        if (catalog.getProperties() != null) {
            catalog.getProperties().forEach((key, value) -> additional.put(key, value.toString()));
        }

        final var catalogDesc = new CatalogDesc();
        catalogDesc.setAdditional(additional);
        catalogDesc.setTitle("IDS Catalog");
        catalogDesc.setDescription("This catalog is created from an IDS infomodel catalog.");
        catalogDesc.setBootstrapId(catalog.getId());

        return new CatalogTemplate(catalogDesc, null, null);
    }

    /**
     * Map ids app resource to resource.
     * @param resource  The app resource.
     * @param remoteUrl The recipient id.
     * @return app template.
     */
    public static AppTemplate fromIdsApp(final AppResource resource,
                                         final URI remoteUrl) {
        Utils.requireNonNull(resource, ErrorMessage.ENTITY_NULL);
        final var appDesc = new AppDesc();
        final var endpoints = new ArrayList<AppEndpointTemplate>();

        //Set app description fields
        appDesc.setRemoteAddress(remoteUrl);
        if (resource.getKeyword() != null) {
            appDesc.setKeywords(resource.getKeyword()
                    .stream()
                    .map(TypedLiteral::toString).collect(Collectors.toList()));
        }
        appDesc.setLicense(resource.getStandardLicense());
        appDesc.setPublisher(resource.getPublisher());
        appDesc.setRemoteId(resource.getId());
        appDesc.setSovereign(resource.getSovereign());
        appDesc.setBootstrapId(resource.getId());

        //set representation fields
        if (resource.getRepresentation() != null) {
            var appRepresentations = resource.getRepresentation().stream()
                    .filter(x -> x instanceof AppRepresentation)
                    .map(x -> (AppRepresentation) x)
                    .collect(Collectors.toList());
            if (!appRepresentations.isEmpty()) {
                final var representation = appRepresentations.get(0);
                appDesc.setAdditional(buildAdditionalForRepresentation(representation));
                appDesc.setLanguage(String.valueOf(representation.getLanguage()));
                appDesc.setDataAppDistributionService(
                        representation.getDataAppDistributionService()
                );
                appDesc.setDataAppRuntimeEnvironment(representation.getDataAppRuntimeEnvironment());

                if (representation.getDataAppInformation() != null) {
                    //set dataApp fields
                    final var dataApp = representation.getDataAppInformation();
                    appDesc.setAppDocumentation(dataApp.getAppDocumentation());
                    appDesc.setAppEnvironmentVariables(dataApp.getAppEnvironmentVariables());
                    appDesc.setAppStorageConfiguration(dataApp.getAppStorageConfiguration());
                    if (dataApp.getSupportedUsagePolicies() != null) {
                        appDesc.setSupportedUsagePolicies(
                                dataApp.getSupportedUsagePolicies().stream()
                                        .map(MappingUtils::fromIdsUsagePolicyClass)
                                        .collect(Collectors.toList())
                        );
                    }
                    for (final var endpoint : dataApp.getAppEndpoint()) {
                        endpoints.add(fromIdsAppEndpoint(endpoint));
                    }

                }
            }
        }
        return new AppTemplate(appDesc, endpoints);
    }

    private static Map<String, String> buildAdditionalForRepresentation(
            final AppRepresentation representation) {
        final var additional = propertiesToAdditional(representation.getProperties());

        final var dataAppInformation = representation.getDataAppInformation();
        final var instance = representation.getInstance();
        final var language = representation.getLanguage();
        final var mediaType = representation.getMediaType();
        final var representationStandard = representation.getRepresentationStandard();
        final var shapesGraph = representation.getShapesGraph();

        //if (dataAppInformation != null) {
        //    additional.put("ids:dataAppInformation", dataAppInformation.toRdf());
        //}
        if (instance != null) {
            addListToAdditional(instance, additional, "ids:instance");
        }
        if (language != null) {
            additional.put("ids:language", language.toString());
        }
        if (mediaType != null) {
            additional.put("ids:mediaType", mediaType.getFilenameExtension());
        }
        if (representationStandard != null) {
            additional.put("ids:representationStandard", representationStandard.toString());
        }
        if (shapesGraph != null) {
            additional.put("ids:shapesGraph", shapesGraph.toString());
        }

        return additional;
    }


    private static void setResourceEndpoint(final AppResource resource, final AppDesc appDesc) {
        if (resource.getResourceEndpoint() == null || resource.getResourceEndpoint().isEmpty()) {
            appDesc.setEndpointDocumentation(null);
        } else {
            if (resource.getResourceEndpoint().get(0).getEndpointDocumentation() == null
                    || resource.getResourceEndpoint().get(0).getEndpointDocumentation().isEmpty()) {
                appDesc.setEndpointDocumentation(null);
            } else {
                appDesc.setEndpointDocumentation(
                        resource.getResourceEndpoint().get(0).getEndpointDocumentation().get(0));
            }
        }
    }

    private static PolicyPattern fromIdsUsagePolicyClass(final UsagePolicyClass policyClass) {
        switch (policyClass) {
            case CONNECTOR_RESTRICTED_DATA_USAGE:
                return PolicyPattern.CONNECTOR_RESTRICTED_USAGE;
            case DURATION_RESTRICTED_DATA_USAGE:
                return PolicyPattern.DURATION_USAGE;
            case INTERVAL_RESTRICTED_DATA_USAGE:
                return PolicyPattern.USAGE_DURING_INTERVAL;
            case LOCAL_LOGGING:
                return PolicyPattern.USAGE_LOGGING;
            case ALLOW_DATA_USAGE:
                return PolicyPattern.PROVIDE_ACCESS;
            case REMOTE_NOTIFICATION:
                return PolicyPattern.USAGE_NOTIFICATION;
            case RESTRICTED_NUMBER_OF_USAGES:
                return PolicyPattern.N_TIMES_USAGE;
            case USE_DATA_AND_DELETE_AFTER:
                return PolicyPattern.USAGE_UNTIL_DELETION;
            default:
                return PolicyPattern.PROHIBIT_ACCESS;
        }
    }

    private static AppEndpointTemplate fromIdsAppEndpoint(final AppEndpoint appEndpoint) {
        final var appEndpointDesc = new AppEndpointDesc();

        appEndpointDesc.setAdditional(buildAdditionalForAppEndpoint(appEndpoint));
        if (appEndpoint.getAppEndpointPort() != null) {
            appEndpointDesc.setEndpointPort(appEndpoint.getAppEndpointPort().intValue());
        }
        if (appEndpoint.getAppEndpointType() != null) {
            appEndpointDesc.setEndpointType(appEndpoint.getAppEndpointType().name());
        }
        if (appEndpoint.getLanguage() != null) {
            appEndpointDesc.setLanguage(appEndpoint.getLanguage().toString());
        }
        if (appEndpoint.getAppEndpointMediaType() != null) {
            appEndpointDesc.setMediaType(
                    appEndpoint.getAppEndpointMediaType().getFilenameExtension()
            );
        }
        appEndpointDesc.setProtocol(appEndpoint.getAppEndpointProtocol());
        appEndpointDesc.setBootstrapId(appEndpoint.getId());
        appEndpointDesc.setDocs(
                appEndpoint.getEndpointDocumentation() != null
                        && !appEndpoint.getEndpointDocumentation().isEmpty()
                        ? appEndpoint.getEndpointDocumentation().get(0)
                        : null
        );
        if (appEndpoint.getEndpointInformation() != null) {
            appEndpointDesc.setInfo(appEndpoint.getEndpointInformation().toString());
        }
        appEndpointDesc.setLocation(appEndpoint.getAccessURL());

        return new AppEndpointTemplate(appEndpointDesc);
    }

    private static Map<String, String> buildAdditionalForAppEndpoint(
             final AppEndpoint appEndpoint) {
        Utils.requireNonNull(appEndpoint, ErrorMessage.ENTITY_NULL);
        final var additional = propertiesToAdditional(appEndpoint.getProperties());

        final var inboundPath = appEndpoint.getInboundPath();
        final var outboundPath = appEndpoint.getOutboundPath();
        final var language = appEndpoint.getLanguage();
        final var path = appEndpoint.getPath();

        if (inboundPath != null) {
            additional.put("ids:inboundPath", inboundPath);
        }
        if (outboundPath != null) {
            additional.put("ids:outboundPath", outboundPath);
        }
        if (language != null) {
            additional.put("ids:language", language.toString());
        }
        if (path != null) {
            additional.put("ids:path", path);
        }

        return additional;
    }


        private static Map<String, String> buildAdditionalForResource(final Resource resource) {
        Utils.requireNonNull(resource, ErrorMessage.ENTITY_NULL);

        final var additional = propertiesToAdditional(resource.getProperties());

        final var periodicity = resource.getAccrualPeriodicity();
        final var contentPart = resource.getContentPart();
        final var contentStandard = resource.getContentStandard();
        final var contentType = resource.getContentType();
        final var created = resource.getCreated();
        final var customLicense = resource.getCustomLicense();
        final var representation = resource.getDefaultRepresentation();
        final var modified = resource.getModified();
        final var resourceEndpoint = resource.getResourceEndpoint();
        final var resourcePart = resource.getResourcePart();
        final var sample = resource.getSample();
        final var shapesGraph = resource.getShapesGraph();
        final var spatialCoverage = resource.getSpatialCoverage();
        final var temporalCoverage = resource.getTemporalCoverage();
        final var temporalRes = resource.getTemporalResolution();
        final var theme = resource.getTheme();
        final var variant = resource.getVariant();
        final var version = resource.getVersion();

        if (periodicity != null) {
            additional.put("ids:accrualPeriodicity", periodicity.toRdf());
        }

        if (contentPart != null) {
            addListToAdditional(contentPart, additional, "ids:contentPart");
        }

        if (contentStandard != null) {
            additional.put("ids:contentStandard", contentStandard.toString());
        }

        if (contentType != null) {
            additional.put("ids:contentType", contentType.toRdf());
        }

        if (created != null) {
            additional.put("ids:created", created.toXMLFormat());
        }

        if (customLicense != null) {
            additional.put("ids:customLicense", customLicense.toString());
        }

        if (representation != null) {
            addListToAdditional(representation, additional, "ids:defaultRepresentation");
        }

        if (modified != null) {
            additional.put("ids:modified", modified.toXMLFormat());
        }

        if (resourceEndpoint != null) {
            addListToAdditional(resourceEndpoint, additional, "ids:resourceEndpoint");
        }

        if (resourcePart != null) {
            addListToAdditional(resourcePart, additional, "ids:resourcePart");
        }

        if (sample != null) {
            addListToAdditional(sample, additional, "ids:sample");
        }

        if (shapesGraph != null) {
            additional.put("ids:shapesGraph", shapesGraph.toString());
        }

        if (spatialCoverage != null) {
            addListToAdditional(spatialCoverage, additional, "ids:spatialCoverage");
        }

        if (temporalCoverage != null) {
            addListToAdditional(temporalCoverage, additional, "ids:temporalCoverage");
        }

        if (temporalRes != null) {
            additional.put("ids:temporalResolution", temporalRes.toString());
        }

        if (theme != null) {
            addListToAdditional(theme, additional, "ids:theme");
        }

        if (variant != null) {
            additional.put("ids:variant", variant.toString());
        }

        if (version != null) {
            additional.put("ids:version", version);
        }

        return additional;
    }

    private static void fillResourceDesc(final ResourceDesc desc, final Resource resource) {
        final var description = resource.getDescription();
        final var keywords = IdsUtils.getKeywordsAsString(resource.getKeyword());
        final var language = resource.getLanguage();
        final var publisher = resource.getPublisher();
        final var sovereign = resource.getSovereign();
        final var standardLicense = resource.getStandardLicense();
        final var title = resource.getTitle();
        final var resourceEndpoint = resource.getResourceEndpoint();

        desc.setAdditional(buildAdditionalForResource(resource));
        desc.setKeywords(keywords);
        desc.setPublisher(publisher);
        desc.setLicense(standardLicense);
        desc.setSovereign(sovereign);

        if (description != null) {
            desc.setDescription(description.size() == 1 ? description.get(0).getValue()
                    : description.toString());
        }

        if (title != null) {
            desc.setTitle(title.size() == 1 ? title.get(0).getValue() : title.toString());
        }

        if (language != null) {
            desc.setLanguage(
                    language.size() == 1 ? language.get(0).toString() : language.toString());
        }

        if (resourceEndpoint != null) {
            getFirstEndpointDocumentation(resourceEndpoint)
                    .ifPresent(desc::setEndpointDocumentation);
        }
    }

    /**
     * Map ids resource to connector resource.
     * @param resource The ids resource.
     * @return The connector resource.
     * @throws IllegalArgumentException if the passed resource is null.
     */
    public static ResourceTemplate<OfferedResourceDesc> fromIdsOfferedResource(
            final Resource resource) {
        Utils.requireNonNull(resource, ErrorMessage.ENTITY_NULL);

        final var desc = new OfferedResourceDesc();
        fillResourceDesc(desc, resource);

        return new ResourceTemplate<>(desc);
    }

    /**
     * Map ids resource to connector resource.
     * @param resource The ids resource.
     * @return The connector resource.
     * @throws IllegalArgumentException if the passed resource is null.
     */
    public static ResourceTemplate<RequestedResourceDesc> fromIdsResource(final Resource resource) {
        Utils.requireNonNull(resource, ErrorMessage.ENTITY_NULL);

        final var desc = new RequestedResourceDesc();
        desc.setRemoteId(resource.getId());
        fillResourceDesc(desc, resource);

        return new ResourceTemplate<>(desc);
    }

    /**
     * Adds the string value of a given list as an additional property. If the list only contains
     * one element, the string value will not contain brackets.
     * @param list       the list.
     * @param additional the map of additional properties.
     * @param key        the map key to use.
     */
    private static void addListToAdditional(final List<?> list,
                                            final Map<String, String> additional,
                                            final String key) {
        if (list.size() >= 1 && list.get(0) instanceof ConnectorEndpointImpl) {
            additional.put(key, ((ConnectorEndpointImpl) list.get(0)).getAccessURL().toString());
        } else {
            additional.put(key, list.size() == 1 ? list.get(0).toString() : list.toString());
        }
    }

    /**
     * Map ids representation to connector representation.
     * @param representation The ids representation.
     * @return The connector representation.
     * @throws IllegalArgumentException if the passed representation is null.
     */
    public static RepresentationTemplate fromIdsRepresentation(
            final Representation representation) {
        Utils.requireNonNull(representation, ErrorMessage.ENTITY_NULL);

        final var created = representation.getCreated();
        final var representationId = representation.getId();
        final var language = representation.getLanguage();
        final var mediaType = representation.getMediaType();
        final var modified = representation.getModified();
        final var standard = representation.getRepresentationStandard();
        final var shape = representation.getShapesGraph();

        // Add additional properties to map.
        final var additional = propertiesToAdditional(representation.getProperties());

        if (created != null) {
            additional.put("ids:created", created.toXMLFormat());
        }
        if (modified != null) {
            additional.put("ids:modified", modified.toXMLFormat());
        }

        if (shape != null) {
            additional.put("ids:shapesGraph", String.valueOf(shape));
        }

        final var desc = new RepresentationDesc();
        desc.setAdditional(additional);
        desc.setRemoteId(representationId);

        if (standard != null) {
            desc.setStandard(String.valueOf(standard));
        }

        if (language != null) {
            desc.setLanguage(language.toString());
        }

        if (mediaType != null) {
            desc.setMediaType(mediaType.getFilenameExtension());
        }

        return new RepresentationTemplate(desc);
    }

    /**
     * Build template from ids artifact.
     * @param artifact  The ids artifact.
     * @param download  Whether the artifact will be downloaded automatically.
     * @param remoteUrl The provider's url for receiving artifact request messages.
     * @return The artifact template.
     * @throws IllegalArgumentException if the passed artifact is null.
     */
    public static ArtifactTemplate fromIdsArtifact(final Artifact artifact,
                                                   final boolean download, final URI remoteUrl) {
        Utils.requireNonNull(artifact, ErrorMessage.ENTITY_NULL);

        final var artifactId = artifact.getId();
        final var byteSize = artifact.getByteSize();
        final var checksum = artifact.getCheckSum();
        final var created = artifact.getCreationDate();
        final var duration = artifact.getDuration();
        final var filename = artifact.getFileName();

        // Add additional properties to map.
        final var additional = propertiesToAdditional(artifact.getProperties());

        if (byteSize != null) {
            additional.put("ids:byteSize", byteSize.toString());
        }

        if (checksum != null) {
            additional.put("ids:checkSum", checksum);
        }

        if (created != null) {
            additional.put("ids:creationDate", created.toXMLFormat());
        }

        if (duration != null) {
            additional.put("ids:duration", duration.toString());
        }

        final var desc = new ArtifactDesc();
        desc.setAdditional(additional);
        desc.setRemoteId(artifactId);
        desc.setTitle(filename);
        desc.setAutomatedDownload(download);
        desc.setRemoteAddress(remoteUrl);
        if (artifactId != null) {
            desc.setBootstrapId(URI.create(artifactId.toString()));
        }

        return new ArtifactTemplate(desc);
    }

    /**
     * Build template from ids contract.
     * @param contract The ids contract offer.
     * @return The contract template.
     * @throws IllegalArgumentException if the passed contract is null.
     */
    public static ContractTemplate fromIdsContract(final Contract contract) {
        Utils.requireNonNull(contract, ErrorMessage.ENTITY_NULL);

        final var consumer = contract.getConsumer();
        final var date = contract.getContractDate();
        final var end = contract.getContractEnd();
        final var contractId = contract.getId();
        final var provider = contract.getProvider();
        final var start = contract.getContractStart();

        // Add additional properties to map.
        final var additional = propertiesToAdditional(contract.getProperties());

        if (date != null) {
            additional.put("ids:contractDate", date.toXMLFormat());
        }

        final var desc = new ContractDesc();
        desc.setAdditional(additional);
        desc.setConsumer(consumer);
        desc.setProvider(provider);
        desc.setRemoteId(contractId);

        if (end != null) {
            try {
                desc.setEnd(getDateOf(end.toXMLFormat()));
            } catch (DateTimeParseException ignored) {
                // Default values don't need to be set here.
            }
        }

        if (start != null) {
            try {
                desc.setStart(getDateOf(start.toXMLFormat()));
            } catch (DateTimeParseException ignored) {
                // Default values don't need to be set here.
            }
        }

        return new ContractTemplate(desc);
    }

    /**
     * Build template from ids rule.
     * @param rule The ids rule.
     * @return The rule template.
     * @throws IllegalArgumentException                            if the rule is null.
     * @throws io.dataspaceconnector.exception.RdfBuilderException if the rule cannot be
     *                                                             converted to string.
     */
    public static RuleTemplate fromIdsRule(final Rule rule) {
        Utils.requireNonNull(rule, ErrorMessage.ENTITY_NULL);

        final var value = IdsUtils.toRdf(rule);
        final var desc = new ContractRuleDesc();
        desc.setRemoteId(rule.getId());
        desc.setValue(value);

        if (rule.getTitle() != null) {
            desc.setTitle(rule.getTitle().toString());
        }

        return new RuleTemplate(desc);
    }

    /**
     * Map ids property map to additional map for the internal data model.
     * If the argument is null an empty map will be returned.
     * @param properties A string object map.
     * @return A map containing all properties that could be extracted.
     */
    private static Map<String, String> propertiesToAdditional(
            final Map<String, Object> properties) {
        final Map<String, String> additional = new ConcurrentHashMap<>();
        if (properties != null) {
            for (final Map.Entry<String, Object> entry : properties.entrySet()) {
                if (entry.getValue() != null) {
                    additional.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        return additional;
    }

    /**
     * Convert a string to a {@link ZonedDateTime}.
     * @param calendar The time as string.
     * @return The new ZonedDateTime object.
     * @throws DateTimeParseException if its not a time.
     */
    public static ZonedDateTime getDateOf(final String calendar) throws DateTimeParseException {
        return ZonedDateTime.parse(calendar);
    }

    /**
     * Returns the first endpoint documentations of the first endpoint.
     * @param endpoints The list of endpoints.
     * @return The endpoint documentation.
     */
    private static Optional<URI> getFirstEndpointDocumentation(
            final List<? extends ConnectorEndpoint> endpoints) {
        Optional<URI> output = Optional.empty();

        if (!endpoints.isEmpty()) {
            final var first = endpoints.get(0);

            if (first.getEndpointDocumentation() != null
                    && !first.getEndpointDocumentation().isEmpty()) {
                output = Optional.of(first.getEndpointDocumentation().get(0));
            }
        }

        return output;
    }

    /**
     * Get dsc log level from ids log level.
     * @param logLevel The ids log level.
     * @return The internal log level.
     */
    public static LogLevel fromIdsLogLevel(final de.fraunhofer.iais.eis.LogLevel logLevel) {
        switch (logLevel) {
            // TODO infomodel has less log levels than DSC, info will get lost
            case MINIMAL_LOGGING:
                return LogLevel.WARN;
            case DEBUG_LEVEL_LOGGING:
                return LogLevel.DEBUG;
            default:
                return LogLevel.OFF;
        }
    }

    /**
     * Get dsc security profile from ids security profile.
     * @param securityProfile The ids security profile.
     * @return The internal security profile.
     */
    public static SecurityProfile fromIdsSecurityProfile(
            final de.fraunhofer.iais.eis.SecurityProfile securityProfile) {
        switch (securityProfile) {
            case TRUST_SECURITY_PROFILE:
                return SecurityProfile.TRUST_SECURITY;
            case TRUST_PLUS_SECURITY_PROFILE:
                return SecurityProfile.TRUST_PLUS_SECURITY;
            default:
                return SecurityProfile.BASE_SECURITY;
        }
    }

    /**
     * Build internal configuration desc from ids configModel.
     * @param configModel The ids configuration model.
     * @return The internal configuration desc.
     */
    public static ConfigurationDesc fromIdsConfig(final ConfigurationModel configModel) {
        final var description = new ConfigurationDesc();
        if (!configModel.getConnectorDescription().getTitle().isEmpty()) {
            description.setTitle(
                    configModel.getConnectorDescription().getTitle().get(0).getValue());
        }
        if (!configModel.getConnectorDescription().getDescription().isEmpty()) {
            description.setDescription(
                    configModel.getConnectorDescription().getDescription().get(0).getValue()
            );
        }
        description.setDeployMode(fromIdsDeployMode(configModel.getConnectorDeployMode()));
        description.setCurator(configModel.getConnectorDescription().getCurator());
        description.setDefaultEndpoint(
                configModel.getConnectorDescription().getHasDefaultEndpoint().getAccessURL());
        description.setInboundModelVersion(
                configModel.getConnectorDescription().getInboundModelVersion());
        description.setOutboundModelVersion(
                configModel.getConnectorDescription().getOutboundModelVersion());
        description.setKeystoreSettings(new KeystoreDesc(
                configModel.getKeyStore(),
                configModel.getKeyStorePassword()));
        description.setLogLevel(fromIdsLogLevel(configModel.getConfigurationModelLogLevel()));
        description.setMaintainer(configModel.getConnectorDescription().getMaintainer());
        description.setProxySettings(fromIdsProxy(configModel.getConnectorProxy()));
        description.setSecurityProfile(
                fromIdsSecurityProfile(configModel.getConnectorDescription().getSecurityProfile()));
        description.setTruststoreSettings(new TruststoreDesc(
                configModel.getTrustStore(), configModel.getTrustStorePassword()));
        return description;
    }

    private static DeployMode fromIdsDeployMode(final ConnectorDeployMode deployMode) {
        return deployMode == ConnectorDeployMode.TEST_DEPLOYMENT ? DeployMode.TEST
                : DeployMode.PRODUCTIVE;
    }

    private static ProxyDesc fromIdsProxy(final List<Proxy> proxyList) {
        if (proxyList == null || proxyList.isEmpty()) {
            return null;
        }

        final var proxy = proxyList.get(0);
        final var auth = proxy.getProxyAuthentication();
        return new ProxyDesc(proxy.getProxyURI(), proxy.getNoProxy()
                .stream()
                .map(URI::toString)
                .collect(Collectors.toList()),
                new AuthenticationDesc(auth.getAuthUsername(), auth.getAuthPassword()));
    }
}
