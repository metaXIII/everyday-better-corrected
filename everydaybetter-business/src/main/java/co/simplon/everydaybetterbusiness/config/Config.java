package co.simplon.everydaybetterbusiness.config;

import com.auth0.jwt.algorithms.Algorithm;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class Config {

  private static final String USER = "USER";
  private static final String PATH_ACTIVITIES = "/activities";
  private static final String PATH_ACTIVITIES_ID = "/activities/**";
  private static final String PATH_TRACKING_LOG = "/tracking-logs";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String ACCEPT = "Accept";
  private final EverydayBetterConfig everydayBetterConfig;

  public Config(final EverydayBetterConfig everydayBetterConfig) {
    this.everydayBetterConfig = everydayBetterConfig;
  }

  @Bean
  @Profile("dev")
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(@NonNull final CorsRegistry registry) {
        registry
          .addMapping(PATH_ACTIVITIES)
          .allowedMethods("POST", "GET")
          .allowedOrigins(everydayBetterConfig.business().origins())
          .allowCredentials(true)
          .allowedHeaders(CONTENT_TYPE, ACCEPT)
          .maxAge(3600); //how long the browser can cache the CORS response before remaking an OPTIONS request 1h, default 30m
        registry
          .addMapping(PATH_ACTIVITIES_ID)
          .allowedMethods("GET", "PUT", "DELETE")
          .allowedOrigins(everydayBetterConfig.business().origins())
          .allowCredentials(true)
          .allowedHeaders(CONTENT_TYPE, ACCEPT)
          .maxAge(3600);
        registry
          .addMapping("/categories")
          .allowedMethods("GET")
          .allowedOrigins(everydayBetterConfig.business().origins())
          .allowCredentials(true)
          .allowedHeaders(CONTENT_TYPE, ACCEPT)
          .maxAge(3600);
        registry
          .addMapping(PATH_TRACKING_LOG)
          .allowedMethods("POST", "GET", "DELETE")
          .allowedOrigins(everydayBetterConfig.business().origins())
          .allowCredentials(true)
          .allowedHeaders(CONTENT_TYPE, ACCEPT)
          .maxAge(3600);
        registry
          .addMapping("/tracking-logs/update")
          .allowedMethods("PATCH")
          .allowedOrigins(everydayBetterConfig.business().origins())
          .allowCredentials(true)
          .allowedHeaders(CONTENT_TYPE, ACCEPT)
          .maxAge(3600);
        registry
          .addMapping("/tracking-logs/progress-summary")
          .allowedMethods("GET")
          .allowedOrigins(everydayBetterConfig.business().origins())
          .allowCredentials(true)
          .allowedHeaders(CONTENT_TYPE, ACCEPT)
          .maxAge(3600);
        registry
          .addMapping("/users/create")
          .allowedMethods("POST")
          .allowedOrigins(everydayBetterConfig.business().origins())
          .allowedHeaders(CONTENT_TYPE, ACCEPT)
          .maxAge(3600);
        registry
          .addMapping("/users/authenticate")
          .allowedMethods("POST")
          .allowedOrigins(everydayBetterConfig.business().origins())
          .allowCredentials(true)
          .allowedHeaders(CONTENT_TYPE, ACCEPT)
          .maxAge(3600);
        registry
          .addMapping("/users/logout")
          .allowedMethods("POST")
          .allowedOrigins(everydayBetterConfig.business().origins())
          .allowCredentials(true)
          .allowedHeaders(CONTENT_TYPE, ACCEPT)
          .maxAge(3600);
        registry.addMapping("/v3/api-docs/**").allowedOrigins(everydayBetterConfig.business().origins());
        registry.addMapping("/swagger-ui/**").allowedOrigins(everydayBetterConfig.business().origins());
      }
    };
  }

  @Bean
  PasswordEncoder encoder() {
    return new BCryptPasswordEncoder(everydayBetterConfig.business().cost());
  }

  @Bean
  //so Spring will manage and provide this SecurityFilterChain as a component
  SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
    return http
      .cors(Customizer.withDefaults())
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(req ->
        req
          .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**")
          .permitAll()
          .requestMatchers("/projects/ping")
          .permitAll()
          .requestMatchers(HttpMethod.POST, "/users/create", "/users/authenticate")
          .anonymous()
          .requestMatchers(HttpMethod.POST, PATH_ACTIVITIES, PATH_TRACKING_LOG)
          .hasRole(USER)
          .requestMatchers(
            HttpMethod.GET,
            PATH_ACTIVITIES,
            PATH_ACTIVITIES_ID,
            "/categories",
            PATH_TRACKING_LOG,
            "/tracking-logs/progress-summary"
          )
          .hasRole(USER)
          .requestMatchers(HttpMethod.PUT, PATH_ACTIVITIES_ID)
          .hasRole(USER)
          .requestMatchers(HttpMethod.PATCH, "/tracking-logs/update")
          .hasRole(USER)
          .requestMatchers(HttpMethod.DELETE, PATH_ACTIVITIES_ID, PATH_TRACKING_LOG)
          .hasRole(USER)
      )
      // Always last rule:
      .authorizeHttpRequests(reqs -> reqs.anyRequest().authenticated())
      .oauth2ResourceServer(
        oauth2 -> oauth2.bearerTokenResolver(new CookieTokenResolver()).jwt(Customizer.withDefaults()) // Configuration default JWT
      )
      .build();
  }

  @Bean
  JwtDecoder jwtDecoder() {
    // Tell Spring how to verify JWT signature
    final var secretKey = new SecretKeySpec(everydayBetterConfig.business().secret().getBytes(), "HMACSHA256");
    final var decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    final var validator = JwtValidators.createDefaultWithIssuer(everydayBetterConfig.jwtConfig().issuer());
    decoder.setJwtValidator(validator);
    return decoder;
  }

  @Bean
  JwtProvider jwtProvider() {
    final Algorithm algorithm = Algorithm.HMAC256(everydayBetterConfig.business().secret());
    return new JwtProvider(
      algorithm,
      everydayBetterConfig.jwtConfig().expiration(),
      everydayBetterConfig.jwtConfig().issuer()
    );
  }
}
