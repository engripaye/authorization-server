package org.engripaye.authorizationserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Duration;
import java.util.UUID;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

@Configuration
public class AuthorizationConfigServer {

        @Bean
        public RegisteredClientRepository registeredClientRepository(){
            RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("my-client") //client id
                    .clientSecret("{noop}secret123") // client secret
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS) // 👈 must be here
                    .scope("read")
                    .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(30)).build())
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                    .build();

            return new InMemoryRegisteredClientRepository(client);
        }

    @Bean
    public SecurityFilterChain authServerSecurity(HttpSecurity http) throws Exception {
        // 1️⃣ Instantiate the configurer (no generics)
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        // 2️⃣ Explicitly apply it — still required despite deprecation
        http.apply(authorizationServerConfigurer);

        // 3️⃣ Use the matcher to secure OAuth2 endpoints
        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
