package xyz.brassgoggledcoders.mccivilizations.network;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public record LandClaimUpdatePacket(
        UUID civilizationId,
        Map<ResourceKey<Level>, Collection<ChunkPos>> positions,
        ChangeType changeType
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
    }

    public void consume(Supplier<NetworkEvent.Context> ignoredContextSupplier) {
        ILandClaimRepository landClaimRepository = CivilizationRepositories.getLandClaimRepository();
        ICivilizationRepository civilizationRepository = CivilizationRepositories.getCivilizationRepository();

        Civilization civilization = civilizationRepository.getCivilizationById(this.civilizationId());

        for (Map.Entry<ResourceKey<Level>, Collection<ChunkPos>> entry : this.positions().entrySet()) {
            if (this.changeType() == ChangeType.REPLACE) {
                landClaimRepository.setClaims(this.civilizationId(), entry.getKey(), entry.getValue());
            } else if (civilization != null) {
                if (this.changeType() == ChangeType.ADD) {
                    landClaimRepository.addClaims(civilization, entry.getKey(), entry.getValue());
                } else if (this.changeType() == ChangeType.DELETE) {
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
                friendlyByteBuf.readEnum(ChangeType.class)
        );
    }
}
