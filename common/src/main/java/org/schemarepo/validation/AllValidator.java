package org.schemarepo.validation;
import org.schemarepo.SchemaEntry;
import org.schemarepo.SchemaValidationException;
import org.schemarepo.Validator;
import org.schemarepo.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * <p>
 * A {@link Validator} for validating the provided schema against all
 * schemas in the Iterable in {@link #validate(String, Iterable)}.
 * </p>
 * <p>
 * Uses the {@link ValidationStrategy} provided in the constructor to
 * validate the Schema against each Schema in the Iterable, in Iterator
 * order, via {@link ValidationStrategy#validate(String, String)}.
 * </p>
 */
public final class AllValidator implements Validator {
    private final ValidationStrategy strategy;

    /**
     * @param strategyMap
     *          The map of all registered ValidationStrategy implementations to use for validation of pairwise schemas.
     * @param validationStrategyClassName
     *          The name of the class to use for this instance of Validator. Used as a key into the map of all strategies.
     */
    @Inject
    public AllValidator(Map<String,ValidationStrategy> strategyMap, @Named(Config.VALIDATION_ALL_VALIDATOR_STRATEGY) String validationStrategyClassName) {
        this.strategy = strategyMap.get(validationStrategyClassName);
    }

    @Override
    public void validate(String toValidate, Iterable<SchemaEntry> schemasInOrder)
            throws SchemaValidationException {
        for (SchemaEntry schemaEntry : schemasInOrder) {
            strategy.validate(toValidate, schemaEntry.getSchema());
        }
    }

}
