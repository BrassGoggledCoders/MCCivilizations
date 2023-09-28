package xyz.brassgoggledcoders.mccivilizations.api.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import xyz.brassgoggledcoders.mccivilizations.api.codec.Codecs;

public record Resource(
    long min,
    long max,
    Component name,
    Component group
) {
    public static final Codec<Resource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("min", 0L).forGetter(Resource::min),
            Codec.LONG.optionalFieldOf("max", Long.MAX_VALUE).forGetter(Resource::max),
            Codecs.COMPONENT.fieldOf("name").forGetter(Resource::name),
            Codecs.COMPONENT.fieldOf("group").forGetter(Resource::group)
    ).apply(instance, Resource::new));
}
