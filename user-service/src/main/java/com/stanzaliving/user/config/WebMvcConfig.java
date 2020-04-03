package com.stanzaliving.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.stanzaliving.user.interceptor.UserAuthInterceptor;
import com.stanzaliving.core.security.interceptor.UIDInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Autowired
	private UserAuthInterceptor userAuthInterceptor;

	@Bean
	public UIDInterceptor uidInterceptor() {
		return new UIDInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(uidInterceptor())
				.addPathPatterns("/**").order(Ordered.HIGHEST_PRECEDENCE);

		registry
				.addInterceptor(userAuthInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns(
						"/auth/login",
						"/auth/validateOtp",
						"/auth/resendOtp")
				.excludePathPatterns(
						"/add",
						"/search/**",
						"/mapping/**",
						"/usermanagermapping/**",
						"/managerprofiles/**")
				.excludePathPatterns(
						"/internal/**",
						"/pingMe",
						"/acl/check",
						"/user/urlList/**",
						"/acl/user/fe/**",
						"/acl/user/be/**",
						"/v2/api-docs",
						"/configuration/ui",
						"/swagger-resources/**",
						"/configuration/security",
						"/swagger-ui.html",
						"/webjars/**");

	}
}