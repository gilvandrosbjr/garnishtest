/*
 * Copyright 2016-2018, Garnish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.garnishtest.steps.vars.scenario_user_vars;

import org.garnishtest.modules.generic.variables_resolver.VariablesResolver;
import org.garnishtest.modules.generic.variables_resolver.impl.MapBasedVariablesResolver;
import org.garnishtest.modules.generic.variables_resolver.impl.NoOpVariablesResolver;
import org.garnishtest.modules.generic.variables_resolver.impl.escape.ValueEscaper;
import org.garnishtest.steps.vars.scenario_attrs.ScenarioAttribute;
import lombok.NonNull;

import javax.annotation.Nullable;

public final class ScenarioUserVariables {

    private static final String VAR_PREFIX = "${";
    private static final String VAR_SUFFIX = "}";

    private static final ScenarioAttribute<MapBasedVariablesResolver> CUSTOM_STORY_VARIABLES = ScenarioAttribute.create();

    private ScenarioUserVariables() {
    }

    public static void set(@NonNull final String name,
                           @Nullable final String value) {
        final MapBasedVariablesResolver storyVariables = CUSTOM_STORY_VARIABLES.getValue();

        if (storyVariables != null) {
            storyVariables.set(name, value);
        } else {
            final MapBasedVariablesResolver newStoryVariables = new MapBasedVariablesResolver(VAR_PREFIX, VAR_SUFFIX);

            newStoryVariables.set(name, value);

            CUSTOM_STORY_VARIABLES.setValue(newStoryVariables);
        }
    }

    @NonNull
    public static String resolveInText(@NonNull final String textWithVariables,
                                       @NonNull final ValueEscaper valueEscaper) {
        final VariablesResolver resolver = getResolver();

        return resolver.resolveVariablesInText(textWithVariables, valueEscaper);
    }

    @NonNull
    public static VariablesResolver getResolver() {
        final MapBasedVariablesResolver storyVariables = CUSTOM_STORY_VARIABLES.getValue();
        if (storyVariables == null) {
            return NoOpVariablesResolver.instance();
        }

        return storyVariables;
    }

}
