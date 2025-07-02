package com.pbs.app.config;

import com.pbs.app.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);
    private final JWTService jwtService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Registering STOMP endpoint '/ws-chat'");
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:5174", "http://localhost:5175")
                /* ⚠️ handshake → JWT z query-param „token” */
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest req, WebSocketHandler wsh,
                                                      Map<String, Object> attrs) {
                        String token = UriComponentsBuilder.fromUri(req.getURI())
                                                           .build()
                                                           .getQueryParams()
                                                           .getFirst("token");
                        if (token != null) {
                            try {
                                String userId = jwtService.extractUserId(token);
                                log.info("Handshake auth OK, principal={}", userId);
                                return new StompPrincipal(userId);
                            } catch (Exception e) {
                                log.error("Handshake auth error", e);
                            }
                        }
                        return super.determineUser(req, wsh, attrs);
                    }
                })
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/queue", "/topic");
    }
}
