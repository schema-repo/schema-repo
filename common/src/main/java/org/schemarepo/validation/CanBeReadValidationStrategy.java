package org.schemarepo.validation;


import org.schemarepo.SchemaValidationException;
import org.schemarepo.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * A {@link ValidationStrategy} that checks that the  existing schema
 *  can read the schema to validate according to the rules provided by the abstract canRead method.
 *  This is the opposite of the {@link CanReadValidationStrategy}.
 */
public final class CanBeReadValidationStrategy implements ValidationStrategy {
    private final ValidationStrategy readStrategy;

    /**
     *
     * @param strategyMap
     *          The map of all registered ValidationStrategy implementations to use for validation of pairwise schemas.
     * @param readStrategyClassName
     *          The class name of the basic "CanRead" ValidationStrategy to use for checking CanBeRead (the inverse of CanRead).
     */
    @Inject
    public CanBeReadValidationStrategy(Map<String,ValidationStrategy> strategyMap, @Named(Config.VALIDATION_READ_STRATEGY_CLASS) String readStrategyClassName) {
        readStrategy = strategyMap.get(readStrategyClassName);
    }

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
        // CanBeRead is just the opposite of CanRead, so pass the schemas in opposite order.
        readStrategy.validate(existing,toValidate);
    }
}
