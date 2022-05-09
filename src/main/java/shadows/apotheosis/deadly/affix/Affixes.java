package shadows.apotheosis.deadly.affix;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.random.Weight;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.impl.*;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


public class Affixes {

//#region ------------------ DEFAULT CONFIGS ----------------------
    static final Map<String, AffixConfig> DEFAULT_CONFIGS = ImmutableMap.<String, AffixConfig>builder()
        .put("damage_chain",
                new IntAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(1),
                        UniformInt.of(2, 7),
                        new IntAffixConfig.MergeHandler(1, 10, IntAffixConfig.MergeFunction.ADD_HALF)
                )
        ).put("disengage",
                new NoValueAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(3)
                )
        ).put("eldritch_block",
                new NoValueAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(1)
                )
        ).put("enchantability",
                new IntAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(5),
                        UniformInt.of(9, 25),
                        new IntAffixConfig.MergeHandler(2, 30, IntAffixConfig.MergeFunction.ADD_HALF)
                )
        ).put("execute",
                new FloatAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(5),
                        UniformFloat.of(0.05F, 0.1F),
                        new FloatAffixConfig.MergeHandler(0.03F, 0.2F, FloatAffixConfig.MergeFunction.ADD_HALF)
                )
        ).put("loot_pinata",
                new FloatAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(2),
                        UniformFloat.of(0.001F, 0.02F),
                        new FloatAffixConfig.MergeHandler(0.001F, 0.03F, FloatAffixConfig.MergeFunction.ADD_HALF)
                )
        ).put("magic_arrow",
                new NoValueAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(1)
                )
        ).put("max_crit",
                new NoValueAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(1)
                )
        ).put("omnitool",
                new NoValueAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(2)
                )
        ).put("piercing",
                new NoValueAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(1)
                )
        ).put("radius_mining",
                new IntAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(2),
                        UniformInt.of(1, 3),
                        new IntAffixConfig.MergeHandler(1, 3, IntAffixConfig.MergeFunction.KEEP_HIGHER)
                )
        ).put("snare_hit",
                new IntAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(1),
                        UniformInt.of(2, 6),
                        new IntAffixConfig.MergeHandler(1, 10, IntAffixConfig.MergeFunction.ADD_HALF)
                )
        ).put("snipe_damage",
                new IntAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(3),
                        UniformInt.of(2, 10),
                        new IntAffixConfig.MergeHandler(1, 15, IntAffixConfig.MergeFunction.ADD_HALF)
                )
        ).put("spectral_arrow",
                new FloatAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(2),
                        UniformFloat.of(0.1F, 1F),
                        new FloatAffixConfig.MergeHandler(0.1F, 1F, FloatAffixConfig.MergeFunction.ADD_HALF)
                )
        ).put("spiked",
                new FloatAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(2),
                        UniformFloat.of(0.4F, 1F),
                        new FloatAffixConfig.MergeHandler(0.4F, 1.5F, FloatAffixConfig.MergeFunction.ADD_HALF)
                )
        ).put("teleport_drops",
                new IntAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(2),
                        UniformInt.of(2, 6),
                        new IntAffixConfig.MergeHandler(1, 64, IntAffixConfig.MergeFunction.ADD_HALF)
                )
        ).put("torch_placement",
                new IntAffixConfig(
                        LootRarity.COMMON,
                        Weight.of(4),
                        UniformInt.of(4, 8),
                        new IntAffixConfig.MergeHandler(4, 8, IntAffixConfig.MergeFunction.ADD_HALF)
                )
        ).build();
//#endregion


    public static void register(Register<Affix> e) {

        IForgeRegistry<Affix> reg = e.getRegistry();
        //Formatter::off

        //generic
        register(reg, "enchantability", EnchantabilityAffix::new, IntAffixConfig.class);

        //melee
        register(reg, "crit_damage", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(t-> t == LootCategory.SWORD || t == LootCategory.HEAVY_WEAPON)
                    .setPrefix(true)
                    .build();
        }, AttributeAffixConfig.class);
        register(reg, "life_steal", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(t-> t == LootCategory.SWORD || t == LootCategory.HEAVY_WEAPON)
                    .setPrefix(true)
                    .build();
        }, AttributeAffixConfig.class);

        //common?
        register(reg, "reach_distance", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(t -> t == LootCategory.BREAKER || t == LootCategory.SWORD || t == LootCategory.HEAVY_WEAPON)
                    .build();
        }, AttributeAffixConfig.class);

        //bow
