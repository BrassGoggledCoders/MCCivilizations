package xyz.brassgoggledcoders.mccivilizations.api.civilization;

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
    }

    public void setName(Component name) {
        this.name = name;
    }

    public Component getName() {
        return name;
    }

    @NotNull
    public UUID getId() {
        return id;
    }

    public void setBanner(ItemStack itemStack) {
        this.banner = itemStack;
    }

    public ItemStack getBanner() {
        return banner;
    }
}
