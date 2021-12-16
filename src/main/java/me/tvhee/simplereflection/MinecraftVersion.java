package me.tvhee.simplereflection;

public enum MinecraftVersion
{
	v1_8_R1,
	v1_8_R2,
	v1_8_R3,
	v1_9_R1,
	v1_9_R2,
	v_1_10_R1,
	v_1_11_R1,
	v_1_12_R1,
	v_1_13_R1,
	v_1_13_R2,
	v_1_14_R1,
	v_1_15_R1,
	v1_16_R1,
	v1_16_R2,
	v1_16_R3,
	v1_17_R1,
	v1_18_R1;

	public String replace(String input)
	{
		if(atLeast(MinecraftVersion.v1_17_R1))
			input = input.replaceAll("\\{nms}", "net.minecraft");
		else
			input = input.replaceAll("\\{nms}", "net.minecraft.server." + name());

		return input.replaceAll("\\{obc}", "org.bukkit.craftbukkit." + name());
	}

	public boolean atLeast(MinecraftVersion minecraftVersion)
	{
		return minecraftVersion.ordinal() >= ordinal();
	}

	public static MinecraftVersion getVersion()
	{
		try
		{
			String packageName = ReflectionProvider.reflect(Class.forName("org.bukkit.Bukkit")).invokeMethod("getServer").getReflectedClass().getPackage().getName();
			return MinecraftVersion.valueOf(packageName.substring(packageName.lastIndexOf('.') + 1));
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
