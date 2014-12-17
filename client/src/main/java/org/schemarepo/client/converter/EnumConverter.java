package org.schemarepo.client.converter;

/**
 * This converter can be useful for people who wish to constrain the usable
 * subjects to a predetermined set of elements, i.e.: an Enum.
 */
public class EnumConverter<E extends Enum<E>> implements Converter<E> {

  private Class<E> enumClass;

  public EnumConverter(E enumInstance) {
    this.enumClass = enumInstance.getDeclaringClass();
  }

  public EnumConverter(Class<E> enumClass) {
    this.enumClass = enumClass;
  }

  @Override
  public E fromString(String literal) {
    return Enum.valueOf(enumClass, literal);
  }

  @Override
  public String toString(E strongType) {
    return strongType.name();
  }
}
