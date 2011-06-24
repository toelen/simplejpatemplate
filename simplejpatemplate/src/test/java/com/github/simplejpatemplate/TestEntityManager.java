package com.github.simplejpatemplate;

import java.io.File;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class TestEntityManager {
	private EntityManager entityManager;

	@Before
	public void setUp() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource
				.setDriverClassName(org.apache.derby.jdbc.EmbeddedDriver.class
						.getName());
		dataSource.setUrl("jdbc:derby:memory:InMemoryDb;create=true");

		NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(
				dataSource);

		template.update(
				"create table tablename (idCol int, colone varchar(25) )",
				new MapSqlParameterSource());

		entityManager = new SimpleJpaEntityManager(template);
	}

	@Test
	public void testPersist() {
		EntityParent entity = new EntityParent();
		entity.setId(10);
		entity.setFieldOne("fieldOne");

		entityManager.persist(entity);

		EntityParent found = entityManager.find(EntityParent.class,
				entity.getId());
		Assert.assertNotNull(found);
		Assert.assertEquals(entity.getId(), found.getId());
		Assert.assertEquals(entity.getFieldOne(), found.getFieldOne());
	}

}
