package xyz.brassgoggledcoders.mccivilizations.util;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class NBTHelper {
    public static CompoundTag writeGlobalPos(GlobalPos globalPos) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dimension", globalPos.dimension().location().toString());
        tag.put("BlockPos", NbtUtils.writeBlockPos(globalPos.pos()));
        return tag;
    }

    public static GlobalPos readGlobalPos(CompoundTag tag) {
        return GlobalPos.of(
                ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("Dimension"))),
                NbtUtils.readBlockPos(tag.getCompound("BlockPos"))
        );
    }
}
