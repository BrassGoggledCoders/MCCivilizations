package xyz.brassgoggledcoders.mccivilizations.network;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.network.queue.ClientNetworkQueue;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public record LandClaimUpdatePacket(
        UUID civilizationId,
        Map<ResourceKey<Level>, Collection<ChunkPos>> positions,
        ChangeType changeType,
        int priority
) {

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.civilizationId());
        friendlyByteBuf.writeMap(
                this.positions(),
                FriendlyByteBuf::writeResourceKey,
                (valueByteBuf, positions) -> valueByteBuf.writeCollection(
                        positions,
                        (collectByteBuf, position) -> {
                            collectByteBuf.writeInt(position.x);
                            collectByteBuf.writeInt(position.z);
                        }
                )
        );
        friendlyByteBuf.writeEnum(this.changeType());
        friendlyByteBuf.writeInt(this.priority());
    }

    public void consume(Supplier<NetworkEvent.Context> ignoredContextSupplier) {
        ClientNetworkQueue.getInstance()
                .queue(
                        this,
                        LandClaimUpdatePacket::isReady,
                        LandClaimUpdatePacket::handle,
                        this.priority()
                );
    }

    public static boolean isReady(LandClaimUpdatePacket packet) {
        return CivilizationRepositories.getCivilizationRepository()
                .getCivilizationById(packet.civilizationId()) != null;
    }

    public static void handle(LandClaimUpdatePacket packet) {
        ILandClaimRepository landClaimRepository = CivilizationRepositories.getLandClaimRepository();
        ICivilizationRepository civilizationRepository = CivilizationRepositories.getCivilizationRepository();

        Civilization civilization = civilizationRepository.getCivilizationById(packet.civilizationId());

        for (Map.Entry<ResourceKey<Level>, Collection<ChunkPos>> entry : packet.positions().entrySet()) {
            if (civilization != null) {
                if (packet.changeType() == ChangeType.ADD) {
                    landClaimRepository.addClaims(civilization, entry.getKey(), entry.getValue());
                } else if (packet.changeType() == ChangeType.REMOVE) {
                    landClaimRepository.removeClaims(civilization, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public static LandClaimUpdatePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new LandClaimUpdatePacket(
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readMap(
                        keyByteBuf -> keyByteBuf.readResourceKey(Registry.DIMENSION_REGISTRY),
                        valueByteBuf -> valueByteBuf.readList(collectionByteBuf -> new ChunkPos(
                                collectionByteBuf.readInt(),
                                collectionByteBuf.readInt()
                        ))
                ),
                friendlyByteBuf.readEnum(ChangeType.class),
                friendlyByteBuf.readInt()
        );
    }
}
