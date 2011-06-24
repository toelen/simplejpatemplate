package com.github.simplejpatemplate;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.github.simplejpatemplate.rowmapper.JpaRowMapper;
import com.github.simplejpatemplate.util.JdbcJpaHelper;

/**
 * A very simple <code>EntityManager</code> using a Spring
 * <code>NamedParameterJdbcTemplate</code> for JDBC access
 * 
 * @author leen toelen
 * 
 */
public class SimpleJpaEntityManager implements EntityManager {
	private final NamedParameterJdbcTemplate template;
	private final JdbcJpaHelper helper;

	public SimpleJpaEntityManager(NamedParameterJdbcTemplate template) {
		this.template = template;
		this.helper = new JdbcJpaHelper();
	}

	public void persist(Object entity) {
		try {
			Map<String, Object> values = helper.getInsertParameters(entity);
			String sql = helper.createInsertQuery(null, entity, values);
			template.update(sql, values);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public <T> T merge(T entity) {
		try {
			Object primaryKey = helper.getEntityID(entity);
			Class<T> cl = (Class<T>) entity.getClass();
			T existing = (T) find(cl, primaryKey);
			if (existing == null) {
				persist(entity);
				return entity;
			} else {
				throw new RuntimeException("Not implemented: update");
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void remove(Object entity) {
		try {
			Object primaryKey = helper.getEntityID(entity);
			String sql = helper.createDeleteQuery(entity.getClass(),
					primaryKey, null);
			SqlParameterSource params = new MapSqlParameterSource("id",
					primaryKey);

			template.update(sql, params);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public <T> T find(Class<T> entityClass, Object primaryKey) {
		try {
			String sql = helper
					.createSelectQuery(entityClass, primaryKey, null);
			SqlParameterSource params = new MapSqlParameterSource("id",
					primaryKey);
			RowMapper<T> rowMapper = new JpaRowMapper<T>(entityClass);
			return template.queryForObject(sql, params, rowMapper);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		throw new RuntimeException("Not implemented");
	}

	public void flush() {

	}

	public void setFlushMode(FlushModeType flushMode) {
		throw new RuntimeException("Not implemented");
	}

	public FlushModeType getFlushMode() {
		throw new RuntimeException("Not implemented");
	}

	public void lock(Object entity, LockModeType lockMode) {
		throw new RuntimeException("Not implemented");
	}

	public void refresh(Object entity) {
		throw new RuntimeException("Not implemented");
	}

	public void clear() {
	}

	public boolean contains(Object entity) {
		throw new RuntimeException("Not implemented");

	}

	public Query createQuery(String qlString) {
		throw new RuntimeException("Not implemented");
	}

	public Query createNamedQuery(String name) {
		throw new RuntimeException("Not implemented");
	}

	public Query createNativeQuery(String sqlString) {
		throw new RuntimeException("Not implemented");
	}

	public Query createNativeQuery(String sqlString, Class resultClass) {
		throw new RuntimeException("Not implemented");
	}

	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		throw new RuntimeException("Not implemented");
	}

	public void joinTransaction() {
		throw new RuntimeException("Not implemented");
	}

	public Object getDelegate() {
		throw new RuntimeException("Not implemented");
	}

	public void close() {
		throw new RuntimeException("Not implemented");
	}

	public boolean isOpen() {
		throw new RuntimeException("Not implemented");
	}

	public EntityTransaction getTransaction() {
		throw new RuntimeException("Not implemented");
	}

}
