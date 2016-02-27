package org.schemarepo.validation;

import org.schemarepo.SchemaValidationException;
import org.schemarepo.config.Config;

/**
 * A stub ValidationStrategy that always fails. This is the default passed as the "CanRead" strategy to the
 * {@link CanBeReadValidationStrategy} and {@link MutualReadValidationStrategy}. This is not useful, but necessary
 * to prevent binding failure if no reasonable CanReadValidationStrategy is configured.
 */
public class AlwaysFailValidationStrategy implements ValidationStrategy {
    @Override
    public void validate(String toValidate, String existing) throws SchemaValidationException {
        throw new SchemaValidationException("FailValidationStrategy always fails validation. Configure a valid ValidationStrategy for the setting " + Config.VALIDATION_READ_STRATEGY_CLASS + " if using CanBeReadValidationStrategy or MutualReadValidationStrategy.");
    }
}
