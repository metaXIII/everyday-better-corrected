package co.simplon.everydaybetterbusiness.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public final class AppUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

  private AppUtils() {
    // Utility class
    throw new IllegalStateException("Utility class");
  }

  public static String getAuthenticatedUser() throws BadCredentialsException {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof final Jwt jwt) {
      return jwt.getClaim("sub");
    }
    throw new BadCredentialsException("User is not authenticated");
  }

  public static ObjectMapper getMapper() {
    return OBJECT_MAPPER;
  }
}
