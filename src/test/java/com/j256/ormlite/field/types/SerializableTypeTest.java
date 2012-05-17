package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;

public class SerializableTypeTest extends BaseTypeTest {

	private static final String SERIALIZABLE_COLUMN = "serializable";
	private static final String BYTE_COLUMN = "byteField";

	@Test
	public void testSerializable() throws Exception {
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable, Object> dao = createDao(clazz, true);
		Integer val = 1331333131;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
		objOutStream.writeObject(val);
		byte[] sqlArg = outStream.toByteArray();
		String valStr = val.toString();
		LocalSerializable foo = new LocalSerializable();
		foo.serializable = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, sqlArg, valStr, DataType.SERIALIZABLE, SERIALIZABLE_COLUMN, false, false,
				true, false, true, true, false, false);
	}

	@Test
	public void testSerializableNull() throws Exception {
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable, Object> dao = createDao(clazz, true);
		LocalSerializable foo = new LocalSerializable();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.SERIALIZABLE, SERIALIZABLE_COLUMN, false, false,
				true, false, true, true, false, false);
	}

	@Test
	public void testSerializableNoValue() throws Exception {
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable, Object> dao = createDao(clazz, true);
		LocalSerializable foo = new LocalSerializable();
		foo.serializable = null;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadOnlyConnection();
		CompiledStatement stmt = null;
		try {
			stmt = conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			FieldType fieldType =
					FieldType.createFieldType(connectionSource, TABLE_NAME,
							clazz.getDeclaredField(SERIALIZABLE_COLUMN), clazz);
			assertNull(DataType.SERIALIZABLE.getDataPersister().resultToJava(fieldType, results,
					results.findColumn(SERIALIZABLE_COLUMN)));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

	@Test(expected = SQLException.class)
	public void testSerializableInvalidResult() throws Exception {
		Class<LocalByteArray> clazz = LocalByteArray.class;
		Dao<LocalByteArray, Object> dao = createDao(clazz, true);
		LocalByteArray foo = new LocalByteArray();
		foo.byteField = new byte[] { 1, 2, 3, 4, 5 };
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadOnlyConnection();
		CompiledStatement stmt = null;
		try {
			stmt = conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			FieldType fieldType =
					FieldType.createFieldType(connectionSource, TABLE_NAME,
							LocalSerializable.class.getDeclaredField(SERIALIZABLE_COLUMN), LocalSerializable.class);
			DataType.SERIALIZABLE.getDataPersister().resultToJava(fieldType, results, results.findColumn(BYTE_COLUMN));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

	@Test(expected = SQLException.class)
	public void testSerializableParseDefault() throws Exception {
		DataType.SERIALIZABLE.getDataPersister().parseDefaultString(null, null);
	}

	@Test
	public void testUpdateBuilderSerializable() throws Exception {
		Dao<SerializedUpdate, Integer> dao = createDao(SerializedUpdate.class, true);
		SerializedUpdate foo = new SerializedUpdate();
		SerializedField serialized1 = new SerializedField("wow");
		foo.serialized = serialized1;
		assertEquals(1, dao.create(foo));

		SerializedUpdate result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertNotNull(result.serialized);
		assertEquals(serialized1.foo, result.serialized.foo);

		// update with dao.update
		SerializedField serialized2 = new SerializedField("zip");
		foo.serialized = serialized2;
		assertEquals(1, dao.update(foo));

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertNotNull(result.serialized);
		assertEquals(serialized2.foo, result.serialized.foo);

		// update with UpdateBuilder
		SerializedField serialized3 = new SerializedField("crack");
		UpdateBuilder<SerializedUpdate, Integer> ub = dao.updateBuilder();
		ub.updateColumnValue(SerializedUpdate.SERIALIZED_FIELD_NAME, serialized3);
		ub.where().idEq(foo.id);
		assertEquals(1, ub.update());

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertNotNull(result.serialized);
		assertEquals(serialized3.foo, result.serialized.foo);
	}

	/* ------------------------------------------------------------------------------------ */

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalSerializable {
		@DatabaseField(columnName = SERIALIZABLE_COLUMN, dataType = DataType.SERIALIZABLE)
		Integer serializable;;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByteArray {
		@DatabaseField(columnName = BYTE_COLUMN, dataType = DataType.BYTE_ARRAY)
		byte[] byteField;
	}

	protected static class SerializedUpdate {
		public final static String SERIALIZED_FIELD_NAME = "serialized";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(dataType = DataType.SERIALIZABLE, columnName = SERIALIZED_FIELD_NAME)
		public SerializedField serialized;
		public SerializedUpdate() {
		}
	}

	protected static class SerializedField implements Serializable {
		private static final long serialVersionUID = 4531762180289888888L;
		String foo;
		public SerializedField(String foo) {
			this.foo = foo;
		}
	}
}
