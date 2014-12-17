package org.schemarepo.client.converter;

/**
 * For the entities you wish to use directly as Strings, without any conversion.
 */
public class IdentityConverter implements Converter<String> {
  @Override
  public String fromString(String literal) {
    return literal;
  }

  @Override
  public String toString(String strongType) {
    return strongType;
  }
}
