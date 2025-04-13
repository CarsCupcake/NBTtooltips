package net.mision_thi.nbttooltips.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.CommandBlock;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.text.Text;
import net.mision_thi.nbttooltips.NBTtooltipsMod;
import net.mision_thi.nbttooltips.config.ConfigSection;
import net.mision_thi.nbttooltips.config.ModConfigs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class IgnoreNbtCommand {
    public static ArrayList<String> ignoreNbt = new ArrayList<>();
    public static int run(CommandContext<FabricClientCommandSource> commandContext) throws CommandSyntaxException {
        var arg = StringArgumentType.getString(commandContext, "nbt");
        if (ignoreNbt.contains(arg)) {
            ignoreNbt.remove(arg);
            commandContext.getSource().sendFeedback(Text.literal("§aEnabled " + arg));
        } else {
            ignoreNbt.add(arg);
            commandContext.getSource().sendFeedback(Text.literal("§cDisabled " + arg));
        }
        NBTtooltipsMod.config.set("ignored", ignoreNbt.toArray(String[]::new), ConfigSection.STRING_ARRAY);
        NBTtooltipsMod.config.save();
        return 0;
    }
}
