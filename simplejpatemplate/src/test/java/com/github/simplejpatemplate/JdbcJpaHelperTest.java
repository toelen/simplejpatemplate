package com.github.simplejpatemplate;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class JdbcJpaHelperTest {
	@Test
	public void testInsertableValues() throws IllegalArgumentException,
			IllegalAccessException {
		JdbcJpaHelper helper = new JdbcJpaHelper();
		EntityParent parent = new EntityParent();
		parent.setId(1);
		Map<String, Object> values = helper.getInsertValues(parent);
		Assert.assertNotNull(values);

		Assert.assertEquals(1, values.get("idCol"));
	}
}
