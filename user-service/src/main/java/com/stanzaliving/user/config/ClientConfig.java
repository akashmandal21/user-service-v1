package com.stanzaliving.user.config;

import com.stanzaliving.core.base.http.StanzaRestClient;
import com.stanzaliving.core.transformation.client.api.InternalDataControllerApi;
import com.stanzaliving.core.transformation.client.cache.TransformationCache;
import com.stanzaliving.core.user.client.api.UserClientApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Value("${service.transformation.url}")
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
