package com.stanzaliving.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.stanzaliving.user.interceptor.UserAuthInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Autowired
	private UserAuthInterceptor userAuthInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
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