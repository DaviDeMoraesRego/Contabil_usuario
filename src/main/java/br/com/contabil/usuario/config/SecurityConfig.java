package br.com.contabil.usuario.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Value("${app.cors.allowed-origins}")
	private List<String> allowedOrigins;

	@Value("${app.swagger.enabled:false}")
	private boolean swaggerEnabled;

	@Value("${app.cors.allowed-origins}")
	private String expectedClientId;

	private static final String[] SWAGGER_WHITELIST = { "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**",
			"/webjars/**" };

	private static final String[] ACTUATOR_WHITELIST = { "/actuator/health/liveness", "/actuator/health/readiness" };

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.cors(Customizer.withDefaults())
				.headers(headers -> headers
						.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
						.frameOptions(frame -> frame.deny()).contentTypeOptions(Customizer.withDefaults())
						.referrerPolicy(referrer -> referrer
								.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
						.addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
								"camera=(), microphone=(), geolocation=()"))
						.contentSecurityPolicy(
								csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'")))
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, e) -> {
					log.warn("Acesso nao autenticado: {} {} | IP: {}", request.getMethod(), request.getRequestURI(),
							request.getRemoteAddr());
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				}).accessDeniedHandler((request, response, e) -> {
					log.warn("Acesso negado: {} {} | IP: {}", request.getMethod(), request.getRequestURI(),
							request.getRemoteAddr());
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
				})).authorizeHttpRequests(auth -> {
					auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
					auth.requestMatchers(ACTUATOR_WHITELIST).permitAll();
					if (swaggerEnabled) {
						auth.requestMatchers(SWAGGER_WHITELIST).permitAll();
					}
					auth.anyRequest().authenticated();
				}).oauth2ResourceServer(
						oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

		return http.build();
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
			String azp = jwt.getClaimAsString("azp");
			if (azp == null || !azp.equals(expectedClientId)) {
				log.warn("Token rejeitado: azp invalido '{}'", azp);
				throw new JwtException("Token nao autorizado para esta aplicacao");
			}
			return List.of();
		});
		return converter;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
		config.setExposedHeaders(List.of("Authorization"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}