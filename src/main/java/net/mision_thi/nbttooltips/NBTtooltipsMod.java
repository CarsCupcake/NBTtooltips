package net.mision_thi.nbttooltips;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.CommandManager;
import net.mision_thi.nbttooltips.commands.CopyArmorStandCommand;
import net.mision_thi.nbttooltips.commands.CopyNpcCommand;
import net.mision_thi.nbttooltips.commands.IgnoreNbtCommand;
import net.mision_thi.nbttooltips.config.ConfigFile;
import net.mision_thi.nbttooltips.config.ConfigSection;
import net.mision_thi.nbttooltips.config.ModConfigs;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NBTtooltipsMod implements ClientModInitializer {
	public static final MinecraftClient client = MinecraftClient.getInstance();
	public static final String MOD_ID = "NBTtooltips";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final KeyBinding KEYBIND_SHOW = KeyBindingHelper
			.registerKeyBinding(new KeyBinding("nbttooltips.keybind.show", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, KeyBinding.INVENTORY_CATEGORY));
	public static final KeyBinding KEYBIND_COPY = KeyBindingHelper
			.registerKeyBinding(new KeyBinding("nbttooltips.keybind.copy", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, KeyBinding.INVENTORY_CATEGORY));
	public static ConfigFile config;
	@Override
	public void onInitializeClient() {
		System.setProperty("java.awt.headless", "false");
		ModConfigs.registerConfigs();
		config = new ConfigFile("nbttooltips_ignored");
		IgnoreNbtCommand.ignoreNbt = new ArrayList<>(Arrays.asList(config.get("ignored", ConfigSection.STRING_ARRAY, new String[0])));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(ClientCommandManager.literal("command_with_common_exec")
					.then(ClientCommandManager.argument("nbt", StringArgumentType.string()))
					.executes(IgnoreNbtCommand::run));
			dispatcher.register(ClientCommandManager.literal("copynpc").executes(CopyNpcCommand::run));
			dispatcher.register(ClientCommandManager.literal("copyarmorstands")
							.then(ClientCommandManager.argument("radius", IntegerArgumentType.integer()))
					.executes(CopyArmorStandCommand::run));
		});
    }


}