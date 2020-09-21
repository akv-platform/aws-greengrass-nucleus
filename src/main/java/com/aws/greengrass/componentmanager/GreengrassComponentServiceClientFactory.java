/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.componentmanager;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.evergreen.AWSEvergreen;
import com.amazonaws.services.evergreen.AWSEvergreenClientBuilder;
import com.aws.greengrass.deployment.DeviceConfiguration;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import com.aws.greengrass.tes.LazyCredentialProvider;
import com.aws.greengrass.util.Coerce;
import com.aws.greengrass.util.Utils;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;

@Getter
@SuppressWarnings("PMD.ConfusingTernary")
public class GreengrassComponentServiceClientFactory {

    public static final String CONTEXT_COMPONENT_SERVICE_ENDPOINT = "greengrassServiceEndpoint";
    private static final Logger logger = LogManager.getLogger(GreengrassComponentServiceClientFactory.class);

    private final AWSEvergreen cmsClient;

    /**
     * Constructor with custom endpoint/region configuration.
     *
     * @param greengrassServiceEndpoint String containing service endpoint
     * @param deviceConfiguration       Device configuration
     * @param credentialsProvider       AWS Credentials provider for device credentials
     */
    @Inject
    public GreengrassComponentServiceClientFactory(
            @Named(CONTEXT_COMPONENT_SERVICE_ENDPOINT) String greengrassServiceEndpoint,
            DeviceConfiguration deviceConfiguration,
            LazyCredentialProvider credentialsProvider) {
        AWSEvergreenClientBuilder clientBuilder =
                AWSEvergreenClientBuilder.standard();
        String region = Coerce.toString(deviceConfiguration.getAWSRegion());

        if (!Utils.isEmpty(region)) {
            if (!Utils.isEmpty(greengrassServiceEndpoint)) {
                // Region and endpoint are both required when updating endpoint config
                logger.atInfo("initialize-cms-client").addKeyValue("service-endpoint", greengrassServiceEndpoint)
                        .addKeyValue("service-region", region).log();
                EndpointConfiguration endpointConfiguration =
                        new EndpointConfiguration(greengrassServiceEndpoint, region);
                clientBuilder.withEndpointConfiguration(endpointConfiguration);
            } else {
                // This section is to override default region if needed
                logger.atInfo("initialize-cms-client").addKeyValue("service-region", region).log();
                clientBuilder.withRegion(region);
            }
        }

        if (credentialsProvider != null) {
            clientBuilder.withCredentials(credentialsProvider);
        }

        this.cmsClient = clientBuilder.build();
    }
}