package me.tvhee.simplereflection;

public enum PrimitiveClass
{
	BYTE(byte.class, Byte.class),
	SHORT(short.class, Short.class),
	INTEGER(int.class, Integer.class),
	LONG(long.class, Long.class),
	FLOAT(float.class, Float.class),
	DOUBLE(double.class, Double.class),
	BOOLEAN(boolean.class, Boolean.class),
	CHARACTER(char.class, Character.class);

	private final Class<?> primitive;
	private final Class<?> alternative;

	PrimitiveClass(Class<?> primitive, Class<?> alternative)
	{
		this.primitive = primitive;
		this.alternative = alternative;
	}

	public Class<?> getPrimitive()
	{
		return primitive;
	}

	public Class<?> getAlternative()
	{
		return alternative;
	}

	public boolean isInstance(Object object)
	{
		return primitive.equals(object.getClass()) || alternative.equals(object.getClass());
	}

	public boolean compare(Class<?> test1, Class<?> test2)
	{
		return (test1.equals(primitive) || test1.equals(alternative)) && (test2.equals(primitive) || test2.equals(alternative));
	}

	public static boolean compareAll(Class<?> test1, Class<?> test2)
	{
		for(PrimitiveClass primitiveClass : PrimitiveClass.values())
		{
			if(primitiveClass.compare(test1, test2))
				return true;
		}

		return false;
	}

	public static PrimitiveClass of(Class<?> clazz)
	{
		for(PrimitiveClass primitiveClass : PrimitiveClass.values())
		{
			if(primitiveClass.primitive.equals(clazz) || primitiveClass.alternative.equals(clazz))
				return primitiveClass;
		}

		return null;
	}
}
