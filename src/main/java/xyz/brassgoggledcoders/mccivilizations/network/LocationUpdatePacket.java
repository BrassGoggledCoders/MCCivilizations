package xyz.brassgoggledcoders.mccivilizations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.location.Location;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.network.queue.ClientNetworkQueue;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public record LocationUpdatePacket(
        UUID civilizationId,
        Collection<Location> locations,
        ChangeType changeType,
        int priority
) {

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.civilizationId());
        friendlyByteBuf.writeCollection(this.locations(), (collectionByteBuf, location) -> location.encode(collectionByteBuf));
        friendlyByteBuf.writeEnum(this.changeType());
        friendlyByteBuf.writeInt(this.priority());
    }

    public void consume(Supplier<NetworkEvent.Context> ignored) {
        ClientNetworkQueue.getInstance()
                .queue(
                        this,
                        value -> CivilizationRepositories.getCivilizationRepository()
                                .civilizationExists(value.civilizationId()),
                        value -> {

                            Civilization civilization = CivilizationRepositories.getCivilizationRepository()
                                    .getCivilizationById(this.civilizationId());
                            if (civilization != null) {
                                for (Location location : this.locations()) {
                                    if (value.changeType() == ChangeType.ADD) {
                                        CivilizationRepositories.getLocationRepository()
                                                .addLocation(civilization, location);
                                    } else {
                                        CivilizationRepositories.getLocationRepository()
                                                .removeLocation(civilization, location);
                                    }
                                }

                            }

                        },
                        this.priority()
                );
    }

    public static LocationUpdatePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new LocationUpdatePacket(
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readList(Location::decode),
                friendlyByteBuf.readEnum(ChangeType.class),
                friendlyByteBuf.readInt()
        );
    }
}
