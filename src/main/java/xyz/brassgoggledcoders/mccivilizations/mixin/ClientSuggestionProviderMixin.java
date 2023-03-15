package xyz.brassgoggledcoders.mccivilizations.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.brassgoggledcoders.mccivilizations.command.suggestion.IHasPlayer;

@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin implements IHasPlayer {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Override
    public Player getPlayer() {
        return minecraft.player;
    }
}
