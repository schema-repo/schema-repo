package org.schemarepo.client.converter;

/**
 * An interface which the TypedSchemaRepository uses to convert IDs, schema
 * literals and subject names back and forth with Strings.
 */
public interface Converter<TYPE> {
  /**
   * Given a String literal, provide a strongly-typed instance.
   *
   * @param literal to be converted
   * @return the requested TYPE
   */
  TYPE fromString(String literal);

  /**
   * Given a strongly-typed instance, provide its String literal representation.
   *
   * @param strongType instance to be converted
   * @return the String literal representation
   */
  String toString(TYPE strongType);
}
