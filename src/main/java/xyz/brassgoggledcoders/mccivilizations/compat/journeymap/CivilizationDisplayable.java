package xyz.brassgoggledcoders.mccivilizations.compat.journeymap;

import journeymap.client.api.display.Displayable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;


public record CivilizationDisplayable(
        Civilization civilization,
        ResourceKey<Level> level,
        ChunkPos chunkPos,
        Displayable displayable
) {
}
