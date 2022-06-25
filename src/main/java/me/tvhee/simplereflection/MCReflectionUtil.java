package me.tvhee.simplereflection;

public final class MCReflectionUtil
{
	private static String minecraftVersion;

	private MCReflectionUtil() {}

	public static boolean hasClass(String before1_17, String after1_17)
	{
		try
		{
			getClass0(before1_17, after1_17);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public static Class<?> getClass(String before1_17, String after1_17)
	{
		try
		{
			return getClass0(before1_17, after1_17);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static Class<?> getClass0(String before1_17, String after1_17)
	{
		return ReflectionUtil.getClass(versionAtLeast("v1_17_R1") ? replaceVersion(after1_17) : replaceVersion(before1_17));
	}

	public static String replaceVersion(String input)
	{
		if(versionAtLeast("v1_17_R1"))
			input = input.replaceAll("\\{nms}", "net.minecraft");
		else
			input = input.replaceAll("\\{nms}", "net.minecraft.server." + getVersion());

		return input.replaceAll("\\{obc}", "org.bukkit.craftbukkit." + getVersion());
	}

	public static boolean versionAtLeast(String nmsVersion)
	{
		String currentVersion = getVersion();

		String[] currentVersionSplit = currentVersion.split("_");
		int currentVersionNumber = Integer.parseInt(currentVersionSplit[1]);
		int currentRelease = Integer.parseInt(currentVersionSplit[2].substring(1));

		String[] versionSplit = nmsVersion.split("_");
		int versionNumber = Integer.parseInt(versionSplit[1]);
		int release = Integer.parseInt(versionSplit[2].substring(1));

		return currentVersionNumber >= versionNumber && currentRelease >= release;
	}

	public static String getVersion()
	{
		if(minecraftVersion != null)
			return minecraftVersion;

		try
		{
			String packageName = new Reflection(ReflectionUtil.getClass("org.bukkit.Bukkit")).method("getServer").clazz().getPackage().getName();
			String version = packageName.substring(packageName.lastIndexOf('.') + 1);
			minecraftVersion = version;
			return version;
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Not running under NMS!", e);
		}
	}

	public static boolean hasBukkitClass(String name)
	{
		try
		{
			getBukkitClass(name);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public static Class<?> getBukkitClass(String name)
	{
		return ReflectionUtil.getClass(replaceVersion(name));
	}
}