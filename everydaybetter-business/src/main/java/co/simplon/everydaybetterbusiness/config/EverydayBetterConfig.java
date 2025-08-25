package co.simplon.everydaybetterbusiness.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public record EverydayBetterConfig(BusinessConfig business, JwtConfig jwtConfig) {
  @ConfigurationProperties(prefix = "co.simplon.everydaybetterbusiness")
  public record BusinessConfig(String origins, @Min(value = 4) Integer cost, String secret) {}

  @ConfigurationProperties(prefix = "co.simplon.everydaybetterbusiness.jwt")
  public record JwtConfig(Long expiration, String issuer) {}
}
