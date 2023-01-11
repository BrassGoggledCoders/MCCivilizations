package xyz.brassgoggledcoders.mccivilizations.civilization;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizations;

import java.util.UUID;

public class Civilizations implements ICivilizations {
    @Override
    @Nullable
    public Civilization getCivilizationByCitizen(Entity entity) {
        return new Civilization(
                UUID.randomUUID(),
                Component.literal("Skylandia"),
                ItemStack.EMPTY
        );
    }

    @Override
    @Nullable
    public Civilization getCivilizationById(UUID id) {
        return null;
    }

    @Override
    public void createCivilization(Civilization civilization) {

    }

    @Override
    public void joinCivilization(Civilization newCivilization, Entity player) {

    }
}
