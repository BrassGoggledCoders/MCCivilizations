package xyz.brassgoggledcoders.mccivilizations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public record LandClaimUpdatePacket(
        UUID civilizationId,
        Collection<ChunkPos> positions,
        ChangeType changeType
) {

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.civilizationId());
        friendlyByteBuf.writeCollection(this.positions(), (collectByteBuf, position) -> {
            collectByteBuf.writeInt(position.x);
            collectByteBuf.writeInt(position.z);
        });
        friendlyByteBuf.writeEnum(this.changeType());
    }

    public void consume(Supplier<NetworkEvent.Context> contextSupplier) {
        addClaims(contextSupplier.get().getDirection(), contextSupplier.get().getSender());
    }

    public void addClaims(NetworkDirection direction, @Nullable ServerPlayer player) {
        ILandClaimRepository landClaimRepository = CivilizationRepositories.getLandClaimRepository();
        ICivilizationRepository civilizationRepository = CivilizationRepositories.getCivilizationRepository();

        Civilization civilization = civilizationRepository.getCivilizationById(this.civilizationId());

        if (direction == NetworkDirection.LOGIN_TO_CLIENT) {
            if (this.changeType() == ChangeType.REPLACE) {
                landClaimRepository.setClaims(this.civilizationId(), this.positions());
            } else if (civilization != null) {
                if (this.changeType() == ChangeType.ADD) {
                    landClaimRepository.addClaims(civilization, this.positions);
                } else if (this.changeType() == ChangeType.DELETE) {
                    landClaimRepository.removeClaims(civilization, this.positions);
                }
            }
        } else if (direction == NetworkDirection.PLAY_TO_SERVER) {
            if (player != null) {
                Civilization playerCivilization = civilizationRepository.getCivilizationByCitizen(player);
                if (playerCivilization == civilization) {
                    if (this.changeType() == ChangeType.ADD) {
                        landClaimRepository.addClaims(civilization, this.positions);
                    } else if (this.changeType() == ChangeType.DELETE) {
                        landClaimRepository.removeClaims(civilization, this.positions);
                    }
                }
            }
        }
    }

    public static LandClaimUpdatePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new LandClaimUpdatePacket(
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readList(collectionByteBuf -> new ChunkPos(
                        collectionByteBuf.readInt(),
                        collectionByteBuf.readInt()
                )),
                friendlyByteBuf.readEnum(ChangeType.class)
        );
    }
}
