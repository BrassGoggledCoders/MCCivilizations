package xyz.brassgoggledcoders.mccivilizations.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;

public class NetworkHandler {
    private static final NetworkHandler INSTANCE = new NetworkHandler();
    private static final String VERSION = "1";

    private final SimpleChannel simpleChannel;

    public NetworkHandler() {
        this.simpleChannel = NetworkRegistry.newSimpleChannel(
                MCCivilizations.rl("main"),
                () -> VERSION,
                VERSION::equals,
                VERSION::equals
        );

        this.simpleChannel.messageBuilder(CivilizationUpdatePacket.class, 0)
                .decoder(CivilizationUpdatePacket::decode)
                .encoder(CivilizationUpdatePacket::encode)
                .consumerMainThread(CivilizationUpdatePacket::consume)
                .add();

        this.simpleChannel.messageBuilder(LandClaimUpdatePacket.class, 1)
                .decoder(LandClaimUpdatePacket::decode)
                .encoder(LandClaimUpdatePacket::encode)
                .consumerMainThread(LandClaimUpdatePacket::consume)
                .add();

        this.simpleChannel.messageBuilder(LandClaimClaimPacket.class, 2)
                .decoder(LandClaimClaimPacket::decode)
                .encoder(LandClaimClaimPacket::encode)
                .consumerMainThread(LandClaimClaimPacket::consume)
                .add();
    }

    public void sendPacket(ServerPlayer serverPlayer, Object packet) {
        this.simpleChannel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
    }

    public void sendPacketToAll(Object packet) {
        this.simpleChannel.send(PacketDistributor.ALL.noArg(), packet);
    }

    public void sendPacketToServer(Object packet) {
        this.simpleChannel.send(PacketDistributor.SERVER.noArg(), packet);
    }

    public static NetworkHandler getInstance() {
        return INSTANCE;
    }

    public static void setup() {

    }
}
