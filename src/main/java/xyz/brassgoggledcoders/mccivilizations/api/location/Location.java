package xyz.brassgoggledcoders.mccivilizations.api.location;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.api.MCCivilizationsRegistries;
import xyz.brassgoggledcoders.mccivilizations.util.NBTHelper;

import java.util.UUID;

public class Location {
    @NotNull
    private final UUID id;
    @NotNull
    private final GlobalPos position;
    @NotNull
    private final LocationType locationType;
    private final BlockState blockState;
    private Component name;

    public Location(@NotNull UUID id, @NotNull GlobalPos position, @NotNull LocationType locationType, @NotNull BlockState blockState, Component name) {
        this.id = id;
        this.position = position;
        this.locationType = locationType;
        this.blockState = blockState;
        this.name = name;
    }

    @NotNull
    public UUID getId() {
        return this.id;
    }

    @NotNull
    public GlobalPos getPosition() {
        return position;
    }

    @NotNull
    public LocationType getLocationType() {
        return locationType;
    }

    @NotNull
    public BlockState getBlockState() {
        return blockState;
    }

    public Component getName() {
        return name;
    }

    public void setName(Component name) {
        this.name = name;
    }


    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", this.getId());
        tag.put("Position", NBTHelper.writeGlobalPos(this.getPosition()));
        tag.putString("LocationType", MCCivilizationsRegistries.getLocationTypeKey(this.getLocationType()));
        tag.put("BlockState", NbtUtils.writeBlockState(this.getBlockState()));
        tag.putString("Name", Component.Serializer.toJson(this.getName()));
        return tag;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.getId());
        friendlyByteBuf.writeGlobalPos(this.getPosition());
        friendlyByteBuf.writeRegistryId(MCCivilizationsRegistries.LOCATION_TYPE_REGISTRY.get(), this.getLocationType());
        friendlyByteBuf.writeInt(Block.getId(this.getBlockState()));
        friendlyByteBuf.writeComponent(this.getName());
    }

    public static Location fromNBT(CompoundTag tag) {
        return new Location(
                tag.getUUID("Id"),
                NBTHelper.readGlobalPos(tag.getCompound("Position")),
                MCCivilizationsRegistries.getLocationType(tag.getString("LocationType")),
                tag.contains("BlockState") ? NbtUtils.readBlockState(tag.getCompound("BlockState")) : Blocks.AIR.defaultBlockState(),
                Component.Serializer.fromJson(tag.getString("Name"))
        );
    }

    public static Location decode(FriendlyByteBuf friendlyByteBuf) {
        return new Location(
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readGlobalPos(),
                friendlyByteBuf.readRegistryId(),
                Block.stateById(friendlyByteBuf.readInt()),
                friendlyByteBuf.readComponent()
        );
    }
}
