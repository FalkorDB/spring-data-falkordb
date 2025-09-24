/*
 * Copyright 2011-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.neo4j.aot;

import java.util.function.Predicate;

import org.springframework.data.util.TypeCollector;
import org.springframework.data.util.TypeUtils;

/**
 * {@link TypeCollector} predicates to exclude Neo4j simple types.
 *
 * @author Mark Paluch
 * @since 8.0
 */
class Neo4jTypeFilters implements TypeCollector.TypeCollectorFilters {

	private static final Predicate<Class<?>> CLASS_FILTER = it -> TypeUtils.type(it)
		.isPartOf("org.springframework.data.neo4j.types", "org.neo4j.driver.types");

	@Override
	public Predicate<Class<?>> classPredicate() {
		return Neo4jAotPredicates.IS_SIMPLE_TYPE.or(CLASS_FILTER).negate();
	}

}
