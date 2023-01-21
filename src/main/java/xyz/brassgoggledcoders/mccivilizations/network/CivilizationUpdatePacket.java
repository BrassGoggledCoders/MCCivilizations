package xyz.brassgoggledcoders.mccivilizations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.network.queue.ClientNetworkQueue;

import java.util.Collection;
import java.util.function.Supplier;

public record CivilizationUpdatePacket(
        Collection<Civilization> civilizations,
        ChangeType changeType,
        int priority
) {
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeCollection(this.civilizations(), (listByteBuf, civilization) -> civilization.toNetwork(listByteBuf));
        friendlyByteBuf.writeEnum(this.changeType());
        friendlyByteBuf.writeInt(this.priority());
    }

    public void consume(Supplier<NetworkEvent.Context> ignored) {
        ClientNetworkQueue.getInstance()
                .queue(
                        this,
                        value -> true,
                        CivilizationUpdatePacket::handle,
                        this.priority()
                );
    }

    public static void handle(CivilizationUpdatePacket packet) {
        ICivilizationRepository civilizationRepository = CivilizationRepositories.getCivilizationRepository();
        if (packet.changeType() == ChangeType.ADD) {
            packet.civilizations().forEach(civilizationRepository::upsertCivilization);
        } else if (packet.changeType() == ChangeType.REMOVE) {
            packet.civilizations().forEach(civilizationRepository::removeCivilization);
        }
    }

    public static CivilizationUpdatePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new CivilizationUpdatePacket(
                friendlyByteBuf.readList(Civilization::fromNetwork),
                friendlyByteBuf.readEnum(ChangeType.class),
                friendlyByteBuf.readInt()
        );
    }
}