//                new DrawSpeedAffix(LootRarity.COMMON, 5).setRegistryName("draw_speed"),
        register(reg, "movement_speed", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(LootCategory::isRanged)
                    .setPrefix(true)
                    .build();
        }, AttributeAffixConfig.class);
        register(reg, "snipe_damage", SnipeDamageAffix::new, IntAffixConfig.class);
        register(reg, "spectral_shot", SpectralShotAffix::new, FloatAffixConfig.class);
        register(reg, "snare_hit", SnareHitAffix::new, IntAffixConfig.class);
        register(reg, "magic_arrow", MagicArrowAffix::new, NoValueAffixConfig.class);
        register(reg, "teleport_drops", TeleportDropsAffix::new, IntAffixConfig.class);

        //sword
        register(reg, "loot_pinata", LootPinataAffix::new, FloatAffixConfig.class);
        register(reg, "damage_chain", DamageChainAffix::new, IntAffixConfig.class);
        register(reg, "attack_speed", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(t-> t == LootCategory.SWORD)
                    .setPrefix(true)
                    .build();
        }, AttributeAffixConfig.class);
        register(reg, "cold_damage", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(t-> t == LootCategory.SWORD)
                    .setPrefix(true)
                    .build();
        }, AttributeAffixConfig.class);
        register(reg, "fire_damage", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(t-> t == LootCategory.SWORD)
                    .setPrefix(true)
                    .build();
        }, AttributeAffixConfig.class);
        register(reg, "crit_chance", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(t-> t == LootCategory.SWORD)
                    .setPrefix(true)
                    .build();
        }, AttributeAffixConfig.class);

        //axe / heavy?
        register(reg, "piercing", PiercingAffix::new, NoValueAffixConfig.class);
//                configured("cleave", CleaveAffix::new, FloatAffixConfig.class);
        register(reg, "execute", ExecuteAffix::new, FloatAffixConfig.class);
        register(reg, "max_crit", MaxCritAffix::new, NoValueAffixConfig.class);
        register(reg, "current_hp_damage", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(t-> t == LootCategory.HEAVY_WEAPON)
                    .build();
        }, AttributeAffixConfig.class);
        register(reg, "overheal", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(t-> t == LootCategory.HEAVY_WEAPON)
                    .build();
        }, AttributeAffixConfig.class);

        //tools
        register(reg, "torch_placement", TorchPlacementAffix::new, IntAffixConfig.class);
        register(reg, "omnitool", OmniToolAffix::new, NoValueAffixConfig.class);
        register(reg, "radius_mining", RadiusMiningAffix::new, IntAffixConfig.class);

        //armor
        register(reg, "max_health", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(LootCategory::isDefensive)
                    .build();
        }, AttributeAffixConfig.class);
        register(reg, "armor", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(LootCategory::isDefensive)
                    .setPrefix(true)
                    .build();
        }, AttributeAffixConfig.class);
        register(reg, "armor_toughness", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(LootCategory::isDefensive)
                    .setPrefix(true)
                    .build();
        }, AttributeAffixConfig.class);

        //shield
        register(reg, "shield_speed", (cfg) -> {
            return AttributeAffix.builder(cfg)
                    .types(t-> t == LootCategory.SHIELD)
                    .setPrefix(true)
                    .build();
        }, AttributeAffixConfig.class);
        register(reg, "disengage", DisengageAffix::new, NoValueAffixConfig.class);
        register(reg, "spiked_shield", SpikedAffix::new, FloatAffixConfig.class);
        register(reg, "eldritch_block", EldritchBlockAffix::new, NoValueAffixConfig.class);

        //pc3k: hated that one personally, not willing to implement xD
//                register(reg, ArrowCatcherAffix::new, "arrow_catcher", 1);

//              register(reg, ShieldDamageAffix::new, "shield_damage", 3);
            //Formatter::on
    }

    static Optional<AffixConfig> getConfigOverride(String name) {
        return Optional.empty();
    }

    static <T extends AffixConfig> void register(IForgeRegistry<Affix> registry, String name, Function<T, Affix> factory, Class<T> clazz) {
        AffixConfig config = getConfigOverride(name).orElse(DEFAULT_CONFIGS.get(name));

        if (config == null) {
            DeadlyModule.LOGGER.error("No override and no default config found for [{}] affix! Skipping registration", name);
            return;
        }

        try {
            T typeConfig = clazz.cast(config);
            if(typeConfig.getWeight().asInt() > 0)
                registry.register(factory.apply(typeConfig).setRegistryName(name));
            else
                DeadlyModule.LOGGER.info("Weight for [{}] affix is <= 0, skipping registration", name);
        } catch (ClassCastException ex) {
            DeadlyModule.LOGGER.error(
                    "Invalid config registered for affix [{}]. Expected [{}], but got [{}]",
                    name, clazz.getName(), config.getClass().getName()
            );
        }
    }
}