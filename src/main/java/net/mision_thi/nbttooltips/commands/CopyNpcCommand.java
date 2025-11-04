package net.mision_thi.nbttooltips.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class CopyNpcCommand {
    public static int run(CommandContext<FabricClientCommandSource> commandContext) throws CommandSyntaxException {
        assert MinecraftClient.getInstance().player != null;
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
        var player = (PlayerEntity) entity;
        var jsonElement = new PropertyMap.Serializer().serialize(player.getGameProfile().getProperties(), null, null).getAsJsonObject();
        jsonElement.addProperty("name", player.getGameProfile().getName());
        if (player.getDisplayName() != null)
            jsonElement.addProperty("displayName", player.getDisplayName().getLiteralString());
        var posObj = new JsonObject();
        posObj.addProperty("x", player.getX());
        posObj.addProperty("y", player.getY());
        posObj.addProperty("z", player.getZ());
        posObj.addProperty("yaw", player.getYaw());
        posObj.addProperty("pitch", player.getPitch());
        jsonElement.add("position", posObj);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        MinecraftClient.getInstance().keyboard.setClipboard(gson.toJson(jsonElement));
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.literal("§aCopied to clipboard!"), false);
        return 0;
    }
}
