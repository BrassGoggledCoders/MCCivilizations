package xyz.brassgoggledcoders.mccivilizations.api.civilization;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Civilization {
    private final UUID id;

    private Component name;
    private ItemStack banner;
    private DyeColor dyeColor;

    public Civilization(UUID id, Component name, ItemStack banner, DyeColor dyeColor) {
        this.id = id;
        this.name = name;
        this.banner = banner;
        this.dyeColor = dyeColor;
    }

    @NotNull
    public UUID getId() {
        return id;
    }

    public void setName(Component name) {
        this.name = name;
    }

    public Component getName() {
        return name;
    }


    public void setBanner(ItemStack itemStack) {
        this.banner = itemStack;
    }

    public ItemStack getBanner() {
        return banner;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    public void setDyeColor(DyeColor dyeColor) {
        this.dyeColor = dyeColor;
    }
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", this.getId());
        tag.putString("Name", Component.Serializer.toJson(this.getName()));
        tag.put("Banner", this.getBanner().save(new CompoundTag()));
        tag.putInt("DyeColor", this.getDyeColor().getId());
        return tag;
    }

    public void toNetwork(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.getId());
        friendlyByteBuf.writeComponent(this.getName());
        friendlyByteBuf.writeItem(this.getBanner());
        friendlyByteBuf.writeEnum(this.getDyeColor());
    }

    public static Civilization fromTag(CompoundTag tag) {
        return new Civilization(
                tag.getUUID("Id"),
                Component.Serializer.fromJson(tag.getString("Name")),
                ItemStack.of(tag.getCompound("Banner")),
                DyeColor.byId(tag.getInt("DyeColor"))
        );
    }

    public static Civilization fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return new Civilization(
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readComponent(),
                friendlyByteBuf.readItem(),
                friendlyByteBuf.readEnum(DyeColor.class)
        );
    }
}
