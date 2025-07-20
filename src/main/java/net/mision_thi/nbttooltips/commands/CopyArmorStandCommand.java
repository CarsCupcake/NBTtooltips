package net.mision_thi.nbttooltips.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.math.EulerAngle;

import java.util.Objects;

public class CopyArmorStandCommand {
    public static int run(CommandContext<FabricClientCommandSource> commandContext) throws CommandSyntaxException {
        MinecraftClient client = MinecraftClient.getInstance();
        var radius = commandContext.getArgument("radius", Integer.class);
        var array = new JsonArray();
        assert client.world != null;
        for (var entity : client.world.getEntities()) {
            if (entity.getType() != EntityType.ARMOR_STAND) continue;
            if (entity.distanceTo(client.player) > radius) continue;
            var armorStand = (ArmorStandEntity) entity;
            var obj = new JsonObject();
            if (entity.hasCustomName())
                obj.addProperty("name", Objects.requireNonNull(entity.getCustomName()).getString());
            obj.addProperty("small", armorStand.isSmall());
            obj.add("leftArm", makeArm(armorStand.getLeftArmRotation()));
            obj.add("rightArm", makeArm(armorStand.getRightArmRotation()));
            obj.add("leftLeg", makeArm(armorStand.getLeftLegRotation()));
            obj.add("rightLeg", makeArm(armorStand.getRightLegRotation()));
            obj.add("head", makeArm(armorStand.getHeadRotation()));
            obj.add("body", makeArm(armorStand.getBodyRotation()));
            obj.addProperty("invisible", armorStand.isInvisible());
            var mainHandItemObj = new JsonObject();
            mainHandItemObj.addProperty("id", armorStand.getMainHandStack().getItem().getRegistryEntry().getIdAsString());
            mainHandItemObj.addProperty("glint", armorStand.getMainHandStack().hasGlint());
            obj.add("mainHandItem", mainHandItemObj);
            var subHandItemObj = new JsonObject();
            subHandItemObj.addProperty("id", armorStand.getOffHandStack().getItem().getRegistryEntry().getIdAsString());
            subHandItemObj.addProperty("glint", armorStand.getOffHandStack().hasGlint());
            obj.add("offHandItem", subHandItemObj);
            var pos = new JsonObject();
            var playerPos = client.player.getPos();
            pos.addProperty("x", playerPos.x - armorStand.getX());
            pos.addProperty("y", playerPos.y -armorStand.getY());
            pos.addProperty("z", playerPos.z - armorStand.getZ());
            obj.add("relativePos", pos);
            var pPos = new JsonObject();
            pPos.addProperty("x", playerPos.x);
            pPos.addProperty("y", playerPos.y);
            pPos.addProperty("z", playerPos.z);
            obj.add("playerPos", pPos);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        MinecraftClient.getInstance().keyboard.setClipboard(gson.toJson(array));
        return 0;
    }

    private static JsonObject makeArm(EulerAngle angle) {
        var obj = new JsonObject();
        obj.addProperty("yaw", angle.yaw());
        obj.addProperty("pitch", angle.pitch());
        obj.addProperty("roll", angle.roll());
        return obj;
    }
}
