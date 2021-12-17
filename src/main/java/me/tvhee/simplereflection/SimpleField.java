package me.tvhee.simplereflection;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SimpleField
{
	private static Unsafe unsafe;
	private final PrimitiveClass fieldClass;
	private final Field field;
	private long offset;
	private Object fieldBase;

	public SimpleField(Field field)
	{
		if(unsafe == null)
		{
			try
			{
				final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
				unsafeField.setAccessible(true);
				unsafe = (Unsafe) unsafeField.get(null);
			}
			catch(NoSuchFieldException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

		this.fieldClass = PrimitiveClass.of(field.getType());
		this.field = field;
	}

	public Field getJavaField()
	{
		return field;
	}

	public void setFieldValue(Object instance, Object value) throws ReflectException
	{
		initializeField(instance);

		if(fieldClass == null)
		{
			putObject(value);
		}
		else if(fieldClass.isInstance(value))
		{
			switch(fieldClass)
			{
				case BYTE : putByte((byte) value);
				case SHORT : putShort((short) value);
				case INTEGER : putInt((int) value);
				case LONG : putLong((long) value);
				case FLOAT : putFloat((float) value);
				case DOUBLE : putDouble((double) value);
				case BOOLEAN : putBoolean((boolean) value);
				case CHARACTER : putChar((char) value);
			}
		}
		else
			throw new ReflectException(ReflectException.ReflectExceptionCause.WRONG_PARAMETERS, field, null);
	}

	public Object getFieldValue(Object instance)
	{
		initializeField(instance);

		if(fieldClass == null)
			return getObject();

		switch(fieldClass)
		{
			case BYTE : return getByte();
			case SHORT : return getShort();
			case INTEGER : return getInt();
			case LONG : return getLong();
			case FLOAT : return getFloat();
			case DOUBLE : return getDouble();
			case BOOLEAN : return getBoolean();
			case CHARACTER : return getChar();
		}

		return null;
	}

	private int getInt()
	{
		return unsafe.getInt(fieldBase, offset);
	}

	private void putInt(int toPut)
	{
		unsafe.putInt(fieldBase, offset, toPut);
	}

	private Object getObject()
	{
		return unsafe.getObject(fieldBase, offset);
	}

	private void putObject(Object toPut)
	{
		unsafe.putObject(fieldBase, offset, toPut);
	}

	private boolean getBoolean()
	{
		return unsafe.getBoolean(fieldBase, offset);
	}

	private void putBoolean(boolean toPut)
	{
		unsafe.putBoolean(fieldBase, offset, toPut);
	}

	private byte getByte()
	{
		return unsafe.getByte(fieldBase, offset);
	}

	private void putByte(byte toPut)
	{
		unsafe.putByte(fieldBase, offset, toPut);
	}

	private short getShort()
	{
		return unsafe.getShort(fieldBase, offset);
	}

	private void putShort(short toPut)
	{
		unsafe.putShort(fieldBase, offset, toPut);
	}

	private char getChar()
	{
		return unsafe.getChar(fieldBase, offset);
	}

	private void putChar(char toPut)
	{
		unsafe.putChar(fieldBase, offset, toPut);
	}

	private long getLong()
	{
		return unsafe.getLong(fieldBase, offset);
	}

	private void putLong(long toPut)
	{
		unsafe.putLong(fieldBase, offset, toPut);
	}

	private float getFloat()
	{
		return unsafe.getFloat(fieldBase, offset);
	}

	private void putFloat(float toPut)
	{
		unsafe.putFloat(fieldBase, offset, toPut);
	}

	private double getDouble()
	{
		return unsafe.getDouble(fieldBase, offset);
	}

	private void putDouble(double toPut)
	{
		unsafe.putDouble(fieldBase, offset, toPut);
	}

	private void initializeField(Object instance)
	{
		if(Modifier.isStatic(field.getModifiers()))
		{
			this.fieldBase = unsafe.staticFieldBase(field);
			this.offset = unsafe.staticFieldOffset(field);
		}
		else if(instance == null)
		{
			throw new IllegalArgumentException("Instance is null!");
		}
		else
		{
			this.fieldBase = instance;
			this.offset = unsafe.objectFieldOffset(field);
		}
	}
}
