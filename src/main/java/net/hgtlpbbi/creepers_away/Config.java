package net.hgtlpbbi.creepers_away;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> FEARED_ENTITIES = BUILDER
            .comment("苦力怕会害怕的实体 ID 列表。",
                    "已完全接管猫/豹猫：默认包含 cat 和 ocelot，",
                    "例：[\"minecraft:cat\", \"minecraft:ocelot\"]")
            .defineListAllowEmpty("fearedEntities", List.of("minecraft:cat", "minecraft:ocelot"), () -> "minecraft:villager",
                    value -> value instanceof String id && Config.isValidEntry(id));

    private static final ModConfigSpec.ConfigValue<List<? extends String>> FEARED_ARMOR = BUILDER
            .comment("苦力怕会害怕的玩家佩戴的护甲的 ID 列表",
                    "只匹配护甲栏（头盔、胸甲、护腿、靴子）。支持物品 ID 和物品标签，例如：",
                    "  \"minecraft:diamond_helmet\"   单个物品",
                    "  \"#minecraft:chest_armor\"      物品标签")
            .defineListAllowEmpty("fearedArmor", List.of(), () -> "minecraft:diamond_helmet",
                    value -> value instanceof String id && Config.isValidEntry(id));

    private static final ModConfigSpec.ConfigValue<List<? extends String>> FEARED_HELD = BUILDER
            .comment("苦力怕会害怕的玩家手持的物品的 ID 列表",
                    "只匹配主手和副手。支持物品 ID 和物品标签，例如：",
                    "  \"minecraft:diamond_sword\"    单个物品",
                    "  \"#minecraft:swords\"          物品标签")
            .defineListAllowEmpty("fearedHeld", List.of(), () -> "minecraft:diamond_sword",
                    value -> value instanceof String id && Config.isValidEntry(id));

    private static final ModConfigSpec.ConfigValue<List<? extends String>> FEARED_CURIOS = BUILDER
            .comment("苦力怕会害怕的玩家佩戴的饰品的 ID 列表",
                    "支持物品 ID 和物品标签，例如：",
                    "  \"somemod:charm_of_creepers\"  单个物品",
                    "  \"#curios:charm\"              所有被声明为护符(charm)的饰品",
                    "Curios 未安装时此项自动失效；护甲和手持请分别用 fearedArmor / fearedHeld。")
            .defineListAllowEmpty("fearedCurios", List.of(), () -> "#curios:charm",
                    value -> value instanceof String id && Config.isValidEntry(id));

    static final ModConfigSpec SPEC = BUILDER.build();

    private static volatile Set<Identifier> fearedIds = Set.of();
    private static volatile ItemTargets fearedCurioTargets = ItemTargets.EMPTY;
    private static volatile ItemTargets fearedArmorTargets = ItemTargets.EMPTY;
    private static volatile ItemTargets fearedHeldTargets = ItemTargets.EMPTY;
    private static volatile boolean anyWearTargets;

    private Config() {
    }

    public record ItemTargets(Set<Identifier> ids, List<TagKey<Item>> tags) {
        public static final ItemTargets EMPTY = new ItemTargets(Set.of(), List.of());

        static ItemTargets parse(List<? extends String> entries) {
            Set<Identifier> ids = new LinkedHashSet<>();
            List<TagKey<Item>> tags = new ArrayList<>();
            for (String raw : entries) {
                String entry = raw.trim();
                if (entry.isEmpty()) {
                    continue;
                }
                boolean tag = entry.startsWith("#");
                Identifier id = Identifier.tryParse(tag ? entry.substring(1) : entry);
                if (id == null) {
                    continue;
                }
                if (tag) {
                    tags.add(TagKey.create(Registries.ITEM, id));
                } else {
                    ids.add(id);
                }
            }
            return new ItemTargets(Set.copyOf(ids), List.copyOf(tags));
        }

        public boolean isEmpty() {
            return ids.isEmpty() && tags.isEmpty();
        }

        public boolean matches(ItemStack stack) {
            if (ids.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                return true;
            }
            for (TagKey<Item> tag : tags) {
                if (stack.is(tag)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static Set<Identifier> fearedIds() {
        return fearedIds;
    }

    public static ItemTargets fearedCurios() {
        return fearedCurioTargets;
    }

    public static ItemTargets fearedArmor() {
        return fearedArmorTargets;
    }

    public static ItemTargets fearedHeld() {
        return fearedHeldTargets;
    }

    /** Cheap snapshot flag so the hot paths can bail out when nothing is configured. */
    public static boolean anyWearTargets() {
        return anyWearTargets;
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((ModConfigEvent.Loading event) -> reloadIfOurs(event));
        modBus.addListener((ModConfigEvent.Reloading event) -> reloadIfOurs(event));
    }

    private static void reloadIfOurs(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        fearedIds = parseIds(FEARED_ENTITIES.get());
        fearedCurioTargets = ItemTargets.parse(FEARED_CURIOS.get());
        fearedArmorTargets = ItemTargets.parse(FEARED_ARMOR.get());
        fearedHeldTargets = ItemTargets.parse(FEARED_HELD.get());
        anyWearTargets = !fearedCurioTargets.isEmpty()
                || !fearedArmorTargets.isEmpty()
                || !fearedHeldTargets.isEmpty();
        CreepersAway.LOGGER.debug("creepers_away: loaded {} entity id(s), armour ids {}, held ids {}", 
                fearedIds.size(), fearedArmorTargets.ids().size(), fearedHeldTargets.ids().size());
    }

    private static Set<Identifier> parseIds(List<? extends String> entries) {
        return entries.stream()
                .map(String::trim)
                .filter(entry -> !entry.startsWith("#"))
                .map(Identifier::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean isValidEntry(String entry) {
        String value = entry.trim();
        return Identifier.tryParse(value.startsWith("#") ? value.substring(1) : value) != null;
    }
}