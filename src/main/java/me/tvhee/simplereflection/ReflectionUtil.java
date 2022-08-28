package me.tvhee.simplereflection;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ReflectionUtil
{
	private static final Map<String, Class<?>> classCache = new HashMap<>();

	private ReflectionUtil() {}

	public static boolean hasClass(String name)
	{
		try
		{
			getClass0(name);
			return true;
		}
		catch(ClassNotFoundException e)
		{
			return false;
		}
	}

	public static Class<?> getClass(String name)
	{
		try
		{
			return getClass0(name);
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static Class<?> getClass0(String name) throws ClassNotFoundException
	{
		if(classCache.containsKey(name))
			return classCache.get(name);

		Class<?> clazz = Class.forName(name);
		classCache.put(name, clazz);
		return clazz;
	}

	public static boolean parametersEquals(Class<?>[] parameters, Class<?>[] requestedParameters)
	{
		if(parameters.length != requestedParameters.length)
			return false;

		for(int i = 0; i < requestedParameters.length; i++)
		{
			if(!classEquals(parameters[i], requestedParameters[i]) && parameters[i] != null && Primitive.of(parameters[i]) == null)
				return false;
		}

		return true;
	}

	public static boolean classEquals(Class<?> clazz, Class<?> requestedClazz)
	{
		return clazz != null && requestedClazz != null && (requestedClazz.isAssignableFrom(clazz) || requestedClazz.equals(clazz) || Primitive.compare(clazz, requestedClazz));
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
		{
			Object object = objects[i] instanceof Reflection ? ((Reflection) objects[i]).object() : objects[i];
			classes[i] = object == null ? null : object.getClass();
		}

		return classes;
	}
	public static List<Class<?>> getClasses(File jarFile)
	{
		return new ArrayList<>(getClasses(jarFile, null));
	}
	public static <T> Set<Class<T>> getClasses(File jarFile, Class<T> extendingClass)
	{
		if(jarFile == null || !jarFile.getName().endsWith(".jar") || jarFile.isDirectory())
			throw new IllegalArgumentException("The file must be a jarfile!");

		try
		{
			TreeSet<Class<T>> classes = new TreeSet<>(Comparator.comparing(Class::toString));

			try(JarFile fileJar = new JarFile(jarFile))
			{
				Enumeration<JarEntry> entries = fileJar.entries();

				while(entries.hasMoreElements())
				{
					String name = entries.nextElement().getName();

					if(name.endsWith(".class"))
					{
						try
						{
							name = name.replaceFirst("\\.class", "").replaceAll("/", ".");
							Class<?> clazz = URLClassLoader.newInstance(new URL[]{ new URL(jarFile.getPath()) }).loadClass(name);

							if(extendingClass == null || (extendingClass.isAssignableFrom(clazz) && clazz != extendingClass))
								classes.add((Class<T>) clazz);
						}
						catch(NoClassDefFoundError ignored)
						{
						}
					}
				}
			}

			return classes;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new HashSet<>();
		}
	}
}
