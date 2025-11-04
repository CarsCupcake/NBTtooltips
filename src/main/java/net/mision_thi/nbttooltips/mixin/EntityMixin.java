package net.mision_thi.nbttooltips.mixin;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.entity.Entity.class)
public interface EntityMixin {
    @Accessor("customData")
    NbtComponent getCustomData();
}
