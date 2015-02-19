package org.schemarepo.config;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import org.schemarepo.validation.AvroCanReadValidationStrategy;
import org.schemarepo.validation.ValidationStrategy;

/**
 * Extension module to bind implementation(s) of ValidationStrategy.
 */
public class AvroValidationModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder<String,ValidationStrategy> strategyMap = MapBinder.newMapBinder(binder(),String.class,ValidationStrategy.class);
        strategyMap.addBinding(AvroCanReadValidationStrategy.class.getCanonicalName()).to(AvroCanReadValidationStrategy.class);
    }
}
