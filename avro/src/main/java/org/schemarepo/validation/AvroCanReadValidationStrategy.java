package org.schemarepo.validation;

import org.apache.avro.Schema;
import org.schemarepo.SchemaValidationException;
import org.apache.avro.io.parsing.ResolvingGrammarGenerator;
import org.apache.avro.io.parsing.Symbol;
import org.schemarepo.api.converter.AvroSchemaConverter;

import java.io.IOException;

/**
 * Implements a {@link CanReadValidationStrategy} for Avro {@link Schema} type.
 */
public class AvroCanReadValidationStrategy extends CanReadValidationStrategy {
    private final AvroSchemaConverter converter = new AvroSchemaConverter();

    @Override
    protected void canRead(String writtenWithString, String readUsingString) throws SchemaValidationException {
        boolean error;
        Schema writtenWith = converter.fromString(writtenWithString);
        Schema readUsing = converter.fromString(readUsingString);
        try {
            error = Symbol.hasErrors(new ResolvingGrammarGenerator().generate(
                    writtenWith, readUsing));
        } catch (IOException e) {
            throw new SchemaValidationException(getMessage(readUsing, writtenWith), e);
        }
        if (error) {
            throw new SchemaValidationException(getMessage(readUsing, writtenWith));
        }
    }

    private static String getMessage(Schema reader, Schema writer) {
        return "Unable to read schema: \n"
                + writer.toString(true) + "\nusing schema:\n" + reader.toString(true);
    }
}
