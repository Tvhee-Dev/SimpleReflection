package me.tvhee.simplereflection;

import java.util.ArrayList;
import java.util.List;

public class ReflectionProvider
{
	private ReflectionProvider() {}

	public static SimpleReflection reflect(Class<?> clazz)
	{
		return new SimpleReflection(clazz);
	}

	public static SimpleReflection reflect(Object instance)
	{
		return new SimpleReflection(instance);
	}

	public static boolean hasClass(String name)
	{
		try
		{
			return getClass(name) != null;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public static Class<?> getClass(String name)
	{
		try
		{
			return Class.forName(MinecraftVersion.getVersion().replace(name));
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static Class<? extends Enum<?>> getEnumClass(String name)
	{
		return (Class<? extends Enum<?>>) getClass(name);
	}

	public static boolean parametersEquals(Class<?>[] parameters, Class<?>[] requestedParameters)
	{
		if(parameters.length != requestedParameters.length)
			return false;

		for(int i = 0; i < requestedParameters.length; i++)
		{
			if(!classEquals(parameters[i], requestedParameters[i]))
				return false;
		}

		return true;
	}

	public static boolean classEquals(Class<?> clazz1, Class<?> clazz2)
	{
		boolean found = clazz1.equals(clazz2) || PrimitiveClass.compareAll(clazz1, clazz2);

		if(!found)
		{
			for(Class<?> methodReturnTypeClass : getSuperClasses(clazz1))
			{
				for(Class<?> returnTypeClass : getSuperClasses(clazz2))
				{
					if(PrimitiveClass.compareAll(methodReturnTypeClass, returnTypeClass))
					{
						found = true;
						break;
					}
				}
			}
		}

		return found;
	}

	public static List<Class<?>> getSuperClasses(Class<?> clazz)
	{
		List<Class<?>> superClasses = new ArrayList<>();
		Class<?> superClass = clazz;

		while(superClass != null)
		{
			if(superClass.equals(Object.class))
				break;

			superClasses.add(superClass);

			for(Class<?> interfaceClass : superClass.getInterfaces())
			{
				superClasses.add(interfaceClass);
				superClasses.addAll(getSuperClasses(interfaceClass));
			}

			superClass = superClass.getSuperclass();
		}

		return superClasses;
	}

	public static Class<?>[] getClasses(Object[] objects)
	{
		Class<?>[] classes = new Class[objects.length];

		for(int i = 0; i < objects.length; i++)
			classes[i] = objects[i].getClass();

		return classes;
	}
}
