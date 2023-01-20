package xyz.brassgoggledcoders.mccivilizations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;

import java.util.Collection;
import java.util.function.Supplier;

public record CivilizationUpdatePacket(
        Collection<Civilization> civilizations,
        ChangeType changeType
) {
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeCollection(this.civilizations(), (listByteBuf, civilization) -> civilization.toNetwork(listByteBuf));
        friendlyByteBuf.writeEnum(this.changeType());
    }

    public void consume(Supplier<NetworkEvent.Context> ignored) {
        ICivilizationRepository civilizationRepository = CivilizationRepositories.getCivilizationRepository();
        if (this.changeType() == ChangeType.ADD) {
            this.civilizations().forEach(civilizationRepository::upsertCivilization);

        } else if (this.changeType() == ChangeType.DELETE) {
            this.civilizations().forEach(civilizationRepository::removeCivilization);
        }
    }

    public static CivilizationUpdatePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new CivilizationUpdatePacket(
                friendlyByteBuf.readList(Civilization::fromNetwork),
                friendlyByteBuf.readEnum(ChangeType.class)
        );
    }
}
