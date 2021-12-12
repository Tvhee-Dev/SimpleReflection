package me.tvhee.simplereflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectException extends RuntimeException
{
	private final ReflectExceptionCause cause;

	public ReflectException(ReflectExceptionCause cause, Constructor<?> constructor, Exception baseException)
	{
		super(cause.getConstructorMessage(constructor), baseException);
		this.cause = cause;
	}

	public ReflectException(ReflectExceptionCause cause, Field field, Exception baseException)
	{
		super(cause.getFieldMessage(field), baseException);
		this.cause = cause;
	}

	public ReflectException(ReflectExceptionCause cause, Method method, Exception baseException)
	{
		super(cause.getMethodMessage(method), baseException);
		this.cause = cause;
	}

	public ReflectException(ReflectExceptionCause cause, String clazz, Exception baseException)
	{
		super("Class " + clazz + " not found!", baseException);
		this.cause = cause;
	}

	public ReflectExceptionCause getExceptionCause()
	{
		return cause;
	}

	public enum ReflectExceptionCause
	{
		WRONG_PARAMETERS("The parameters of constructor {parameters} (class {class}) are wrong!", "The parameters of method {method} (class {class}) are wrong!", "The parameter of field {field} (class {class}) is wrong!"),
		NOT_STATIC("", "Method {method} (class {class}) is not static! Please provide an instance", "Field {field} (class {class}) is not static! Please provide an instance");

		private final String constructorMessage;
		private final String methodMessage;
		private final String fieldMessage;

		ReflectExceptionCause(String constructorMessage, String methodMessage, String fieldMessage)
		{
			this.constructorMessage = constructorMessage;
			this.methodMessage = methodMessage;
			this.fieldMessage = fieldMessage;
		}

		private String getConstructorMessage(Constructor<?> constructor)
		{
			return getClassMessage(constructorMessage.replaceAll("\\{parameters}", Arrays.toString(constructor.getParameterTypes())), constructor.getDeclaringClass());
		}

		private String getFieldMessage(Field field)
		{
			return getClassMessage(fieldMessage.replaceAll("\\{parameters}", field.getType().toString()).replaceAll("\\{field}", field.getName()), field.getDeclaringClass());
		}

		private String getMethodMessage(Method method)
		{
			return getClassMessage(methodMessage.replaceAll("\\{parameters}", Arrays.toString(method.getParameterTypes())).replaceAll("\\{method}", method.getName()), method.getDeclaringClass());
		}

		private String getClassMessage(String message, Class<?> clazz)
		{
			return message.replaceAll("\\{class}", clazz.getName());
		}
	}

	@Override
	public String toString()
	{
		return "ReflectException{" + "cause=" + cause + '}';
	}
}
