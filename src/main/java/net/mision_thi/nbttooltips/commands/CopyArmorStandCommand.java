package net.mision_thi.nbttooltips.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

import static net.mision_thi.nbttooltips.tooltips.TooltipChanger.NBT_OPS_UNLIMITED;

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
            JsonObject equipment = new JsonObject();
            equipment.add("mainHand", makeItem(armorStand.getMainHandStack()));
            equipment.add("offHand", makeItem(armorStand.getOffHandStack()));
            equipment.add("helmet", makeItem(armorStand.getEquippedStack(EquipmentSlot.HEAD)));
            equipment.add("chestplate", makeItem(armorStand.getEquippedStack(EquipmentSlot.CHEST)));
            equipment.add("leggings", makeItem(armorStand.getEquippedStack(EquipmentSlot.LEGS)));
            equipment.add("boots", makeItem(armorStand.getEquippedStack(EquipmentSlot.FEET)));
            obj.add("equipment", equipment);
            var pos = new JsonObject();
            var playerPos = client.player.getPos();
            var rel = armorStand.getPos().subtract(playerPos);
            makePos(pos, rel, armorStand.getYaw(), armorStand.getPitch());
            obj.add("relativePos", pos);
            array.add(obj);
        }
        JsonObject obj = new JsonObject();
        var pPos = new JsonObject();
        makePos(pPos, client.player.getPos(), client.player.getYaw(), client.player.getPitch());
        obj.add("playerPos", pPos);
        obj.add("stands", array);
        client.player.sendMessage(Text.of("§aCopied " +  array.size() + " entities"), false);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        MinecraftClient.getInstance().keyboard.setClipboard(gson.toJson(obj));
        return 0;
    }

    private static JsonObject makeItem(ItemStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("id", stack.getItem().getRegistryEntry().getIdAsString());
        object.addProperty("glint", stack.hasGlint());
        var nbt = (NbtCompound) ComponentChanges.CODEC.encodeStart(NBT_OPS_UNLIMITED, stack.getComponentChanges()).getOrThrow();
        if (stack.getItem().getRegistryEntry().getIdAsString().equalsIgnoreCase("minecraft:player_head")) {
            System.out.println(nbt);
            System.out.println(nbt.getKeys());
            System.out.println(nbt.get("minecraft:profile"));
            System.out.println(nbt.get("profile"));
            for (var n : Objects.requireNonNull(((NbtCompound) Objects.requireNonNull(nbt.get("minecraft:profile"))).getList("properties").orElseGet(NbtList::new))) {
                var comp = (NbtCompound) n;
                System.out.println(n);
                    JsonObject obj = new JsonObject();
                    obj.addProperty("value", comp.getString("value").orElse(null));
                    obj.addProperty("signature", comp.getString("signature").orElse(null));
                    object.add("textures", obj);
            }
        }
        var optionalColor = nbt.getInt("dyed_color");
        optionalColor.ifPresent(integer -> object.addProperty("color", integer));
        return object;
    }

    private static void makePos(JsonObject object, Vec3d vec3d, float yaw, float pitch) {
        object.addProperty("x", vec3d.x);
        object.addProperty("y", vec3d.y);
        object.addProperty("z", vec3d.z);
        object.addProperty("yaw", yaw);
        object.addProperty("pitch", pitch);
    }

    private static JsonObject makeArm(EulerAngle angle) {
        var obj = new JsonObject();
        obj.addProperty("yaw", angle.yaw());
        obj.addProperty("pitch", angle.pitch());
        obj.addProperty("roll", angle.roll());
        return obj;
    }
}
