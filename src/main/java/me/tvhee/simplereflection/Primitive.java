package me.tvhee.simplereflection;

public enum Primitive
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

	Primitive(Class<?> primitive, Class<?> alternative)
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
		return cast(object) != null;
	}

	public Object cast(Object object)
	{
		try
		{
			switch(this)
			{
				case DOUBLE : return ((Number) object).doubleValue();
				case BYTE : return ((Number) object).byteValue();
				case LONG : return ((Number) object).longValue();
				case FLOAT : return ((Number) object).floatValue();
				case SHORT : return ((Number) object).shortValue();
				case INTEGER : return ((Number) object).intValue();
				case CHARACTER : return (char) object;
				case BOOLEAN : return (boolean) object;
			}

			return true;
		}
		catch(ClassCastException e)
		{
			return false;
		}
	}

	public static Primitive of(Class<?> clazz)
	{
		for(Primitive primitive : Primitive.values())
		{
			if(primitive.primitive.equals(clazz) || primitive.alternative.equals(clazz))
				return primitive;
		}

		return null;
	}

	public static boolean compare(Class<?> clazz1, Class<?> clazz2)
	{
		Primitive primitive1 = of(clazz1);
		Primitive primitive2 = of(clazz2);
		return primitive1 != null && primitive1 == primitive2;
	}
}
