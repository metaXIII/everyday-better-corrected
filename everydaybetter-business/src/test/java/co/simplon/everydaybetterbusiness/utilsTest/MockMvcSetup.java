package co.simplon.everydaybetterbusiness.utilsTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import co.simplon.everydaybetterbusiness.common.utils.AppUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public abstract class MockMvcSetup {

  protected static final ObjectMapper mapper = AppUtils.getMapper();
  protected static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor validToken = jwt().jwt(
    Jwt.withTokenValue("tokenValue").claim("sub", "test@example.com").header("header", "header").build()
  );

  @Autowired
  protected MockMvc mockMvc;

  @BeforeAll
  static void initAll() {
    mapper.registerModule(new JavaTimeModule());
  }

  protected String asJson(final Object object) throws IOException {
    return mapper.writeValueAsString(object);
  }

  protected abstract Object getController();
}
