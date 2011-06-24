package com.github.simplejpatemplate;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class JdbcJpaHelperTest {
	@Test
	public void testInsertableValues() throws Exception {
		JdbcJpaHelper helper = new JdbcJpaHelper();
		EntityParent parent = new EntityParent();
		parent.setId(1);
		Map<String, Object> values = helper.getInsertParameters(parent);
		Assert.assertNotNull(values);

		Assert.assertEquals(1, values.get("idCol"));
	}

	@Test
	public void testDeleteQuery() throws Exception {
		JdbcJpaHelper helper = new JdbcJpaHelper();
		EntityParent parent = new EntityParent();
		parent.setId(1);
		String deleteQuery = helper.createDeleteQuery(EntityParent.class,
				parent.getId(), null);
		Assert.assertNotNull(deleteQuery);
		Assert.assertEquals(
				"DELETE FROM catalog.schema.name WHERE idCol = :id",
				deleteQuery);
	}

	@Test
	public void testInsertQuery() throws Exception {
		JdbcJpaHelper helper = new JdbcJpaHelper();
		EntityParent parent = new EntityParent();
		parent.setId(1);

		Map<String, Object> values = helper.getInsertParameters(parent);
		Assert.assertNotNull(values);
		Assert.assertNotNull(values.get("idCol"));
		String insertQuery = helper.createInsertQuery(null, parent, values);
		Assert.assertNotNull(insertQuery);
		Assert.assertEquals(
				"INSERT INTO catalog.schema.name ( idCol , colone  ) VALUES ( :idCol , :colone  )",
				insertQuery);
	}
}
