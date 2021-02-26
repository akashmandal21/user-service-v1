package com.stanzaliving.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stanzaliving.core.base.http.StanzaRestClient;
import com.stanzaliving.core.transformation.client.api.InternalDataControllerApi;
import com.stanzaliving.core.transformation.client.cache.TransformationCache;

@Configuration
public class ClientConfig {

    @Value("${service.transformationmaster.url}")
    private String transformationUrl;

    @Bean
    public InternalDataControllerApi internalDataControllerApi() {
        return new InternalDataControllerApi(new StanzaRestClient(transformationUrl));
    }

    @Bean
    public TransformationCache transformationCache(InternalDataControllerApi internalDataControllerApi) {
        return new TransformationCache(internalDataControllerApi);
    }
}
