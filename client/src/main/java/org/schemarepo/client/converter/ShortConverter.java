package org.schemarepo.client.converter;

/**
 * To convert back and forth with Short.
 *
 * For most people this can be a reasonable choice for IDs. If anyone needs to
 * store more than 65K schemas for a single subject, they should probably take
 * a long hard look at how they're using the schema repo.
 */
public class ShortConverter implements Converter<Short> {
  @Override
  public Short fromString(String literal) {
    return Short.parseShort(literal);
  }

  @Override
  public String toString(Short strongType) {
    return strongType.toString();
  }
}
