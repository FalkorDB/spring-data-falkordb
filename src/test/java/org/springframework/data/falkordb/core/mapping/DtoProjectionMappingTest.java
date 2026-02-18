/*
 * Regression test for DTO projections (non-@Node types) returned from @Query
 * methods such as:
 *
 *   RETURN s AS skill, collect(f) AS field
 */

package org.springframework.data.falkordb.core.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.data.falkordb.core.FalkorDBClient;
import org.springframework.data.falkordb.core.schema.Id;
import org.springframework.data.falkordb.core.schema.Node;
import org.springframework.data.mapping.model.EntityInstantiators;

class DtoProjectionMappingTest {

	@Test
	void shouldMapDtoWithNestedEntityAndCollectedEntities() {
		com.falkordb.graph_entities.Node skillNode = new com.falkordb.graph_entities.Node();
		skillNode.addProperty("name", "Java");

		com.falkordb.graph_entities.Node field1 = new com.falkordb.graph_entities.Node();
		field1.addProperty("name", "Backend");

		com.falkordb.graph_entities.Node field2 = new com.falkordb.graph_entities.Node();
		field2.addProperty("name", "JVM");

		LinkedHashMap<String, Object> row = new LinkedHashMap<>();
		row.put("skill", skillNode);
		row.put("field", Arrays.asList(field1, field2));

		FalkorDBClient.Record record = new MapBackedRecord(row);

		DefaultFalkorDBMappingContext mappingContext = new DefaultFalkorDBMappingContext();
		DefaultFalkorDBEntityConverter converter = new DefaultFalkorDBEntityConverter(mappingContext,
				new EntityInstantiators());

		SkillField dto = converter.read(SkillField.class, record);

		assertThat(dto).isNotNull();
		assertThat(dto.skill).isNotNull();
		assertThat(dto.skill.name).isEqualTo("Java");
		assertThat(dto.field).extracting(f -> f.name).containsExactly("Backend", "JVM");
	}

	@Node("Skill")
	static class Skill {
		@Id
		String name;
	}

	@Node("Field")
	static class Field {
		@Id
		String name;
	}

	static class SkillField {
		Skill skill;
		List<Field> field;
	}

	static class MapBackedRecord implements FalkorDBClient.Record {

		private final Map<String, Object> data;

		MapBackedRecord(Map<String, Object> data) {
			this.data = data;
		}

		@Override
		public Object get(int index) {
			return null;
		}

		@Override
		public Object get(String key) {
			return data.get(key);
		}

		@Override
		public Iterable<String> keys() {
			return data.keySet();
		}

		@Override
		public int size() {
			return data.size();
		}

		@Override
		public Iterable<Object> values() {
			return data.values();
		}
	}
}
