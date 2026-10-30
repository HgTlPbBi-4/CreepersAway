package net.hachimi.creepers_away;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> FEARED_ENTITIES = BUILDER
            .comment("苦力怕会害怕的实体 ID 列表（行为与原版怕猫一致：6 格内转身逃跑）。",
                    "原版硬编码的猫/豹猫躲避已被移除，此列表完全接管：默认包含 cat 和 ocelot，",
                    "把它们删掉苦力怕就不再怕猫了。例：[\"minecraft:cat\", \"minecraft:ocelot\"]")
            .defineListAllowEmpty("fearedEntities", List.of("minecraft:cat", "minecraft:ocelot"), () -> "minecraft:villager",
                    value -> value instanceof String id && Config.isValidEntry(id));

    private static final ModConfigSpec.ConfigValue<List<? extends String>> FEARED_CURIOS = BUILDER
            .comment("戴着这些饰品的玩家会让苦力怕害怕：6 格内逃跑，且附近不会自动引爆。",
                    "支持物品 ID 和物品标签，任意 Curios 槽位都算：",
                    "  \"somemod:charm_of_creepers\"  单个物品",
                    "  \"#somemod:scares_creepers\"   物品标签",
                    "  \"#curios:charm\"              所有被声明为护符(charm)的饰品",
                    "Curios 未安装时此项自动失效。")
            .defineListAllowEmpty("fearedCurios", List.of(), () -> "#curios:charm",
                    value -> value instanceof String id && Config.isValidEntry(id));

    static final ModConfigSpec SPEC = BUILDER.build();

    private static volatile Set<ResourceLocation> fearedIds = Set.of();
    private static volatile Set<ResourceLocation> fearedCurioIds = Set.of();
    private static volatile List<TagKey<Item>> fearedCurioTags = List.of();

    private Config() {
    }

    public static Set<ResourceLocation> fearedIds() {
        return fearedIds;
    }

    public static Set<ResourceLocation> fearedCurioIds() {
        return fearedCurioIds;
    }

    public static List<TagKey<Item>> fearedCurioTags() {
        return fearedCurioTags;
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((ModConfigEvent.Loading event) -> reloadIfOurs(event));
        modBus.addListener((ModConfigEvent.Reloading event) -> reloadIfOurs(event));
    }

    private static void reloadIfOurs(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        List<? extends String> curioEntries = FEARED_CURIOS.get();
        fearedIds = parseIds(FEARED_ENTITIES.get());
        fearedCurioIds = parseIds(curioEntries);
        fearedCurioTags = parseTags(curioEntries, Registries.ITEM);
        CreepersAway.LOGGER.info("[ca-debug] config loaded: entities={} curioIds={} curioTags={}",
                fearedIds, fearedCurioIds, fearedCurioTags.stream().map(TagKey::location).toList());
    }

    private static Set<ResourceLocation> parseIds(List<? extends String> entries) {
        return entries.stream()
                .map(String::trim)
                .filter(entry -> !entry.startsWith("#"))
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static <T> List<TagKey<T>> parseTags(List<? extends String> entries, ResourceKey<Registry<T>> registry) {
        return entries.stream()
                .map(String::trim)
                .filter(entry -> entry.startsWith("#"))
                .map(entry -> ResourceLocation.tryParse(entry.substring(1)))
                .filter(Objects::nonNull)
                .map(id -> TagKey.create(registry, id))
                .collect(Collectors.toUnmodifiableList());
    }

    private static boolean isValidEntry(String entry) {
        String value = entry.trim();
        return ResourceLocation.tryParse(value.startsWith("#") ? value.substring(1) : value) != null;
    }
}