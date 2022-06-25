package me.tvhee.simplereflection;

import java.lang.reflect.InvocationTargetException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class UnsafeField
{
	private static Unsafe unsafe;
	private final Primitive fieldClass;
	private final Field field;
	private long offset;
	private Object fieldBase;
	private boolean unsupported;

	public UnsafeField(Field field)
	{
		if(unsafe == null)
		{
			try
			{
				Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
				unsafeField.setAccessible(true);
				unsafe = (Unsafe) unsafeField.get(null);
			}
			catch(NoSuchFieldException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

		this.fieldClass = Primitive.of(field.getType());
		this.field = field;

		try
		{
			this.unsupported = (boolean) Class.class.getMethod("isRecord").invoke(field.getDeclaringClass());

			if(this.unsupported)
				field.setAccessible(true);
		}
		catch(IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch(NoSuchMethodException ignored)
		{
		}
	}

	public boolean isSupported()
	{
		return !unsupported;
	}

	public Field getJavaField()
	{
		return field;
	}

	public void setFieldValue(Object instance, Object value)
	{
		if(unsupported)
			throw new UnsupportedOperationException("Record classes are not supported!");

		initializeField(instance);

		if(!field.getType().isInstance(value) && value != null)
			throw new IllegalArgumentException(value + " is not an instance of " + field.getType() + "!");

		if(fieldClass == null)
		{
			putObject(value);
		}
		else if(fieldClass.isInstance(value))
		{
			value = fieldClass.cast(value);

			switch(fieldClass)
			{
				case BYTE :
				{
					putByte((byte) value);
					break;
				}
				case SHORT :
				{
					putShort((short) value);
					break;
				}
				case INTEGER :
				{
					putInt((int) value);
					break;
				}
				case LONG :
				{
					putLong((long) value);
					break;
				}
				case FLOAT :
				{
					putFloat((float) value);
					break;
				}
				case DOUBLE :
				{
					putDouble((double) value);
					break;
				}
				case BOOLEAN :
				{
					putBoolean((boolean) value);
					break;
				}
				case CHARACTER :
				{
					putChar((char) value);
					break;
				}
			}
		}
	}

	public Object getFieldValue(Object instance)
	{
		if(unsupported)
			throw new UnsupportedOperationException("Record classes are not supported!");

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
		return unsafe.getIntVolatile(fieldBase, offset);
	}

	private void putInt(int toPut)
	{
		unsafe.putIntVolatile(fieldBase, offset, toPut);
	}

	private Object getObject()
	{
		return unsafe.getObjectVolatile(fieldBase, offset);
	}

	private void putObject(Object toPut)
	{
		unsafe.putObjectVolatile(fieldBase, offset, toPut);
	}

	private boolean getBoolean()
	{
		return unsafe.getBooleanVolatile(fieldBase, offset);
	}

	private void putBoolean(boolean toPut)
	{
		unsafe.putBooleanVolatile(fieldBase, offset, toPut);
	}

	private byte getByte()
	{
		return unsafe.getByteVolatile(fieldBase, offset);
	}

	private void putByte(byte toPut)
	{
		unsafe.putByteVolatile(fieldBase, offset, toPut);
	}

	private short getShort()
	{
		return unsafe.getShortVolatile(fieldBase, offset);
	}

	private void putShort(short toPut)
	{
		unsafe.putShortVolatile(fieldBase, offset, toPut);
	}

	private char getChar()
	{
		return unsafe.getCharVolatile(fieldBase, offset);
	}

	private void putChar(char toPut)
	{
		unsafe.putCharVolatile(fieldBase, offset, toPut);
	}

	private long getLong()
	{
		return unsafe.getLongVolatile(fieldBase, offset);
	}

	private void putLong(long toPut)
	{
		unsafe.putLongVolatile(fieldBase, offset, toPut);
	}

	private float getFloat()
	{
		return unsafe.getFloatVolatile(fieldBase, offset);
	}

	private void putFloat(float toPut)
	{
		unsafe.putFloatVolatile(fieldBase, offset, toPut);
	}

	private double getDouble()
	{
		return unsafe.getDoubleVolatile(fieldBase, offset);
	}

	private void putDouble(double toPut)
	{
		unsafe.putDoubleVolatile(fieldBase, offset, toPut);
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
