package xyz.brassgoggledcoders.mccivilizations.api.civilization;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Civilization {
    private final UUID id;

    private Component name;
    private ItemStack banner;

    public Civilization(UUID id, Component name, ItemStack banner) {
        this.id = id;
        this.name = name;
        this.banner = banner;
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

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", this.getId());
        tag.putString("Name", Component.Serializer.toJson(this.getName()));
        tag.put("Banner", this.getBanner().save(new CompoundTag()));
        return tag;
    }

    public void toNetwork(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.getId());
        friendlyByteBuf.writeComponent(this.getName());
        friendlyByteBuf.writeItem(friendlyByteBuf.readItem());
    }

    public static Civilization fromTag(CompoundTag tag) {
        return new Civilization(
                tag.getUUID("Id"),
                Component.Serializer.fromJson(tag.getString("Name")),
                ItemStack.of(tag.getCompound("Banner"))
        );
    }

    public static Civilization fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return new Civilization(
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readComponent(),
                friendlyByteBuf.readItem()
        );
    }
}
