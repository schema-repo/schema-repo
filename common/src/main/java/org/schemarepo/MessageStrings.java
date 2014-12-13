package org.schemarepo;

/**
 * Static Strings used to communicate a message to the end-user.
 */
public class MessageStrings {
  public static final String SCHEMA_WITH_NEWLINE_ERROR =
          "ERROR: One of the schemas for this " +
          "topic contains a new line and won't be parse-able properly. " +
          "Please use a non-plain text format instead (e.g.: JSON).\n";

  public static final String SUBJECT_DOES_NOT_EXIST_ERROR =
          "ERROR: This subject does not exist.\n";

  public static final String SCHEMA_DOES_NOT_EXIST_ERROR =
          "ERROR: This schema does not exist.\n";
}
