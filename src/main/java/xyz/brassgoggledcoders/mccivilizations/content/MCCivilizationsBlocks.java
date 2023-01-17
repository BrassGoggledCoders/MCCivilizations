package xyz.brassgoggledcoders.mccivilizations.content;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.Util;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.block.CivilizationBannerType;
import xyz.brassgoggledcoders.mccivilizations.block.HangingCivilizationBannerBlock;
import xyz.brassgoggledcoders.mccivilizations.block.StandingCivilizationBannerBlock;
import xyz.brassgoggledcoders.mccivilizations.blockentity.CivilizationBannerBlockEntity;
import xyz.brassgoggledcoders.mccivilizations.item.CivilizationBannerBlockItem;
import xyz.brassgoggledcoders.mccivilizations.recipe.TransferBannerPatternRecipeBuilder;
import xyz.brassgoggledcoders.mccivilizations.render.CivilizationBannerBlockEntityRenderer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unused")
public class MCCivilizationsBlocks {

    public static final Map<CivilizationBannerType, BlockEntry<StandingCivilizationBannerBlock>> STANDING_BANNERS =
            Util.make(
                    new HashMap<>(CivilizationBannerType.values().length),
                    map -> {
                        for (CivilizationBannerType type : CivilizationBannerType.values()) {
                            BlockEntry<StandingCivilizationBannerBlock> entry = MCCivilizations.getRegistrate()
                                    .object("%s_standing_banner".formatted(type.name().toLowerCase(Locale.US)))
                                    .block(properties -> new StandingCivilizationBannerBlock(type, properties))
                                    .lang("%s Banner".formatted(type.getLang()))
                                    .blockstate((context, provider) -> provider.simpleBlock(
                                            context.get(),
                                            provider.models()
                                                    .getExistingFile(provider.mcLoc("block/banner"))
                                    ))
                                    .register();

                            map.put(type, entry);
                        }
                    }
            );

    public static final Map<CivilizationBannerType, BlockEntry<HangingCivilizationBannerBlock>> HANGING_BANNERS =
            Util.make(
                    new HashMap<>(CivilizationBannerType.values().length),
                    map -> {
                        for (CivilizationBannerType type : CivilizationBannerType.values()) {
                            BlockEntry<HangingCivilizationBannerBlock> entry = MCCivilizations.getRegistrate()
                                    .object("%s_hanging_banner".formatted(type.name().toLowerCase(Locale.US)))
                                    .block(properties -> new HangingCivilizationBannerBlock(type, properties))
                                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                                    .blockstate((context, provider) -> provider.simpleBlock(
                                            context.get(),
                                            provider.models()
                                                    .getExistingFile(provider.mcLoc("block/banner"))
                                    ))
                                    .register();

                            map.put(type, entry);
                        }
                    }
            );

    public static final Map<CivilizationBannerType, ItemEntry<CivilizationBannerBlockItem>> BANNER_ITEMS =
            Util.make(
                    new HashMap<>(CivilizationBannerType.values().length),
                    map -> {
                        for (CivilizationBannerType type : CivilizationBannerType.values()) {
                            ItemEntry<CivilizationBannerBlockItem> entry = MCCivilizations.getRegistrate()
                                    .object("%s_banner".formatted(type.name().toLowerCase(Locale.US)))
                                    .item(properties -> new CivilizationBannerBlockItem(
                                            STANDING_BANNERS.get(type).get(),
                                            HANGING_BANNERS.get(type).get(),
                                            properties
                                    ))
                                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                                    .model((context, provider) -> provider.withExistingParent(
                                            context.getName(),
                                            provider.mcLoc("item/template_banner")
                                    ))
                                    .recipe((context, provider) -> TransferBannerPatternRecipeBuilder.of(context.get())
                                            .withIngredient(type.getIngredient())
                                            .save(provider)
                                    )
                                    .tag(MCCivilizationsItemTags.NO_COST_NAMING)
                                    .register();

                            map.put(type, entry);
                        }
                    }
            );

    public static final BlockEntityEntry<CivilizationBannerBlockEntity> CIVILIZATION_BANNER_BLOCK_ENTITY = Util.make(() -> {
        BlockEntityBuilder<CivilizationBannerBlockEntity, Registrate> builder = MCCivilizations.getRegistrate()
                .object("civilization_banner")
                .<CivilizationBannerBlockEntity>blockEntity(CivilizationBannerBlockEntity::new)
                .renderer(() -> CivilizationBannerBlockEntityRenderer::new);

        STANDING_BANNERS.values().forEach(builder::validBlock);
        HANGING_BANNERS.values().forEach(builder::validBlock);

        return builder.register();
    });

    public static void setup() {

    }
}
