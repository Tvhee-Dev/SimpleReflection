package me.tvhee.simplereflection;

import java.util.ArrayList;
import java.util.List;

public class ReflectionProvider
{
	private ReflectionProvider(){}

	public static SimpleReflection reflect(Class<?> clazz)
	{
		if(clazz == null)
			return null;

		return new SimpleReflection(clazz);
	}

	public static SimpleReflection reflect(Object instance)
	{
		if(instance == null)
			return null;

		return new SimpleReflection(instance);
	}

	public static boolean hasMinecraftClass(String before1_17, String after1_17)
	{
		try
		{
			return getMinecraftClass(before1_17, after1_17) != null;
		}
		catch(Exception e)
		{
			return false;
		}
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
			return Class.forName(name);
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static Class<?> getCraftBukkitClass(String name)
	{
		return getClass(MinecraftVersion.getVersion().replace(name));
	}

	public static Class<?> getMinecraftClass(String before1_17, String after1_17)
	{
		if(MinecraftVersion.getVersion().atLeast(MinecraftVersion.v1_17_R1))
			return getClass(MinecraftVersion.getVersion().replace(after1_17));
		else
			return getClass(MinecraftVersion.getVersion().replace(before1_17));
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
		return clazz1.isAssignableFrom(clazz2) || clazz1.equals(clazz2);
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
