package xyz.brassgoggledcoders.mccivilizations.network;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;

import java.util.function.Supplier;

public record LandClaimClaimPacket(
        ResourceKey<Level> level,
        ChunkPos chunkPos,
        ChangeType changeType
) {

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceKey(this.level());
        friendlyByteBuf.writeInt(this.chunkPos().x);
        friendlyByteBuf.writeInt(this.chunkPos().z);
        friendlyByteBuf.writeEnum(this.changeType());
    }

    public void consume(Supplier<NetworkEvent.Context> contextSupplier) {
        ServerPlayer serverPlayer = contextSupplier.get().getSender();
        if (serverPlayer != null) {
            Civilization playerCivilization = CivilizationRepositories.getCivilizationRepository()
                    .getCivilizationByCitizen(serverPlayer);

            if (playerCivilization != null) {
                ILandClaimRepository repository = CivilizationRepositories.getLandClaimRepository();
                if (this.changeType() == ChangeType.ADD) {
                    repository.addClaim(playerCivilization, this.level(), this.chunkPos());
                } else if (this.changeType() == ChangeType.REMOVE) {
                    repository.removeClaim(playerCivilization, this.level(), this.chunkPos());
                }
            }
        }
    }

    public static LandClaimClaimPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new LandClaimClaimPacket(
                friendlyByteBuf.readResourceKey(Registry.DIMENSION_REGISTRY),
                new ChunkPos(
                        friendlyByteBuf.readInt(),
                        friendlyByteBuf.readInt()
                ),
                friendlyByteBuf.readEnum(ChangeType.class)
        );
    }
}
