package xyz.brassgoggledcoders.mccivilizations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;

import java.util.function.Supplier;

public record CivilizationUpdatePacket(
        Civilization civilization,
        ChangeType changeType
) {
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        this.civilization().toNetwork(friendlyByteBuf);
        friendlyByteBuf.writeEnum(this.changeType());
    }

    public void consume(Supplier<NetworkEvent.Context> ignored) {
        if (this.changeType() == ChangeType.ADD) {
            CivilizationRepositories.getCivilizationRepository()
                    .upsertCivilization(this.civilization());
        } else if (this.changeType() == ChangeType.DELETE) {
            CivilizationRepositories.getCivilizationRepository()
                    .removeCivilization(this.civilization());
        }
    }

    public static CivilizationUpdatePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new CivilizationUpdatePacket(
                Civilization.fromNetwork(friendlyByteBuf),
                friendlyByteBuf.readEnum(ChangeType.class)
        );
    }
}
