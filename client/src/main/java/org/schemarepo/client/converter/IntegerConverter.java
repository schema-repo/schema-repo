package org.schemarepo.client.converter;

/**
 * To convert back and forth with Integer.
 *
 * As a converter for IDs, this is probably overkill for most use cases, since
 * it allows billions of schemas per subject. There may be some highly generic
 * and dynamic system architectures that would warrant such high cardinality,
 * but for most intents and purposes, one should probably think twice about
 * using this kind of ID.
 */
public class IntegerConverter implements Converter<Integer> {
  @Override
  public Integer fromString(String literal) {
    return Integer.parseInt(literal);
  }

  @Override
  public String toString(Integer strongType) {
    return strongType.toString();
  }
}
