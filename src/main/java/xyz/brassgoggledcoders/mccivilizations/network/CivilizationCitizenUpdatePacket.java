package xyz.brassgoggledcoders.mccivilizations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.network.queue.ClientNetworkQueue;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public record CivilizationCitizenUpdatePacket(
        UUID civilizationId,
        Collection<UUID> citizens,
        ChangeType changeType
) {

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.civilizationId());
        friendlyByteBuf.writeCollection(this.citizens(), FriendlyByteBuf::writeUUID);
        friendlyByteBuf.writeEnum(this.changeType());
    }

    public void consume(Supplier<NetworkEvent.Context> ignored) {
        ClientNetworkQueue.getInstance()
                .queue(
                        this,
                        value -> CivilizationRepositories.getCivilizationRepository().getCivilizationById(value.civilizationId()) != null,
                        value -> {
                            ICivilizationRepository repository = CivilizationRepositories.getCivilizationRepository();
                            Civilization civilization = repository.getCivilizationById(value.civilizationId());
                            if (civilization != null) {
                                for (UUID citizen: this.citizens()) {
                                    if (this.changeType() == ChangeType.ADD) {
                                        repository.joinCivilization(civilization, citizen);
                                    } else {
                                        repository.leaveCivilization(civilization, citizen);
                                    }
                                }

                            } else {
                                MCCivilizations.LOGGER.error("Failed to Find Civilization for Id: {}", value.civilizationId());
                            }
                        },
                        3
                );
    }

    public static CivilizationCitizenUpdatePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new CivilizationCitizenUpdatePacket(
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readList(FriendlyByteBuf::readUUID),
                friendlyByteBuf.readEnum(ChangeType.class)
        );
    }
}
