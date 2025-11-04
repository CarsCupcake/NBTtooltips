package net.mision_thi.nbttooltips.commands;

import com.google.gson.*;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.mision_thi.nbttooltips.mixin.EntityMixin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
        var jsonElement = new JsonObject();
        jsonElement.add("properties", new PropertyMap.Serializer().serialize(player.getGameProfile().getProperties(), null, null));
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
        if (player.hasCustomName()) {
            jsonElement.addProperty("customName", Objects.requireNonNull(player.getCustomName()).getString());
            jsonElement.addProperty("customNameVisible", player.isCustomNameVisible());
        }
        var asEntityMixin = (EntityMixin) player;
        var customData = asEntityMixin.getCustomData();
        if (customData != null)
            jsonElement.add("nbt", toJsonObject(customData.copyNbt()));

        client.player.sendMessage(Text.of("The npc has " + player.getPassengerList() + " passengers"), false);
        var scoreboardJson = new JsonObject();
        scoreboardJson.addProperty("nameForScoreboard", player.getNameForScoreboard());
        var scoreboard = player.getScoreboard();
        if (scoreboard != null) {
            var teams = new JsonObject();
            for (var teamEntry : scoreboard.getTeamNames()) {
                var team = scoreboard.getTeam(teamEntry);
                if (team == null) continue;
                var teamJson = getTeamJson(team);
                teams.add(teamEntry, teamJson);
            }
        }
        var metadata = new JsonObject();
        var dataTracker = player.getDataTracker();
        for (var changed : Objects.requireNonNull(dataTracker.getChangedEntries())) {
            metadata.addProperty(changed.id() + "", changed.value().toString());
        }
        jsonElement.add("metadata", metadata);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        MinecraftClient.getInstance().keyboard.setClipboard(gson.toJson(jsonElement));
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.literal("§aCopied to clipboard!"), false);
        return 0;
    }

    private static @NotNull JsonObject getTeamJson(Team team) {
        var teamJson = new JsonObject();
        teamJson.addProperty("name", team.getName());
        teamJson.addProperty("color", team.getColor().getName());
        teamJson.addProperty("prefix", team.getPrefix().getString());
        teamJson.addProperty("suffix", team.getSuffix().getString());
        var playerListArray = new JsonArray();
        for (var playerName : team.getPlayerList())
            playerListArray.add(playerName);
        teamJson.add("players", playerListArray);
        teamJson.addProperty("friendlyFire", team.isFriendlyFireAllowed());
        teamJson.addProperty("displayName", team.getDisplayName().getString());
        return teamJson;
    }

    private static JsonObject toJsonObject(NbtCompound nbtCompound) {
        var obj = new JsonObject();
        for (var entry : nbtCompound.entrySet()) {
            var e = entry.getValue();
            if (e instanceof NbtEnd) continue;
            obj.add(entry.getKey(), fromUnknown(e));
        }
        return obj;
    }

    private static JsonPrimitive toJsonPrimitive(NbtPrimitive nbtIntArray) {
        return switch (nbtIntArray) {
            case null -> null;
            case AbstractNbtNumber nbtNumber -> new JsonPrimitive(nbtNumber.numberValue());
            case NbtString nbtString -> new JsonPrimitive(nbtString.asString().orElse(""));
        };
    }

    private static JsonElement fromUnknown(NbtElement e) {
        if (e instanceof NbtCompound c) {
             return toJsonObject(c);
        } else if (e instanceof AbstractNbtList list1) {
            return toJsonArray(list1);
        } else if (e instanceof NbtPrimitive primitive) {
            return toJsonPrimitive(primitive);
        }
        return null;
    }

    private static JsonArray toJsonArray(AbstractNbtList list) {
        return switch (list) {
            case null -> null;
            case NbtList l -> {
                var array = new JsonArray();
                for (var e : l) {
                    if (e instanceof NbtEnd) continue;
                    array.add(fromUnknown(e));
                }
                yield array;
            }
            case NbtByteArray byteArray -> {
                var array = new JsonArray();
                byteArray.forEach(nbtElement -> array.add(new JsonPrimitive(nbtElement.asByte().orElse((byte) 0))));
                yield array;
            }
            case NbtIntArray intArray -> {
                var array = new JsonArray();
                intArray.forEach(nbtElement -> array.add(new JsonPrimitive(nbtElement.asInt().orElse(0))));
                yield array;
            }
            case NbtLongArray longArray -> {
                var array = new JsonArray();
                longArray.forEach(nbtElement -> array.add(new JsonPrimitive(nbtElement.asLong().orElse(0L))));
                yield array;
            }
        };
    }
}
