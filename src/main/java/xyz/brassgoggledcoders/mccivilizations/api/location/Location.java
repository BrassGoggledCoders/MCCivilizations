package xyz.brassgoggledcoders.mccivilizations.api.location;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
    private Component name;

    public Location(@NotNull UUID id, @NotNull GlobalPos position, @NotNull LocationType locationType, Component name) {
        this.id = id;
        this.position = position;
        this.locationType = locationType;
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
        tag.putString("Name", Component.Serializer.toJson(this.getName()));
        return tag;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.getId());
        friendlyByteBuf.writeGlobalPos(this.getPosition());
        friendlyByteBuf.writeRegistryId(MCCivilizationsRegistries.LOCATION_TYPE_REGISTRY.get(), this.getLocationType());
        friendlyByteBuf.writeComponent(this.getName());
    }

    public static Location fromNBT(CompoundTag tag) {
        return new Location(
                tag.getUUID("Id"),
                NBTHelper.readGlobalPos(tag.getCompound("Position")),
                MCCivilizationsRegistries.getLocationType(tag.getString("LocationType")),
                Component.Serializer.fromJson(tag.getString("Name"))
        );
    }

    public static Location decode(FriendlyByteBuf friendlyByteBuf) {
        return new Location(
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readGlobalPos(),
                friendlyByteBuf.readRegistryId(),
                friendlyByteBuf.readComponent()
        );
    }
}
