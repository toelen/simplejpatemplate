package com.github.simplejpatemplate.rowmapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.springframework.jdbc.core.RowMapper;

/**
 * A Spring <code>RowMapper</code> which uses the JPA annotations to map a
 * <code>ResultSet</code> to an object
 * 
 * @author toelen
 * @see RowMapper
 * @param <T>
 */
public class JpaRowMapper<T> implements RowMapper<T> {
	private Class<T> cl;

	private Field[] fields;
	private HashMap<Field, String> colNames = new HashMap<Field, String>();;
	private HashMap<Field, OneToOne> oneToOnes = new HashMap<Field, OneToOne>();
	private HashMap<Field, ManyToOne> manyToOnes = new HashMap<Field, ManyToOne>();
	private HashMap<Field, OneToMany> oneToManies = new HashMap<Field, OneToMany>();
	private HashMap<Field, ManyToMany> manyToManies = new HashMap<Field, ManyToMany>();
	private Id idAnnotation;

	public JpaRowMapper(Class<T> cl) {
		this.cl = cl;

		fields = cl.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			field.setAccessible(true);

			Column col = field.getAnnotation(Column.class);
			String colName = "";
			if (col != null) {
				colName = col.name();

				colNames.put(field, colName);
			} else {
				colName = field.getName();
				colNames.put(field, colName);
			}

			if (idAnnotation == null) {
				idAnnotation = field.getAnnotation(Id.class);
			}

			OneToOne oneToOne = field.getAnnotation(OneToOne.class);
			if (oneToOne != null) {
				oneToOnes.put(field, oneToOne);
			}
			ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
			if (manyToOne != null) {
				manyToOnes.put(field, manyToOne);
			}
			OneToMany oneToMany = field.getAnnotation(OneToMany.class);
			if (oneToMany != null) {
				oneToManies.put(field, oneToMany);
			}
			ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
			if (manyToMany != null) {
				manyToManies.put(field, manyToMany);
			}
		}
	}

	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		T result = null;
		try {
			result = cl.newInstance();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		if (result == null) {
			return null;
		}

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			// Column col = field.getAnnotation(Column.class);
			OneToOne oneToOne = oneToOnes.get(field);
			ManyToOne manyToOne = manyToOnes.get(field);
			OneToMany oneToMany = oneToManies.get(field);
			ManyToMany manyToMany = manyToManies.get(field);

			try {
				String colName = colNames.get(field);

				Object value = null;
				if (oneToMany != null) {
					// Nothing to do. Work the other way around
				} else if (manyToMany != null) {
					// Nothing to do. Work the other way around
				} else if (manyToOne != null || oneToOne != null) {
					// Nothing to do. Work the other way around
				} else if (field.getType() == UUID.class) {
					String colValue = rs.getString(colName);
					UUID uuid = UUID.fromString(colValue);
					value = uuid;
				} else if (field.getType() == Integer.class) {
					Integer colValue = rs.getInt(colName);
					value = colValue;
				} else if (field.getType() == Long.class) {
					Long colValue = rs.getLong(colName);
					value = colValue;
				} else if (field.getType() == Calendar.class) {
					// Date colDValue = rs.getDate(colName);
					Timestamp colValue = rs.getTimestamp(colName);
					if (colValue != null) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(colValue);
						value = cal;
					}
				} else if (field.getType() == String.class) {
					String colValue = rs.getString(colName);
					value = colValue;
				} else if (field.getType() == byte[].class) {
					byte[] colValue = rs.getBytes(colName);
					value = colValue;
				}

				field.set(result, value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();

			} catch (SQLException sqlex) {
				if (sqlex.getMessage().startsWith("Invalid column name") == false) {
					sqlex.printStackTrace();
				}
			}

		}

		return result;
	}
}
