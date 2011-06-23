package com.github.simplejpatemplate;

import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class SimpleJpaTemplate {
	private final NamedParameterJdbcTemplate template;
	private final JdbcJpaHelper helper;

	public SimpleJpaTemplate(NamedParameterJdbcTemplate template) {
		this.template = template;
		this.helper = new JdbcJpaHelper();
	}

	public int persist(Object entity) throws Exception {
		Map<String, Object> values = helper.getInsertParameters(entity);
		String sql = helper.createInsertQuery(null,entity,values);
		return template.update(sql, values);
	}
}
