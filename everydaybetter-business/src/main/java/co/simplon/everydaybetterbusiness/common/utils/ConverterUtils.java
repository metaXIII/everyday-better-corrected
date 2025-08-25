package co.simplon.everydaybetterbusiness.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ConverterUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

  private ConverterUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static ObjectMapper getMapper() {
    return OBJECT_MAPPER;
  }
}
