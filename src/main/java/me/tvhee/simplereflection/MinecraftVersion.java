package me.tvhee.simplereflection;

@Deprecated
public enum MinecraftVersion
{
	v1_8_R1,
	v1_8_R2,
	v1_8_R3,
	v1_9_R1,
	v1_9_R2,
	v1_10_R1,
	v1_11_R1,
	v1_12_R1,
	v1_13_R1,
	v1_13_R2,
	v1_14_R1,
	v1_15_R1,
	v1_16_R1,
	v1_16_R2,
	v1_16_R3,
	v1_17_R1,
	v1_18_R1,
	v1_18_R2,
	v1_19_R1;

	public String replace(String input)
	{
		if(atLeast(v1_17_R1))
			input = input.replaceAll("\\{nms}", "net.minecraft");
		else
			input = input.replaceAll("\\{nms}", "net.minecraft.server." + name());

		return input.replaceAll("\\{obc}", "org.bukkit.craftbukkit." + name());
	}

	public boolean atLeast(MinecraftVersion minecraftVersion)
	{
		return ordinal() >= minecraftVersion.ordinal();
	}

	public static MinecraftVersion getVersion()
	{
		return MinecraftVersion.valueOf(MCReflectionUtil.getVersion());
	}
}
