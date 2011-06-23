package com.github.simplejpatemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/*
 */
public class JdbcJpaHelper {
	private Logger logger = Logger.getLogger(JdbcJpaHelper.class.getName());

	/**
	 * Parses the JPA annotations on the entity, and executes an INSERT
	 * statement. The generated row identifier is attached to the entity @Id
	 * 
	 * if the entity is an action, the inserts for its many to many relations
	 * are also executed
	 * 
	 * @param template
	 * @param entity
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public int persist(String databaseName,
			NamedParameterJdbcTemplate template, Object entity)
			throws IllegalArgumentException, IllegalAccessException {
		if (entity == null) {
			return 0;
		}

		int result = 0;

		long start = System.currentTimeMillis();

		QueryAndParams q = createInsertQuery(databaseName, entity);

		Field insertableIdField = hasNonInsertableID(entity);
		final int generatedrows;
		if(insertableIdField == null){
			KeyHolder keyHolder = new GeneratedKeyHolder();
			generatedrows = template.update(q.getQuery(), q.getParams(),keyHolder);
			Number nr = keyHolder.getKey();
			if(nr != null){
				setEntityID(entity, nr, insertableIdField);
			}
		}else{
			generatedrows = template.update(q.getQuery(), q.getParams());
		} 

//		if (insertableIdField != null) {
//			String nr = template.queryForObject("select @@IDENTITY",
//					new HashMap<String, Object>(), String.class);
//
//			if (nr != null) {
//				setEntityID(entity, nr, insertableIdField);
//			} else {
//				logger.log(
//						Level.FINE,
//						"Query  did not return a generated key: "
//								+ q.getQuery());
//			}
//		}
		result += generatedrows;
		long end = System.currentTimeMillis();
		long duration = end - start;
		logger.log(Level.FINEST, duration + "ms: " + q.getQuery() + " {params}");
		return result;
	}

	/**
	 * Parses the JPA annotations on the entity, and executes an INSERT
	 * statement. The generated row identifier is attached to the entity @Id
	 * 
	 * if the entity is an action, the inserts for its many to many relations
	 * are also executed
	 * 
	 * @param databaseName
	 * @param entity
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private QueryAndParams createInsertQuery(String databaseName, Object entity)
			throws IllegalArgumentException, IllegalAccessException {
		if (entity == null) {
			return null;
		}

		// Class<?> cl = entity.getClass();
		// List<Field> insertableFields = getInsertableFields(cl);
		Map<String, Object> values = getInsertParameters(entity);

		String sql = createInsertQuery(databaseName, entity, values);

		// KeyHolder keyHolder = new GeneratedKeyHolder();
		SqlParameterSource params = new MapSqlParameterSource(values);
		return new QueryAndParams(sql, params);
	}

	public String createInsertQuery(String databaseName, Object entity,
			Map<String, Object> values) {

		Table table = entity.getClass().getAnnotation(Table.class);
		String tableName = null;
		String tableSchema = null;
		String tablecatalog = databaseName;

		if (table != null) {
			tableName = table.name();
			tableSchema = table.schema();
		}

		if (tablecatalog == null && table != null) {
			tablecatalog = table.catalog();
		}

		StringBuilder builder = new StringBuilder();
		builder.append("INSERT INTO " + tablecatalog + "." + tableSchema + "."
				+ tableName + " ( ");

		Set<String> keys = values.keySet();
		for (String key : keys) {
			builder.append(key + " , ");
		}

		builder.setLength(builder.length() - 2);
		builder.append(" ) VALUES ( ");

		for (String key : keys) {
			builder.append(":" + key + " , ");

		}
		builder.setLength(builder.length() - 2);

		builder.append(" )");

		String sql = builder.toString();
		return sql;
	}

	/**
	 * Creates a <code>Map</code> of all entity properties that can be inserted.
	 * The key is the column name as in the <code>@Column</code> annotation
	 * 
	 * @param entity
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Map<String, Object> getInsertParameters(Object entity)
			throws IllegalArgumentException, IllegalAccessException {
		if (entity == null) {
			return null;
		}
		Entity entityAnnotation = entity.getClass().getAnnotation(Entity.class);
		if (entityAnnotation == null) {
			throw new IllegalArgumentException("@Entity annotation not present");
		}

		List<Field> insertableFields = getInsertableFields(entity.getClass());
		return getValues(entity, insertableFields);
	}

	private Map<String, Object> getValues(Object entity,
			List<Field> insertableFields) throws IllegalArgumentException,
			IllegalAccessException {
		Map<String, Object> values = new HashMap<String, Object>();

		for (Field field : insertableFields) {
			Column col = field.getAnnotation(Column.class);
			// Id idAnnotation = field.getAnnotation(Id.class);
			OneToOne oneToOne = field.getAnnotation(OneToOne.class);
			ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
			JoinColumn join = field.getAnnotation(JoinColumn.class);

			// if (idAnnotation == null && col != null) {
			final String colName;
			if (col != null) {
				colName = col.name();
			} else if (join != null) {
				colName = join.name();
			} else {
				throw new IllegalArgumentException(
						"Unable to define column name for entity " + entity);
			}

			// boolean nullable = col.nullable();
			// boolean insertable = col.insertable();
			field.setAccessible(true);
			Object value = field.get(entity);

			if (oneToOne != null || manyToOne != null || join != null) {
				// This is a relation type
				// Lookup the remote ID field
				Object temp = getEntityID(value);
				if (temp instanceof UUID) {
					value = ((UUID) temp).toString();
				} else {
					value = temp;
				}
			} else if (value != null) {
				if (field.getType() == UUID.class) {
					value = ((UUID) value).toString();
				} else if (field.getType().isEnum()) {
					value = ((Enum) value).ordinal();
				} else if (value instanceof Enum) {
					value = ((Enum) value).ordinal();
				} else if (field.getType() == byte[].class) {
					// TODO
				}
			} else if (value == null) {
				if (field.getType() == byte[].class) {
					value = new byte[0];
				} else if (field.getType() == UUID.class) {
					value = null;
				}
			}

			values.put(colName, value);
		}
		return values;
	}

	private List<Field> getInsertableFields(Class<?> cl)
			throws IllegalArgumentException, IllegalAccessException {
		if (cl == null) {
			return null;
		}

		final List<Field> result = new ArrayList<Field>();
		Field[] fields = cl.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Column col = field.getAnnotation(Column.class);
			Id idAnnotation = field.getAnnotation(Id.class);
			// OneToOne oneToOne = field.getAnnotation(OneToOne.class);
			// ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
			JoinColumn join = field.getAnnotation(JoinColumn.class);

			boolean insertable = isInsertable(col, idAnnotation, join);
			if (insertable) {
				result.add(field);

			}
		}

		return result;
	}

	private boolean isInsertable(Column col, Id idAnnotation, JoinColumn join) {
		boolean insertable = true;
		if (join != null) {
			insertable = join.insertable();
		} else if (idAnnotation != null && col != null
				&& col.insertable() == false) {
			insertable = false;
		} else if (col != null && col.insertable() == false) {
			insertable = false;
		} else if (col != null) {
			insertable = col.insertable();
		} else {
			insertable = false;
		}

		return insertable;
	}

	/**
	 * Returns the Integer value of the JPA @Id annotated field
	 * 
	 * @param entity
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private Object getEntityID(Object entity) throws IllegalArgumentException,
			IllegalAccessException {
		if (entity == null) {
			return null;
		}

		Field field = getIDField(entity);
		if (field != null) {
			field.setAccessible(true);
			Object value = field.get(entity);

			if (value != null) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Copies the given id to the JPA @Id annotated field
	 * 
	 * @param entity
	 * @param id
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void setEntityID(Object entity, Object id, Field idField)
			throws IllegalArgumentException, IllegalAccessException {
		if (entity == null) {
			return;
		}

		if (idField != null) {
			idField.setAccessible(true);
			if(id instanceof Number){
				idField.set(entity, id);
			}
			if (idField.getType() == UUID.class) {
				idField.set(entity, UUID.fromString(id.toString()));
			} else if (idField.getType() == Integer.class) {
				idField.set(entity, Integer.parseInt(id.toString()));
			} else if (idField.getType() == Long.class) {
				idField.set(entity, Long.parseLong(id.toString()));
			}
		}
	}

	/**
	 * Gets the first field with an ID annotation
	 * 
	 * @param entity
	 * @param id
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private Field getIDField(Object entity) throws IllegalArgumentException,
			IllegalAccessException {
		if (entity == null) {
			return null;
		}
		Class<?> cl = entity.getClass();
		Field[] fields = cl.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Id idAnnotation = field.getAnnotation(Id.class);

			if (idAnnotation != null) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Returns the first field with an Id annotation and Column annotation with
	 * insertable set to true
	 * 
	 * @param entity
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private Field hasNonInsertableID(Object entity)
			throws IllegalArgumentException, IllegalAccessException {
		if (entity == null) {
			return null;
		}
		Class<?> cl = entity.getClass();
		Field[] fields = cl.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Id idAnnotation = field.getAnnotation(Id.class);
			Column col = field.getAnnotation(Column.class);

			if (idAnnotation != null && col != null) {
				if (col.insertable() == false) {
					return field;
				}
			}
		}
		return null;
	}
}
