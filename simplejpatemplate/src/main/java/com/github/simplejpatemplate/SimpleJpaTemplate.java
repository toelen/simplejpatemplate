package com.github.simplejpatemplate;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class SimpleJpaTemplate {
	private final NamedParameterJdbcTemplate template;
	private final JdbcJpaHelper helper;

	public SimpleJpaTemplate(NamedParameterJdbcTemplate template) {
		this.template = template;
		this.helper = new JdbcJpaHelper();
	}

	public int persist(Object entity) throws Exception {
		QueryAndParams q = helper.createInsertQuery(null, entity);
		return template.update(q.getQuery(), q.getParams());
	}
}
