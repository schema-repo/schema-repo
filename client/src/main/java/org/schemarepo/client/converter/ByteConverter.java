package org.schemarepo.client.converter;

/**
 * To convert back and forth with Byte.
 *
 * For most people this can be a reasonable choice for IDs. Most use cases
 * should require less than 256 schemas per subject. However, if one wants to be
 * extra paranoid about future extensibility, the ShortConverter should provide
 * as much mileage as one might need.
 */
public class ByteConverter implements Converter<Byte> {
  @Override
  public Byte fromString(String literal) {
    return Byte.parseByte(literal);
  }

  @Override
  public String toString(Byte strongType) {
    return strongType.toString();
  }
}
