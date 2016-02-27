package org.schemarepo.validation;

import org.schemarepo.SchemaValidationException;

/**
 * A {@link ValidationStrategy} that checks that the schema to
 * validate can read the existing schema according to the rules provided by the abstract canRead method.
 */
public abstract class CanReadValidationStrategy implements ValidationStrategy {


    /**
     * Validate that the first schema provided can be used to read data written
     * with the second schema, according to the rules provided by the abstract canRead method.
     *
     * @throws SchemaValidationException
     *           if the first schema cannot read data written by the second.
     */
    @Override
    public void validate(String toValidate, String existing)
            throws SchemaValidationException {
        canRead(existing,toValidate);
    }

    protected abstract void canRead(String existing, String toValidate) throws SchemaValidationException;
}
