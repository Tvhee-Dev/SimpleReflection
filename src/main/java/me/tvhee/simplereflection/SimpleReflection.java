package me.tvhee.simplereflection;

import java.lang.reflect.*;

public class SimpleReflection
{
	private final Class<?> clazz;
	private Object instance;

	public SimpleReflection(Object instance)
	{
		if(instance == null)
			throw new IllegalArgumentException("instance == null");

		this.instance = instance;
		this.clazz = instance.getClass();
	}

	public SimpleReflection(Class<?> clazz)
	{
		if(clazz == null)
			throw new IllegalArgumentException("clazz == null");

		this.clazz = clazz;
	}

	public Class<?> getReflectedClass()
	{
		return clazz;
	}

	public Object getObjectInstance()
	{
		return instance;
	}

	public <T> T getObjectInstance(Class<T> expectedType)
	{
		PrimitiveClass primitiveClass = PrimitiveClass.of(expectedType);
		Class<?> lookingUp = expectedType;

		if(primitiveClass != null)
			lookingUp = primitiveClass.getAlternative();

		if(lookingUp.isInstance(this.instance))
			return (T) instance;
		
		return null;
	}

	public SimpleReflection setObjectInstance(Object instance)
	{
		this.instance = instance;
		return this;
	}

	public SimpleReflection setField(Class<?> fieldClass, Object value) throws ReflectException
	{
		return setField(fieldClass, 0, value);
	}

	public SimpleReflection setField(Class<?> fieldClass, int index, Object value) throws ReflectException
	{
		SimpleField field = getField0(fieldClass, index);

		if(field != null)
			field.setFieldValue(instance, value);

		return this;
	}

	public SimpleReflection setField(String fieldName, Object value) throws ReflectException
	{
		SimpleField field = getField0(fieldName);

		if(field != null)
			field.setFieldValue(instance, value);

		return this;
	}

	public boolean hasField(Class<?> fieldClass)
	{
		return hasField(fieldClass, 0);
	}

	public SimpleReflection getField(Class<?> fieldClass) throws ReflectException
	{
		return getField(fieldClass, 0);
	}

	public boolean hasField(Class<?> fieldClass, int index)
	{
		try
		{
			return getField(fieldClass, index) != null;
		}
		catch(ReflectException ignored)
		{
			return false;
		}
	}

	public SimpleReflection getField(Class<?> fieldClass, int index) throws ReflectException
	{
		SimpleField field = getField0(fieldClass, index);

		if(field == null)
			return null;

		return checkAndReturn(field.getFieldValue(instance));
	}

	public boolean hasField(String fieldName)
	{
		try
		{
			return getField(fieldName) != null;
		}
		catch(ReflectException ignored)
		{
			return false;
		}
	}

	public SimpleReflection getField(String fieldName) throws ReflectException
	{
		SimpleField field = getField0(fieldName);

		if(field == null)
			return null;

		return checkAndReturn(field.getFieldValue(instance));
	}

	public SimpleReflection invokeMethod(Class<?> returnType, Object... parameters) throws ReflectException
	{
		return invokeMethod(returnType, 0, parameters);
	}

	private SimpleField getField0(Class<?> fieldClass, int index) throws ReflectException
	{
		int currentIndex = -1;

		for(Class<?> superclass : ReflectionProvider.getSuperClasses(clazz))
		{
			for(Field field : superclass.getDeclaredFields())
			{
				if(ReflectionProvider.classEquals(field.getType(), fieldClass))
				{
					currentIndex++;

					if(currentIndex == index)
					{
						if(!Modifier.isStatic(field.getModifiers()) && instance == null)
							throw new ReflectException(ReflectException.ReflectExceptionCause.NOT_STATIC, field, null);

						field.setAccessible(true);
						return new SimpleField(field);
					}
				}
			}
		}

		return null;
	}

	private SimpleField getField0(String name) throws ReflectException
	{
		for(Class<?> superclass : ReflectionProvider.getSuperClasses(clazz))
		{
			for(Field field : superclass.getDeclaredFields())
			{
				if(field.getName().equals(name))
				{
					if(!Modifier.isStatic(field.getModifiers()) && instance == null)
						throw new ReflectException(ReflectException.ReflectExceptionCause.NOT_STATIC, field, null);

					field.setAccessible(true);
					return new SimpleField(field);
				}
			}
		}

		return null;
	}

	public SimpleReflection invokeMethod(String name, Object... parameters) throws ReflectException
	{
		for(Class<?> superClass : ReflectionProvider.getSuperClasses(clazz))
		{
			for(Method method : superClass.getDeclaredMethods())
			{
				if(method.getName().equals(name) && ReflectionProvider.parametersEquals(ReflectionProvider.getClasses(parameters), method.getParameterTypes()))
					return invokeMethod0(method, parameters);
			}
		}

		return null;
	}

	public SimpleReflection invokeMethod(Class<?> returnType, int index, Object[] parameters) throws ReflectException
	{
		int currentIndex = -1;

		for(Class<?> superClass : ReflectionProvider.getSuperClasses(clazz))
		{
			for(Method method : superClass.getDeclaredMethods())
			{
				if(ReflectionProvider.classEquals(method.getReturnType(), returnType) && ReflectionProvider.parametersEquals(method.getParameterTypes(), ReflectionProvider.getClasses(parameters)))
				{
					currentIndex++;

					if(currentIndex == index)
						return invokeMethod0(method, parameters);
				}
			}
		}

		return null;
	}

	private SimpleReflection invokeMethod0(Method method, Object[] parameters) throws ReflectException
	{
		if(!Modifier.isStatic(method.getModifiers()) && instance == null)
			throw new ReflectException(ReflectException.ReflectExceptionCause.NOT_STATIC, method, null);

		try
		{
			method.setAccessible(true);
			return checkAndReturn(method.invoke(instance, parameters));
		}
		catch(IllegalAccessException | InvocationTargetException e)
		{
			throw new ReflectException(ReflectException.ReflectExceptionCause.WRONG_PARAMETERS, method, e);
		}
	}

	public SimpleReflection invokeConstructor(Object... parameters) throws ReflectException
	{
		for(Constructor<?> constructor : clazz.getDeclaredConstructors())
		{
			if(ReflectionProvider.parametersEquals(constructor.getParameterTypes(), ReflectionProvider.getClasses(parameters)))
			{
				try
				{
					constructor.setAccessible(true);
					return checkAndReturn(constructor.newInstance(parameters));
				}
				catch(IllegalAccessException | InstantiationException | InvocationTargetException e)
				{
					throw new ReflectException(ReflectException.ReflectExceptionCause.WRONG_PARAMETERS, constructor, e);
				}
			}
		}

		return null;
	}

	@Override
	public String toString()
	{
		return "SimpleReflection{" + "class=" + clazz + ", instance=" + instance + '}';
	}

	private SimpleReflection checkAndReturn(Object value)
	{
		if(value == null)
			return null;

		return new SimpleReflection(value);
	}
}
