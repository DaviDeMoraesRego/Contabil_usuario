package br.com.contabil.usuario.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Value("${app.cors.allowed-origins}")
	private List<String> allowedOrigins;

	@Value("${app.swagger.enabled:false}")
	private boolean swaggerEnabled;

	private static final String[] SWAGGER_WHITELIST = { "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**",
			"/webjars/**" };

	private static final String[] ACTUATOR_WHITELIST = { "/actuator/health/liveness", "/actuator/health/readiness" };

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(csrf -> csrf.disable())
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
					log.warn("Nao autenticado: {} {} | IP: {}", req.getMethod(), req.getRequestURI(),
							req.getRemoteAddr());
					res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				}).accessDeniedHandler((req, res, e) -> {
					log.warn("Acesso negado: {} {} | IP: {}", req.getMethod(), req.getRequestURI(),
							req.getRemoteAddr());
					res.sendError(HttpServletResponse.SC_FORBIDDEN);
				})).authorizeHttpRequests(auth -> {
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
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
		return converter;
	}

	private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		String role = jwt.getClaimAsString("role");
		if (role == null || role.isBlank()) {
			return List.of(new SimpleGrantedAuthority("ROLE_USER"));
		}
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()));
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(
				List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Cache-Control"));
		config.setExposedHeaders(List.of("Authorization"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}