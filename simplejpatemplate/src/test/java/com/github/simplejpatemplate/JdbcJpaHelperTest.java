package com.github.simplejpatemplate;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.simplejpatemplate.util.JdbcJpaHelper;

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
		EntityChild child = new EntityChild();
		parent.setChild(child);
		child.setIdField("id");
		child.setParent(parent);

		Map<String, Object> values = helper.getInsertParameters(parent);
		Assert.assertNotNull(values);
		Assert.assertNotNull(values.get("idCol"));
		String insertQuery = helper.createInsertQuery(null, parent, values);
		Assert.assertNotNull(insertQuery);
		Assert.assertEquals(
				"INSERT INTO catalog.schema.name ( idCol , colone  ) VALUES ( :idCol , :colone  )",
				insertQuery);

		values = helper.getInsertParameters(child);
		Assert.assertNotNull(values);
		Assert.assertNotNull("idField is null", values.get("idField"));
		insertQuery = helper.createInsertQuery(null, child, values);
		Assert.assertNotNull(insertQuery);
		Assert.assertEquals(
				"INSERT INTO child ( somecol , idField  ) VALUES ( :somecol , :idField  )",
				insertQuery);
	}
}
