package com.github.simplejpatemplate.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Utility class for creating SQL queries using JPA annotations
 */
public class JdbcJpaHelper {
	/**
	 * Creates a select query using the primaryKey in the where clause. A
	 * parameter named :id should be provided when executing the query
	 * 
	 * @param type
	 * @param primaryKey
	 * @param databaseName
	 * @return
	 * @throws Exception
	 */
	public String createSelectQuery(Class<?> type, Object primaryKey,
			String databaseName) throws Exception {
		String tableName = getFullyQualifiedTableName(type, databaseName);

		Field idField = getIDField(type);
		if (idField == null) {
			throw new IllegalArgumentException(
					"@Id field could not be resolved");
		}

		String colName = getColumnName(idField);
		if (colName == null || colName.equals("")) {
			throw new IllegalArgumentException(
					"Id column name could not be resolved");
		}

		StringBuilder builder = new StringBuilder();
		builder.append("SELECT * FROM ");
		builder.append(tableName);
		builder.append(" WHERE ");
		builder.append(colName);
		builder.append(" = :id");

		return builder.toString();
	}

	public String createDeleteQuery(Class<?> type, Object id,
			String databaseName) throws Exception {
		String tableName = getFullyQualifiedTableName(type, databaseName);

		Field idField = getIDField(type);
		if (idField == null) {
			throw new IllegalArgumentException(
					"@Id field could not be resolved");
		}

		String colName = getColumnName(idField);
		if (colName == null || colName.equals("")) {
			throw new IllegalArgumentException(
					"Id column name could not be resolved");
		}

		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM ");
		builder.append(tableName);
		builder.append(" WHERE ");
		builder.append(colName);
		builder.append(" = :id");

		return builder.toString();
	}

	/**
	 * Creates a column name according to the JPA spec
	 * 
	 * @param field
	 * @return
	 */
	private String getColumnName(Field field) {
		Column col = field.getAnnotation(Column.class);
		if (col != null && col.name() != null) {
			return col.name();
		}

		JoinColumn join = field.getAnnotation(JoinColumn.class);
		if (join != null && join.name() != null) {
			return join.name();
		}
		return field.getName();
	}

	/**
	 * Returns the fully qualified table name (catalog.schema.table) using the
	 * {@link Table} annotation
	 * 
	 * @param cl
	 * @param databaseName
	 *            Overrides the @Table catalog when non-null
	 * @return
	 */
	private String getFullyQualifiedTableName(Class<?> cl, String databaseName) {
		Table table = cl.getAnnotation(Table.class);
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

		if (tableName == null || tableName.equals("")) {
			throw new IllegalArgumentException(
					"Table name could not be resolved from @Table");
		}

		StringBuilder builder = new StringBuilder();

		if (tablecatalog != null && tablecatalog.equals("") == false) {
			builder.append(tablecatalog + ".");
		}
		if (tableSchema != null && tableSchema.equals("") == false) {
			builder.append(tableSchema + ".");
		}
		builder.append(tableName);

		return builder.toString();
	}

	/**
	 * Creates an insert statement
	 * 
	 * @param databaseName
	 *            Overrides the @Table catalog property
	 * @param entity
	 * @param values
	 * @return
	 */
	public String createInsertQuery(String databaseName, Object entity,
			Map<String, Object> values) {

		String tableName = getFullyQualifiedTableName(entity.getClass(),
				databaseName);

		StringBuilder builder = new StringBuilder();
		builder.append("INSERT INTO ");
		builder.append(tableName);
		builder.append(" ( ");

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
	 * Creates a {@link Map} of all entity properties that can be inserted. The
	 * key is the column name as in the {@link Column} annotation
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

			final String colName = getColumnName(field);
			if (colName == null) {
				throw new IllegalArgumentException(
						"Unable to define column name for entity " + entity);
			}

			Object value = getColumnValue(entity, field);

			values.put(colName, value);
		}
		return values;
	}

	private Object getColumnValue(Object entity, Field field)
			throws IllegalAccessException {

		field.setAccessible(true);
		Object value = field.get(entity);

		OneToOne oneToOne = field.getAnnotation(OneToOne.class);
		ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
		JoinColumn join = field.getAnnotation(JoinColumn.class);

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
		return value;
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
		} else if (idAnnotation != null) {
			insertable = true;
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
	public Object getEntityID(Object entity) throws IllegalArgumentException,
			IllegalAccessException {
		if (entity == null) {
			return null;
		}

		Field field = getIDField(entity.getClass());
		if (field != null) {
			field.setAccessible(true);
			Object value = field.get(entity);

			if (value != null) {
				return value;
			}
		}
		return null;
	}

	// /**
	// * Copies the given id to the JPA @Id annotated field
	// *
	// * @param entity
	// * @param id
	// * @throws IllegalArgumentException
	// * @throws IllegalAccessException
	// */
	// private void setEntityID(Object entity, Object id, Field idField)
	// throws IllegalArgumentException, IllegalAccessException {
	// if (entity == null) {
	// return;
	// }
	//
	// if (idField != null) {
	// idField.setAccessible(true);
	// if (id instanceof Number) {
	// idField.set(entity, id);
	// }
	// if (idField.getType() == UUID.class) {
	// idField.set(entity, UUID.fromString(id.toString()));
	// } else if (idField.getType() == Integer.class) {
	// idField.set(entity, Integer.parseInt(id.toString()));
	// } else if (idField.getType() == Long.class) {
	// idField.set(entity, Long.parseLong(id.toString()));
	// }
	// }
	// }

	/**
	 * Gets the first field with an ID annotation
	 * 
	 * @param entity
	 * @param id
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private Field getIDField(Class<?> cl) throws IllegalArgumentException,
			IllegalAccessException {
		if (cl == null) {
			return null;
		}

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
