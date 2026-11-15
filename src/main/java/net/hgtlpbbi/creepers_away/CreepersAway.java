package net.hgtlpbbi.creepers_away;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(CreepersAway.MODID)
public final class CreepersAway {
    public static final String MODID = "creepers_away";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreepersAway(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        Config.register(modEventBus);
    }
}