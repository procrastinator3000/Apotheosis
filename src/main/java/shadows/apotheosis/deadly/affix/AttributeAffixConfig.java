package shadows.apotheosis.deadly.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.random.Weight;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.deadly.loot.LootRarity;

import java.util.*;
import java.util.function.Function;

public class AttributeAffixConfig extends AffixConfig {
    public static final Codec<AttributeAffixConfig> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                LootRarity.CODEC.optionalFieldOf("minRarity", LootRarity.COMMON).forGetter(AttributeAffixConfig::getMinRarity),
                Weight.CODEC.fieldOf("weight").forGetter((AttributeAffixConfig::getWeight)),
                Codec.STRING.xmap(AttributeAffixConfig::attributeByName, AttributeAffixConfig::getAttributeName).optionalFieldOf("attribute", Attributes.MAX_HEALTH).forGetter(AttributeAffixConfig::getAttribute),
                Codec.FLOAT.fieldOf("step").forGetter(AttributeAffixConfig::getStep),
                IntProvider.CODEC.fieldOf("level").forGetter(AttributeAffixConfig::getLevel),
                Codec.STRING.xmap(AttributeModifier.Operation::valueOf, AttributeModifier.Operation::name).fieldOf("operation").forGetter(AttributeAffixConfig::getOperation),
                MergeHandler.CODEC.fieldOf("mergeHandler").forGetter(AttributeAffixConfig::getMergeHandler)
        ).apply(instance, AttributeAffixConfig::new);
    });

    private final Attribute attribute;
    private final IntProvider level;
    private final float step;
    private final AttributeModifier.Operation operation;
    private final MergeHandler mergeHandler;
    public final Function<Integer, Float> valueMapper;

    private static Attribute attributeByName(String attributeName){
        return ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributeName));
    }

    private static String getAttributeName(Attribute attribute) {
        return attribute.getRegistryName().toString();
    }

    public AttributeAffixConfig(LootRarity rarity, Weight weight, Attribute attribute, float step, IntProvider level, AttributeModifier.Operation operation, MergeHandler mergeHandler) {
        super(rarity, weight);
        this.mergeHandler = mergeHandler;
        this.attribute = attribute;
        this.step = step;
        this.level = level;
        //pc3k: could calculate using fractionFactor in calcs to avoid numeric error
        // but that would mean we could only get step value that multiplies to whole number
        // ie could not get step greater than 1 (ex. 1.5)
//        this.valueMapper = (lvl) -> { return lvl/fractionFactor + (lvl % fractionFactor) * (1F/(float)fractionFactor) * lvl; };
        this.operation = operation;
        this.valueMapper = (lvl) -> {
            return Math.round(lvl * step * 100F)/100F; //float with 2 decimal point precision
        };
    }

    public MergeHandler getMergeHandler() {
        return mergeHandler;
    }

    @Override
    public String toString() {
        return String.format("AttributeAffixConfig *(%s, %s, %f, %s, %s):%d",
                this.getMinRarity().name(),
                getAttributeName(this.attribute),
                this.step,
                this.level.toString(),
                this.mergeHandler.toString(),
                this.weight);
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public IntProvider getLevel() { return level; }

    public float getStep() { return step; }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }

    private record MergeFunction(String name, AttributeValueMergeFunction operation) {
        public static final Codec<MergeFunction> CODEC = Codec.STRING.xmap(MergeFunction::byNameOrDefault, MergeFunction::getName);

        private static final Map<String, MergeFunction> FUNCTIONS = new HashMap<>();

        public static final MergeFunction ADD = register("add", MergeFunction::mergeLevelsAdd);
        public static final MergeFunction ADD_HALF = register("add_half", MergeFunction::mergeLevelsAddHalf);
        public static final MergeFunction KEEP_HIGHER = register("keep_higher", MergeFunction::mergeLevelsKeepHigher);
        public static final MergeFunction INC_IF_EQUAL_OR_KEEP_HIGHER = register("inc_if_equal_or_keep_higher", MergeFunction::mergeLevelsIncIfEqualOrKeepHigher);

        public String getName() {
            return name;
        }

        public int apply(int min, int max, int lvl1, int lvl2){
            return operation.apply(min, max, lvl1, lvl2);
        }

        private static MergeFunction register(String name, AttributeValueMergeFunction operation) {
            return FUNCTIONS.put(name, new MergeFunction(name, operation));
        }

        public static Optional<MergeFunction> byName(String name) {
            if (FUNCTIONS.containsKey(name))
                return Optional.of(FUNCTIONS.get(name));
            return Optional.empty();
        }

        public static MergeFunction byNameOrDefault(String name) {
            return byName(name).orElse(ADD_HALF);
        }

        //#region ------------------------- IMPLEMENTATION -------------------------
        private static int mergeLevelsAdd(int min, int max, int lvl1, int lvl2) {
            return Mth.clamp(lvl1 + lvl2, min, max);
        }

        private static int mergeLevelsAddHalf(int min, int max, int lvl1, int lvl2) {
            int halfLower = Math.round((Math.min(lvl1, lvl2) / 2F));
            return Mth.clamp(lvl1 - halfLower + lvl2, min, max);
        }

        private static int mergeLevelsKeepHigher(int min, int max, int lvl1, int lvl2) {
            return Mth.clamp(Math.max(lvl1, lvl2), min, max);
        }

        private static int mergeLevelsIncIfEqualOrKeepHigher(int min, int max, int lvl1, int lvl2) {
            int level = lvl1 == lvl2 ? lvl1 + 1 : Math.max(lvl1, lvl2);
            return Mth.clamp(level, min, max);
        }
        //#endregion
    }

    public record MergeHandler(int minValue, int maxValue, MergeFunction mergeFunction) {
        public static final Codec<MergeHandler> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                    Codec.INT.fieldOf("minLevel").forGetter(MergeHandler::getMinValue),
                    Codec.INT.fieldOf("maxLevel").forGetter(MergeHandler::getMaxValue),
                    MergeFunction.CODEC.fieldOf("function").forGetter(MergeHandler::getMergeFunction)
            ).apply(instance, MergeHandler::new);
        });

        public int mergeValues(int l1, int l2) {
            return this.mergeFunction.apply(this.minValue, this.maxValue, l1, l2);
        }

        public int getMinValue() {
            return this.minValue;
        }

        public int getMaxValue() {
            return this.maxValue;
        }

        public MergeFunction getMergeFunction() {
            return this.mergeFunction;
        }

    }

    @FunctionalInterface
    public interface AttributeValueMergeFunction {
        int apply(int min, int max, int lvl1, int lvl2);
    }
}
