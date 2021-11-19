package com.example.securitypractice.configs;

import com.example.securitypractice.services.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    public void setCustomOAuth2UserService(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    .mvcMatchers("/secured/**")
                    .authenticated()
                .and()
                .oauth2Login()
                .tokenEndpoint(
                        s-> s.accessTokenResponseClient(accessTokenResponseClient())
                )
                .userInfoEndpoint(
                        s-> s.userService(customOAuth2UserService)
                );


    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(googleClientRegistration(), vkClientRegistration());
    }

    @Bean
    OAuth2AuthorizedClientService oauth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository){
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository(
            @Qualifier("oauth2AuthorizedClientService") OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(oAuth2AuthorizedClientService);
    }


    private ClientRegistration googleClientRegistration() {
        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId("xxx")
                .clientSecret("xxx")
                .userNameAttributeName("name")
                .build();
    }

    private ClientRegistration vkClientRegistration() {
        return ClientRegistration.withRegistrationId("vk")
                .clientId("xxx")
                .clientSecret("xxx")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .userInfoAuthenticationMethod(AuthenticationMethod.FORM)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("email")
                .authorizationUri("https://oauth.vk.com/authorize?revoke=1")
                .tokenUri("https://oauth.vk.com/access_token")
                .userInfoUri("https://api.vk.com/method/users.get?v=8.1&fields=photo_max")
                .userNameAttributeName("first_name")
                .build();
    }

    @Bean
    OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient =
                new DefaultAuthorizationCodeTokenResponseClient();

        OAuth2AccessTokenResponseHttpMessageConverter accessTokenResponseHttpMessageConverter =
                new OAuth2AccessTokenResponseHttpMessageConverter();
        accessTokenResponseHttpMessageConverter.setTokenResponseConverter(new CustomAccessTokenResponseConverter());

        RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(),
                accessTokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        accessTokenResponseClient.setRestOperations(restTemplate);
        return  accessTokenResponseClient;
    }

    private class CustomAccessTokenResponseConverter implements Converter<Map<String, String>,
            OAuth2AccessTokenResponse> {
        @Override
        public OAuth2AccessTokenResponse convert(Map<String, String> tokenResponseParams) {
            String accessToken = tokenResponseParams.get(OAuth2ParameterNames.ACCESS_TOKEN);
            OAuth2AccessToken.TokenType tokenType = OAuth2AccessToken.TokenType.BEARER;
            Map <String, Object> additionalParams = new HashMap<>();
            tokenResponseParams.forEach(
                    (k,v) -> additionalParams.put(k,v)
            );
            return OAuth2AccessTokenResponse.withToken(accessToken)
                    .tokenType(tokenType)
                    .additionalParameters(additionalParams)
                    .build();

        }
    }

}
