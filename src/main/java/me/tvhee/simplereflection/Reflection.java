package me.tvhee.simplereflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class Reflection
{
	private Class<?> clazz;
	private Object object;

	public Reflection(Class<?> clazz)
	{
		if(clazz == null)
			throw new IllegalArgumentException("Cannot reflect on " + null + " class!");

		this.clazz = clazz;
	}

	public Reflection(Object object)
	{
		if(object == null)
			throw new IllegalArgumentException("Cannot reflect on " + null + " object!");

		if(object instanceof Reflection)
		{
			Reflection reflection = (Reflection) object;
			this.clazz = reflection.clazz;
			this.object = reflection.object;
		}
		else
		{
			this.clazz = object.getClass();
			this.object = object;
		}
	}

	public <T> T object()
	{
		return tryCatch(() -> (T) object);
	}

	public void object(Object object)
	{
		this.object = object;
		this.clazz = object.getClass();
	}

	public Class<?> clazz()
	{
		return clazz;
	}

	public List<Class<?>> genericClasses()
	{
		List<Class<?>> classes = new ArrayList<>();

		if(clazz.getGenericSuperclass() instanceof ParameterizedType)
		{
			for(Type type : ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments())
				classes.add((Class<?>) type);
		}

		return classes;
	}

	public boolean canInstantiate(Object... constructorArguments)
	{
		return tryCatch(() -> instance(constructorArguments)) != null;
	}

	public Reflection instance(Object... constructorArguments)
	{
		for(Constructor<?> constructor : clazz.getDeclaredConstructors())
		{
			if(ReflectionUtil.parametersEquals(ReflectionUtil.getClasses(constructorArguments), constructor.getParameterTypes()))
			{
				try
				{
					return new Reflection(constructor.newInstance(constructorArguments));
				}
				catch(InstantiationException | IllegalAccessException | InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
		}

		throw new IllegalArgumentException("No constructor found! (parameters: " + Arrays.toString(ReflectionUtil.getClasses(constructorArguments)) + ")");
	}

	public boolean hasField(String name)
	{
		return tryCatch(() -> field(name)) != null;
	}

	public boolean hasField(Class<?> returnType)
	{
		return hasField(0, returnType);
	}

	public boolean hasField(int index, Class<?> returnType)
	{
		return tryCatch(() -> field(index, returnType)) != null;
	}

	public Reflection field(String name)
	{
		return getFieldValue0(field0(name));
	}

	public Reflection field(Class<?> returnType)
	{
		return field(0, returnType);
	}

	public List<Reflection> fields()
	{
		List<Reflection> reflectedFields = new ArrayList<>();

		for(Field field : getFields0(clazz))
		{
			UnsafeField unsafeField = new UnsafeField(field);
			Reflection fieldValue;

			if((fieldValue = getFieldValue0(unsafeField)) == null)
				continue;

			reflectedFields.add(fieldValue);
		}

		return reflectedFields;
	}

	public List<Reflection> fields(Class<?> returnType)
	{
		List<Reflection> reflectedFields = new ArrayList<>();

		for(Field field : getFields0(clazz))
		{
			if(!ReflectionUtil.classEquals(returnType, field.getType()))
				continue;

			UnsafeField unsafeField = new UnsafeField(field);
			Reflection fieldValue;

			if((fieldValue = getFieldValue0(unsafeField)) == null)
				continue;

			reflectedFields.add(fieldValue);
		}

		return reflectedFields;
	}

	public Reflection field(int index, Class<?> returnType)
	{
		UnsafeField field = field0(index, returnType);
		Reflection fieldValue;

		if((fieldValue = getFieldValue0(field)) == null)
			return null;

		return fieldValue;
	}

	public List<Reflection> fields(int index, FieldSearch search, Class<?> returnType)
	{
		List<Reflection> reflectedFields = new ArrayList<>();
		List<Field> fields = getFields0(clazz);
		fields.removeIf(field -> !ReflectionUtil.classEquals(returnType, field.getType()));
		int currentIndex = search == FieldSearch.BEFORE_INDEX ? fields.size() : 0;

		for(Field field : fields)
		{
			UnsafeField unsafeField = new UnsafeField(field);
			Reflection fieldValue = getFieldValue0(unsafeField);

			if(search == FieldSearch.AFTER_INDEX && currentIndex < index)
				currentIndex++;
			else if(search == FieldSearch.BEFORE_INDEX && currentIndex > index)
				currentIndex--;
			else if(fieldValue != null)
				reflectedFields.add(fieldValue);
		}

		return reflectedFields;
	}

	public Reflection field(String name, Object value)
	{
		return field(name, (fieldValue) -> value);
	}

	public Reflection field(String name, Function<Reflection, Object> value)
	{
		return setFieldValue0(field0(name), value);
	}

	public Reflection field(Class<?> returnType, Object value)
	{
		return field(returnType, (fieldValue) -> value);
	}

	public Reflection field(Class<?> returnType, Function<Reflection, Object> value)
	{
		return field(0, returnType, value);
	}

	public Reflection fields(Class<?> returnType, Object value)
	{
		return field(returnType, (fieldValue) -> value);
	}

	public Reflection fields(Class<?> returnType, Function<Reflection, Object> value)
	{
		List<Field> fieldsToSet = getFields0(clazz);
		fieldsToSet.removeIf(field -> !ReflectionUtil.classEquals(returnType, field.getType()));
		return setFieldValues0(fieldsToSet, value);
	}

	public Reflection field(int index, Class<?> returnType, Object value)
	{
		return field(index, returnType, (fieldValue) -> value);
	}

	public Reflection field(int index, Class<?> returnType, Function<Reflection, Object> value)
	{
		return setFieldValue0(field0(index, returnType), value);
	}

	public Reflection fields(int index, FieldSearch search, Class<?> returnType, Object value)
	{
		return fields(index, search, returnType, (fieldValue) -> value);
	}

	public Reflection fields(int index, FieldSearch search, Class<?> returnType, Function<Reflection, Object> value)
	{
		List<Field> classFields = getFields0(clazz);
		List<Field> fields = new ArrayList<>();
		int currentIndex = search == FieldSearch.BEFORE_INDEX ? classFields.size() : 0;

		for(Field field : classFields)
		{
			if(!ReflectionUtil.classEquals(returnType, field.getType()))
				continue;

			if(search == FieldSearch.AFTER_INDEX && currentIndex < index)
				currentIndex++;
			else if(search == FieldSearch.BEFORE_INDEX && currentIndex > index)
				currentIndex--;
			else
				fields.add(field);
		}

		return setFieldValues0(fields, value);
	}

	private Reflection setFieldValue0(UnsafeField field, Function<Reflection, Object> value)
	{
		if(!field.isSupported())
		{
			List<Object> fieldValues = new ArrayList<>();

			for(Field classField : getFields0(clazz))
			{
				if(Modifier.isStatic(classField.getModifiers()))
					continue;

				addFieldValue0(field, value, fieldValues, classField);
			}

			return instance(fieldValues.toArray());
		}

		Reflection fieldValue = getFieldValue0(field);
		field.setFieldValue(object, value.apply(fieldValue));
		return this;
	}

	private Reflection setFieldValues0(List<Field> javaFields, Function<Reflection, Object> value)
	{
		List<UnsafeField> fields = new ArrayList<>();

		for(Field field : javaFields)
			fields.add(new UnsafeField(field));

		if(fields.isEmpty())
			return this;

		if(!fields.get(0).isSupported())
		{
			List<Object> fieldValues = new ArrayList<>();

			for(Field classField : getFields0(clazz))
			{
				if(Modifier.isStatic(classField.getModifiers()))
					continue;

				for(UnsafeField unsafeField : fields)
					addFieldValue0(unsafeField, value, fieldValues, classField);
			}

			return instance(fieldValues.toArray());
		}

		for(UnsafeField field : fields)
		{
			Reflection fieldValue = getFieldValue0(field);
			field.setFieldValue(object, value.apply(fieldValue));
		}

		return this;
	}

	private void addFieldValue0(UnsafeField field, Function<Reflection, Object> value, List<Object> fieldValues, Field classField)
	{
		try
		{
			classField.setAccessible(true);

			if(classField.getName().equals(field.getJavaField().getName()))
				fieldValues.add(value.apply(classField.get(object) == null ? null : new Reflection(classField.get(object))));
			else
				fieldValues.add(classField.get(object));
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	private Reflection getFieldValue0(UnsafeField field)
	{
		Object fieldValue = null;

		if(!field.isSupported())
		{
			Field javaField = field.getJavaField();

			try
			{
				fieldValue = javaField.get(Modifier.isStatic(javaField.getModifiers()) ? null : object);
			}
			catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		else
			fieldValue = field.getFieldValue(object);

		if(fieldValue == null)
			return null;

		return new Reflection(fieldValue);
	}

	private UnsafeField field0(String name)
	{
		for(Class<?> superclass : ReflectionUtil.getSuperClasses(clazz))
		{
			for(Field field : getFields0(superclass))
			{
				if(field.getName().equals(name))
				{
					checkStatic(field);
					return new UnsafeField(field);
				}
			}
		}

		throw new IllegalArgumentException("Field (name: " + name + ") not found!");
	}

	private UnsafeField field0(int index, Class<?> returnType)
	{
		int currentIndex = -1;

		for(Class<?> superclass : ReflectionUtil.getSuperClasses(clazz))
		{
			for(Field field : getFields0(superclass))
			{
				if(ReflectionUtil.classEquals(returnType, field.getType()))
				{
					currentIndex++;

					if(currentIndex == index)
						return new UnsafeField(field);
				}
			}
		}

		throw new IllegalArgumentException("Field (type: " + returnType + ", index: " + index + ") not found!");
	}

	private List<Field> getFields0(Class<?> clazz)
	{
		List<Field> fields = new ArrayList<>();

		try
		{
			if((boolean) Class.class.getMethod("isRecord").invoke(clazz))
			{
				Object[] recordComponents = (Object[]) Class.class.getMethod("getRecordComponents").invoke(clazz);

				for(Object component : recordComponents)
					fields.add(clazz.getDeclaredField((String) component.getClass().getMethod("getName").invoke(component)));

				return fields;
			}
		}
		catch(NoSuchFieldException | InvocationTargetException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch(NoSuchMethodException ignored)
		{
		}

		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		return fields;
	}

	public boolean hasMethod(String name, Class<?>... parameters)
	{
		return tryCatch(() -> method0(name, parameters)) != null;
	}

	public boolean hasMethod(Class<?>... parameters)
	{
		return hasMethod(0, parameters);
	}

	public boolean hasMethod(int index, Class<?>... parameters)
	{
		return tryCatch(() -> method0(index, parameters)) != null;
	}

	public boolean hasMethod(Class<?> returnType)
	{
		return hasMethod(0, returnType);
	}

	public boolean hasMethod(int index, Class<?> returnType)
	{
		return tryCatch(() -> method0(index, returnType)) != null;
	}

	public Reflection method(String name, Object... arguments)
	{
		Method method = method0(name, ReflectionUtil.getClasses(arguments));
		return invokeMethod0(method, arguments);
	}

	public Reflection method(Class<?> returnType, Object... arguments)
	{
		return method(0, returnType, arguments);
	}

	public Reflection method(int index, Class<?> returnType, Object... arguments)
	{
		Method method = method0(index, returnType);
		return invokeMethod0(method, arguments);
	}

	public Reflection method(Object... arguments)
	{
		return method(0, arguments);
	}

	public Reflection method(int index, Object... arguments)
	{
		Method method = method0(index, ReflectionUtil.getClasses(arguments));
		return invokeMethod0(method, arguments);
	}

	private Method method0(String name, Class<?>[] parameters)
	{
		for(Class<?> superClass : ReflectionUtil.getSuperClasses(clazz))
		{
			for(Method method : superClass.getDeclaredMethods())
			{
				if(method.getName().equals(name) && ReflectionUtil.parametersEquals(parameters, method.getParameterTypes()))
				{
					method.setAccessible(true);
					return method;
				}
			}
		}

		throw new IllegalArgumentException("Method (name: " + name + ", parameters: " + Arrays.toString(parameters) + ") not found!");
	}

	private Method method0(int index, Class<?>[] parameters)
	{
		int currentIndex = -1;

		for(Class<?> superClass : ReflectionUtil.getSuperClasses(clazz))
		{
			for(Method method : superClass.getDeclaredMethods())
			{
				if(ReflectionUtil.parametersEquals(parameters, method.getParameterTypes()))
				{
					currentIndex++;

					if(currentIndex == index)
					{
						method.setAccessible(true);
						return method;
					}
				}
			}
		}

		throw new IllegalArgumentException("Method (index: " + index + ", parameters: " + Arrays.toString(parameters) + ") not found!");
	}

	private Method method0(int index, Class<?> returnType)
	{
		int currentIndex = -1;

		for(Class<?> superClass : ReflectionUtil.getSuperClasses(clazz))
		{
			for(Method method : superClass.getDeclaredMethods())
			{
				if(ReflectionUtil.classEquals(returnType, method.getReturnType()))
				{
					currentIndex++;

					if(currentIndex == index)
					{
						method.setAccessible(true);
						return method;
					}
				}
			}
		}

		throw new IllegalArgumentException("Method (index: " + index + ", type: " + returnType + ") not found!");
	}

	private Reflection invokeMethod0(Method method, Object[] arguments)
	{
		try
		{
			Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? null : object, arguments);

			if(value != null)
				return new Reflection(value);
		}
		catch(IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private void checkStatic(AccessibleObject accessibleObject)
	{
		try
		{
			int modifiers = (int) accessibleObject.getClass().getMethod("getModifiers").invoke(accessibleObject);

			if(!Modifier.isStatic(modifiers) && object == null)
				throw new IllegalArgumentException(accessibleObject + " is not static and requires an instance. Please call instance() first!");
		}
		catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			e.printStackTrace();
		}
	}

	private <T> T tryCatch(Supplier<T> supplier)
	{
		try
		{
			return supplier.get();
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public enum FieldSearch
	{
		BEFORE_INDEX,
		AFTER_INDEX
	}

	private interface Supplier<T>
	{
		T get() throws Exception;
	}
}
