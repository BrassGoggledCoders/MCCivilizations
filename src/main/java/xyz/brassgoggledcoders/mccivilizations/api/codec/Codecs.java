package xyz.brassgoggledcoders.mccivilizations.api.codec;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public class Codecs {
    public static Codec<Component> COMPONENT = new JsonCodec<>(
            Component.Serializer::fromJson,
            Component.Serializer::toJsonTree
    );
}
