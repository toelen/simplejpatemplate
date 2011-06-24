package com.github.simplejpatemplate;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class SimpleJpaEntityManagerFactory implements EntityManagerFactory {
	private final NamedParameterJdbcTemplate template;

	public SimpleJpaEntityManagerFactory(NamedParameterJdbcTemplate template) {
		this.template = template;
	}

	public EntityManager createEntityManager() {
		return new SimpleJpaEntityManager(template);
	}

	public EntityManager createEntityManager(Map map) {
		return createEntityManager();
	}

	public void close() {
	}

	public boolean isOpen() {
		return true;
	}

}
