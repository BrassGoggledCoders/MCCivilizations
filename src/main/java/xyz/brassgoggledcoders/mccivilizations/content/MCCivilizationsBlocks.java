package xyz.brassgoggledcoders.mccivilizations.content;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.Util;
import net.minecraft.world.item.DyeColor;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.block.CivilizationBannerBlock;
import xyz.brassgoggledcoders.mccivilizations.block.CivilizationBannerType;
import xyz.brassgoggledcoders.mccivilizations.blockentity.CivilizationBannerBlockEntity;

import java.util.Locale;

public class MCCivilizationsBlocks {

    public static final Table<CivilizationBannerType, DyeColor, RegistryEntry<CivilizationBannerBlock>> STANDING_BANNERS =
            Util.make(
                    HashBasedTable.create(CivilizationBannerType.values().length, DyeColor.values().length),
                    table -> {
                        for (CivilizationBannerType type : CivilizationBannerType.values()) {
                            for (DyeColor dyeColor : DyeColor.values()) {
                                RegistryEntry<CivilizationBannerBlock> entry = MCCivilizations.getRegistrate()
                                        .object("%s_%s_banner".formatted(dyeColor.getName(), type.name().toLowerCase(Locale.US)))
                                        .block(properties -> new CivilizationBannerBlock(type, dyeColor, properties))
                                        .register();

                                table.put(type, dyeColor, entry);
                            }
                        }
                    }
            );

    public static final BlockEntityEntry<CivilizationBannerBlockEntity> CIVILIZATION_BANNER_BLOCK_ENTITY = Util.make(() -> {
        BlockEntityBuilder<CivilizationBannerBlockEntity, Registrate> builder = MCCivilizations.getRegistrate()
                .object("civilization_banner")
                .blockEntity(CivilizationBannerBlockEntity::new);

        STANDING_BANNERS.values().forEach(builder::validBlock);

        return builder.register();
    });


    public static void setup() {

    }
}
