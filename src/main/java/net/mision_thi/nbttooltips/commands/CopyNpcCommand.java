package net.mision_thi.nbttooltips.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.mision_thi.nbttooltips.NBTtooltipsMod;
import net.mision_thi.nbttooltips.config.ConfigSection;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

public class CopyNpcCommand {
    public static int run(CommandContext<FabricClientCommandSource> commandContext) throws CommandSyntaxException {
        MinecraftClient client = MinecraftClient.getInstance();
        var entity = client.targetedEntity;
        if (entity == null) {
            client.player.sendMessage(Text.of("§cNo player targeted!"), false);
            return 0;
        }
        if (entity.getType() != EntityType.PLAYER) {
            client.player.sendMessage(Text.of("§cThis command can only be executed for a player entity"), false);
            return 0;
        }
        var player = (PlayerEntity)  entity;
        var jsonElement = new PropertyMap.Serializer().serialize(player.getGameProfile().getProperties(), null, null);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(gson.toJson(jsonElement)), null);
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.literal("§aCopied to clipboard!"), false);
        return 0;
    }
}
