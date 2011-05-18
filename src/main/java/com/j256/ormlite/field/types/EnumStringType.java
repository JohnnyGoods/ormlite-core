package com.j256.ormlite.field.types;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Type that persists an enum as its string value. You can also use the {@link EnumIntegerType}.
 * 
 * @author graywatson
 */
public class EnumStringType extends BaseEnumType {

	public static int DEFAULT_WIDTH = 100;

	private static final EnumStringType singleTon = new EnumStringType();

	public static EnumStringType createType() {
		return singleTon;
	}

	private EnumStringType() {
		super(SqlType.STRING, new Class<?>[] { Enum.class });
	}

	@Override
	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		String val = results.getString(columnPos);
		if (fieldType == null) {
			return val;
		} else if (val == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Map<String, Enum<?>> enumStringMap = (Map<String, Enum<?>>) fieldType.getDataTypeConfigObj();
		if (enumStringMap == null) {
			return enumVal(fieldType, val, null, fieldType.getUnknownEnumVal());
		} else {
			return enumVal(fieldType, val, enumStringMap.get(val), fieldType.getUnknownEnumVal());
		}
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
		return defaultStr;
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) {
		Enum<?> enumVal = (Enum<?>) obj;
		return enumVal.name();
	}

	@Override
	public Object makeConfigObject(FieldType fieldType) throws SQLException {
		Map<String, Enum<?>> enumStringMap = new HashMap<String, Enum<?>>();
		Enum<?>[] constants = (Enum<?>[]) fieldType.getFieldType().getEnumConstants();
		if (constants == null) {
			throw new SQLException("Field " + fieldType + " improperly configured as type " + this);
		}
		for (Enum<?> enumVal : constants) {
			enumStringMap.put(enumVal.name(), enumVal);
		}
		return enumStringMap;
	}

	@Override
	public int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}
}