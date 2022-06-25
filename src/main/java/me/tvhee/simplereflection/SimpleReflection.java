package me.tvhee.simplereflection;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
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

	public List<Class<?>> getGenericClasses()
	{
		List<Class<?>> classes = new ArrayList<>();

		for(Type type : ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments())
			classes.add((Class<?>) type);

		return classes;
	}

	public String getObjectInstanceString()
	{
		return String.valueOf(this.instance);
	}

	public <T> T getObjectInstance()
	{
		try
		{
			return (T) this.instance;
		}
		catch(ClassCastException e)
		{
			return null;
		}
	}

	public SimpleReflection setObjectInstance(Object instance)
	{
		this.instance = instance;
		return this;
	}

	public Map<String, Class<?>> getFields()
	{
		Map<String, Class<?>> fields = new HashMap<>();

		for(Field field : getClassFields(this.clazz))
			fields.put(field.getName(), field.getType());

		return fields;
	}

	public SimpleReflection setField(Class<?> fieldClass, Object value) throws ReflectException
	{
		return setField(fieldClass, 0, value);
	}

	public SimpleReflection setField(Class<?> fieldClass, int index, Object value) throws ReflectException
	{
		Field field = getField0(fieldClass, index);

		if(field != null)
		{
			try
			{
				field.set(instance, value);
			}
			catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

		return this;
	}

	public SimpleReflection setField(String fieldName, Object value) throws ReflectException
	{
		Field field = getField0(fieldName);

		if(field != null)
		{
			try
			{
				field.set(instance, value);
			}
			catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

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
		Field field = getField0(fieldClass, index);

		try
		{
			return field == null ? null : this.checkAndReturn(field.get(this.instance));
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}
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
		Field field = getField0(fieldName);

		try
		{
			return field == null ? null : this.checkAndReturn(field.get(this.instance));
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private Field getField0(Class<?> fieldClass, int index) throws ReflectException
	{
		int currentIndex = -1;

		for(Class<?> superclass : ReflectionUtil.getSuperClasses(clazz))
		{
			for(Field field : getClassFields(superclass))
			{
				if(ReflectionUtil.classEquals(fieldClass, field.getType()))
				{
					currentIndex++;

					if(currentIndex == index)
						return setFieldAccessible(field);
						//return new UnsafeField(field);
				}
			}
		}

		return null;
	}

	private Field getField0(String name) throws ReflectException
	{
		for(Class<?> superclass : ReflectionUtil.getSuperClasses(clazz))
		{
			for(Field field : getClassFields(superclass))
			{
				if(field.getName().equals(name))
					return setFieldAccessible(field);
			}
		}

		return null;
	}

	public SimpleReflection invokeMethod(Class<?> returnType, Object... parameters) throws ReflectException
	{
		return invokeMethod(returnType, 0, parameters);
	}

	public SimpleReflection invokeMethod(String name, Object... parameters) throws ReflectException
	{
		for(Class<?> superClass : ReflectionUtil.getSuperClasses(clazz))
		{
			for(Method method : superClass.getDeclaredMethods())
			{
				if(method.getName().equals(name) && ReflectionUtil.parametersEquals(method.getParameterTypes(), ReflectionUtil.getClasses(parameters)))
					return invokeMethod0(method, parameters);
			}
		}

		return null;
	}

	public SimpleReflection invokeMethod(Class<?> returnType, int index, Object[] parameters) throws ReflectException
	{
		int currentIndex = -1;

		for(Class<?> superClass : ReflectionUtil.getSuperClasses(clazz))
		{
			for(Method method : superClass.getDeclaredMethods())
			{
				if(ReflectionUtil.classEquals(returnType, method.getReturnType()) && ReflectionUtil.parametersEquals(method.getParameterTypes(), ReflectionUtil.getClasses(parameters)))
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

	public List<Constructor<?>> getConstructors()
	{
		return Arrays.asList(this.clazz.getDeclaredConstructors());
	}

	public SimpleReflection invokeConstructor(Object... parameters) throws ReflectException
	{
		for(Constructor<?> constructor : clazz.getDeclaredConstructors())
		{
			if(ReflectionUtil.parametersEquals(constructor.getParameterTypes(), ReflectionUtil.getClasses(parameters)))
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

	private List<Method> getClassMethods(Class<?> type)
	{
		List<Method> methods = new ArrayList<>();

		for(Method method : this.clazz.getDeclaredMethods())
		{
			if(ReflectionUtil.classEquals(this.clazz, method.getReturnType()))
				methods.add(method);
		}

		return methods;
	}

	private List<Field> getClassFields(Class<?> type)
	{
		List<Field> fields = new ArrayList<>();
		Collections.addAll(fields, type.getDeclaredFields());
		return fields;
	}

	private Field setFieldAccessible(Field field)
	{
		if(!Modifier.isStatic(field.getModifiers()) && instance == null)
			throw new ReflectException(ReflectException.ReflectExceptionCause.NOT_STATIC, field, null);

		try
		{
			field.setAccessible(true);

			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);

			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		}
		catch(IllegalAccessException | NoSuchFieldException e)
		{
			e.printStackTrace();
		}

		return field;
	}
}
