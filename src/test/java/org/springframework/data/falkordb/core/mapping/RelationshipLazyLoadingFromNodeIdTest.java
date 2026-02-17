/*
 * Regression tests for relationship hydration when custom Cypher queries return
 * multiple columns (e.g. `RETURN u, r, s`) without explicitly projecting
 * `id(u) AS nodeId`.
 */

package org.springframework.data.falkordb.core.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.data.falkordb.core.FalkorDBClient;
import org.springframework.data.falkordb.core.schema.GeneratedValue;
import org.springframework.data.falkordb.core.schema.Id;
import org.springframework.data.falkordb.core.schema.Node;
import org.springframework.data.falkordb.core.schema.Relationship;
import org.springframework.data.mapping.model.EntityInstantiators;

import com.falkordb.graph_entities.Edge;

class RelationshipLazyLoadingFromNodeIdTest {

	@Test
	void shouldLoadRelationshipWhenNodeIdIsOnlyAvailableOnReturnedNodeObject() {
		// Arrange: source user node returned as part of `RETURN u, r, s`
		com.falkordb.graph_entities.Node userNode = new com.falkordb.graph_entities.Node();
		userNode.setId(100L);
		userNode.addProperty("email", "john.doe@example.com");

		Edge rel = new Edge();
		rel.setRelationshipType("HAS_SKILL");
		rel.setSource(100L);
		rel.setDestination(200L);

		com.falkordb.graph_entities.Node skillNode = new com.falkordb.graph_entities.Node();
		skillNode.setId(200L);
		skillNode.addProperty("name", "Java");

		// Put the relationship first to ensure we don't accidentally treat edges as nodes.
		LinkedHashMap<String, Object> row = new LinkedHashMap<>();
		row.put("r", rel);
		row.put("u", userNode);
		row.put("s", skillNode);
		FalkorDBClient.Record record = new MapBackedRecord(row);

		CapturingClient client = new CapturingClient(new MapBackedRecord(Map.of(
				"target", skillNode,
				"targetId", 200
		)));

		DefaultFalkorDBMappingContext mappingContext = new DefaultFalkorDBMappingContext();
		DefaultFalkorDBEntityConverter converter = new DefaultFalkorDBEntityConverter(mappingContext,
				new EntityInstantiators(), client);

		// Act
		User user = converter.read(User.class, record);

		// Assert: relationship loaded via lazy relationship query using internal id extracted from node object
		assertThat(client.lastParameters).isNotNull();
		assertThat(client.lastParameters.get("sourceId")).isEqualTo(100);

		assertThat(user).isNotNull();
		assertThat(user.email).isEqualTo("john.doe@example.com");
		assertThat(user.skills).isNotNull();
		assertThat(user.skills).hasSize(1);
		assertThat(user.skills.get(0).name).isEqualTo("Java");
	}

	@Test
	void shouldPopulateInternalGeneratedIdFromNodeObjectWhenNotExplicitlyReturned() {
		com.falkordb.graph_entities.Node node = new com.falkordb.graph_entities.Node();
		node.setId(123L);
		node.addProperty("name", "n1");

		FalkorDBClient.Record record = new MapBackedRecord(Map.of("n", node));

		DefaultFalkorDBMappingContext mappingContext = new DefaultFalkorDBMappingContext();
		DefaultFalkorDBEntityConverter converter = new DefaultFalkorDBEntityConverter(mappingContext,
				new EntityInstantiators(), new CapturingClient());

		InternalEntity entity = converter.read(InternalEntity.class, record);

		assertThat(entity).isNotNull();
		assertThat(entity.id).isEqualTo(123L);
		assertThat(entity.name).isEqualTo("n1");
	}

	@Node("User")
	static class User {
		@Id
		String email;

		@Relationship(type = "HAS_SKILL", direction = Relationship.Direction.OUTGOING)
		List<Skill> skills;
	}

	@Node("Skill")
	static class Skill {
		@Id
		String name;
	}

	@Node("InternalEntity")
	static class InternalEntity {
		@Id
		@GeneratedValue
		Long id;

		String name;
	}

	static class CapturingClient implements FalkorDBClient {

		private final List<Record> responseRecords;

		Map<String, Object> lastParameters;

		CapturingClient(Record... records) {
			this.responseRecords = Arrays.asList(records);
		}

		@Override
		public QueryResult query(String query) {
			throw new UnsupportedOperationException();
		}

		@Override
		public QueryResult query(String query, Map<String, Object> parameters) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T query(String query, Function<QueryResult, T> resultMapper) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T query(String query, Map<String, Object> parameters, Function<QueryResult, T> resultMapper) {
			this.lastParameters = parameters;
			return resultMapper.apply(new QueryResult() {
				@Override
				public Iterable<Record> records() {
					return responseRecords;
				}

				@Override
				public boolean hasRecords() {
					return !responseRecords.isEmpty();
				}

				@Override
				public QueryStatistics statistics() {
					return null;
				}
			});
		}

		@Override
		public CompletableFuture<QueryResult> queryAsync(String query) {
			return CompletableFuture.failedFuture(new UnsupportedOperationException());
		}

		@Override
		public CompletableFuture<QueryResult> queryAsync(String query, Map<String, Object> parameters) {
			return CompletableFuture.failedFuture(new UnsupportedOperationException());
		}
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
