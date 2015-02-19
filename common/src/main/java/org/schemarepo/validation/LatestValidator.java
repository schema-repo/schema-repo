package org.schemarepo.validation;


import org.schemarepo.SchemaEntry;
import org.schemarepo.SchemaValidationException;
import org.schemarepo.Validator;
import org.schemarepo.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 * A {@link Validator} for validating the provided schema against the
 * first schema in the iterable in {@link #validate(String, Iterable)}.
 * </p>
 * <p>
 * Uses the {@link ValidationStrategy} provided in the constructor to
 * validate the schema against the first Schema in the iterable, if it exists,
 * via {@link ValidationStrategy#validate(String, String)}.
 * </p>
 */
public class LatestValidator implements Validator {
    private final ValidationStrategy strategy;

    /**
     * @param strategyMap
     *          The map of all registered ValidationStrategy implementations to use for validation of pairwise schemas.
     * @param validationStrategyClassName
     *          The class name of the concrete ValidationStrategy to use for this instance of Validator. Used as a key into the strategyMap map of all strategies.
     */
    @Inject
    public LatestValidator(Map<String,ValidationStrategy> strategyMap, @Named(Config.VALIDATION_LATEST_VALIDATOR_STRATEGY) String validationStrategyClassName) {
        this.strategy = strategyMap.get(validationStrategyClassName);
    }

    @Override
    public void validate(String toValidate, Iterable<SchemaEntry> schemasInOrder)
            throws SchemaValidationException {
        Iterator<SchemaEntry> schemas = schemasInOrder.iterator();
        if (schemas.hasNext()) {
            strategy.validate(toValidate, schemas.next().getSchema());
        }
    }

}
