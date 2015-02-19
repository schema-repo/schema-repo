package org.schemarepo.validation;


import org.schemarepo.SchemaValidationException;
import org.schemarepo.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * A {@link ValidationStrategy} that checks that the schema to
 * validate and the existing schema can mutually read each other according to
 * the rules provided by the abstract canRead method.
 */
public final class MutualReadValidationStrategy implements ValidationStrategy {
    private final ValidationStrategy readStrategy;

    /**
     *
     * @param strategyMap
     *          The map of all registered ValidationStrategy implementations to use for validation of pairwise schemas.
     * @param readStrategyClassName
     *          The class name of the basic "CanRead" ValidationStrategy to use for checking MutualRead.
     */
    @Inject
    public MutualReadValidationStrategy(Map<String,ValidationStrategy> strategyMap, @Named(Config.VALIDATION_READ_STRATEGY_CLASS) String readStrategyClassName) {
        readStrategy = strategyMap.get(readStrategyClassName);
    }

    /**
     * Validate that the schema to validate and the existing
     * schema can mutually read each other according to
     * the rules provided by the abstract canRead method.
     *
     * @throws SchemaValidationException
     *           if the first schema cannot read data written by the second.
     */
    @Override
    public void validate(String toValidate, String existing)
            throws SchemaValidationException {
        // mutual read means can read in both directions.
        readStrategy.validate(toValidate, existing);
        readStrategy.validate(existing, toValidate);
    }
}
