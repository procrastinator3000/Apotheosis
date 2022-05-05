package shadows.apotheosis.deadly.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.random.Weight;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import shadows.apotheosis.deadly.affix.impl.OneFloatAffix;
import shadows.apotheosis.deadly.affix.impl.OneIntAffix;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.util.Weighted;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AffixConfig extends Weighted {

    private final LootRarity minRarity;

    public AffixConfig(LootRarity minRarity, Weight pWeight) {
        super(pWeight);
        this.minRarity = minRarity;
    }

    public LootRarity getMinRarity() {
        return minRarity;
    }

    public static class OneValueAffixConfig<T extends Affix.AffixInstanceData> extends AffixConfig {

        public OneValueAffixConfig(LootRarity minRarity, Weight pWeight) {
            super(minRarity, pWeight);
        }
    }

    public static class NoneValueConfig extends AffixConfig {
        public static final Codec<NoneValueConfig> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                    LootRarity.CODEC.optionalFieldOf("minRarity", LootRarity.COMMON).forGetter(AffixConfig::getMinRarity),
                    Weight.CODEC.fieldOf("weight").forGetter(NoneValueConfig::getWeight)
            ).apply(instance, NoneValueConfig::new);
        });

        public NoneValueConfig(LootRarity rarity, Weight weight) {
            super(rarity, weight);
        }

        @Override
        public String toString() {
            return "NoneValueConfig *(" + this.getMinRarity() + "):" + this.getWeight();
        }
    }

    public static class IntValueConfig extends OneValueAffixConfig<OneIntAffix.InstanceData> {

        public static final Codec<IntValueConfig> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                    LootRarity.CODEC.optionalFieldOf("minRarity", LootRarity.COMMON).forGetter(IntValueConfig::getMinRarity),
                    Weight.CODEC.fieldOf("weight").forGetter(IntValueConfig::getWeight),
                    IntProvider.CODEC.fieldOf("value").forGetter(IntValueConfig::getValueProvider),
                    MergeHandler.CODEC.fieldOf("mergeHandler").forGetter(IntValueConfig::getMergeHandler)
            ).apply(instance, IntValueConfig::new);
        });

        private final IntProvider valueProvider;
        private final MergeHandler mergeHandler;

        public IntValueConfig(LootRarity rarity, Weight weight, IntProvider valueProvider, MergeHandler mergeHandler) {
           super(rarity, weight);
           this.valueProvider = valueProvider;
           this.mergeHandler = mergeHandler;
        }

        public IntProvider getValueProvider() {
            return valueProvider;
        }

        public MergeHandler getMergeHandler() { return mergeHandler; }
        @Override
        public String toString() {
            return "IntValueConfig *(" + this.getMinRarity() + ", " + this.getValueProvider() + "):" + this.getWeight();
        }


        public record MergeFunction(String name, IntValueMergeFunction operation) implements IntValueMergeFunction {
            public static final Codec<MergeFunction> CODEC = Codec.STRING.xmap(MergeFunction::byNameOrDefault, MergeFunction::getName);
            private static final Map<String, MergeFunction> FUNCTIONS = new HashMap<>();

            public static final MergeFunction ADD = register("add", MergeFunction::mergeLevelsAdd);
            public static final MergeFunction ADD_HALF = register("add_half", MergeFunction::mergeLevelsAddHalf);
            public static final MergeFunction KEEP_HIGHER = register("keep_higher", MergeFunction::mergeLevelsKeepHigher);
            public static final MergeFunction INC_IF_EQUAL_OR_KEEP_HIGHER = register("inc_if_equal_or_keep_higher", MergeFunction::mergeLevelsIncIfEqualOrKeepHigher);

            public String getName(){
                return this.name;
            }

            private static MergeFunction register(String name, IntValueMergeFunction operation) {
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
            static int mergeLevelsAdd(int min, int max, int value1, int value2) {
                return Mth.clamp(value1 + value2, min, max);
            }

            static int mergeLevelsAddHalf(int min, int max, int value1, int value2) {
                int halfLower = Math.round((Math.min(value1, value2) / 2F));
                return Mth.clamp(value1 - halfLower + value2, min, max);
            }

            static int mergeLevelsKeepHigher(int min, int max, int value1, int value2) {
                return Mth.clamp(Math.max(value1, value2), min, max);
            }

            static int mergeLevelsIncIfEqualOrKeepHigher(int min, int max, int value1, int value2) {
                int level = value1 == value2 ? value1 + 1 : Math.max(value1, value2);
                return Mth.clamp(level, min, max);
            }

            @Override
            public int apply(int min, int max, int val1, int val2) {
                return operation().apply(min, max, val1, val2);
            }
            //endregion
        }

        public record MergeHandler(int minValue, int maxValue, MergeFunction mergeFunction) {
            public static final Codec<MergeHandler> CODEC = RecordCodecBuilder.create((instance) -> {
                return instance.group(
                        Codec.INT.fieldOf("minValue").forGetter(MergeHandler::getMinValue),
                        Codec.INT.fieldOf("maxValue").forGetter(MergeHandler::getMaxValue),
                        MergeFunction.CODEC.fieldOf("function").forGetter(MergeHandler::getMergeFunction)
                ).apply(instance, MergeHandler::new);
            });

            public int mergeValues(int v1, int v2) {
                return this.mergeFunction.apply(this.minValue, this.maxValue, v1, v2);
            }

            public int getMinValue() {
                return this.minValue;
            }

            public int getMaxValue() {
                return this.maxValue;
            }

            public MergeFunction getMergeFunction(){
                return this.mergeFunction;
            }
        }
    }

    public static class FloatValueConfig extends OneValueAffixConfig<OneFloatAffix.InstanceData> {

        public static final Codec<FloatValueConfig> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                    LootRarity.CODEC.optionalFieldOf("minRarity", LootRarity.COMMON).forGetter(FloatValueConfig::getMinRarity),
                    Weight.CODEC.fieldOf("weight").forGetter((FloatValueConfig::getWeight)),
                    FloatProvider.CODEC.fieldOf("value").forGetter(FloatValueConfig::getValueProvider),
                    MergeHandler.CODEC.fieldOf("mergeHandler").forGetter(FloatValueConfig::getMergeHandler)
            ).apply(instance, FloatValueConfig::new);
        });

        private final FloatProvider valueProvider;
        private final MergeHandler mergeHandler;

        public FloatValueConfig(LootRarity rarity, Weight weight, FloatProvider valueProvider, MergeHandler mergeHandler) {
            super(rarity, weight);
            this.valueProvider = valueProvider;
            this.mergeHandler = mergeHandler;
        }

        public FloatProvider getValueProvider() {
            return valueProvider;
        }

        public MergeHandler getMergeHandler() { return mergeHandler; }

        @Override
        public String toString() {
            return "FloatValueConfig *(" + this.getMinRarity() + ", " + this.getValueProvider() + "):" + this.getWeight();
        }

        public record MergeFunction(String name, FloatValueMergeFunction operation) implements FloatValueMergeFunction {
            public static final Codec<MergeFunction> CODEC = Codec.STRING.xmap(MergeFunction::byNameOrDefault, MergeFunction::getName);

            private static final Map<String, MergeFunction> FUNCTIONS = new HashMap<>();

            public static final MergeFunction ADD = register("add", MergeFunction::mergeLevelsAdd);
            public static final MergeFunction ADD_HALF = register("add_half", MergeFunction::mergeLevelsAddHalf);
            public static final MergeFunction KEEP_HIGHER = register("keep_higher", MergeFunction::mergeLevelsKeepHigher);
            public static final MergeFunction INC_IF_EQUAL_OR_KEEP_HIGHER = register("inc_if_equal_or_keep_higher", MergeFunction::mergeLevelsIncIfEqualOrKeepHigher);

            public String getName() {
                return name;
            }

            @Override
            public float apply(float min, float max, float val1, float val2) {
                return operation.apply(min, max, val1, val2);
            }

            private static MergeFunction register(String name, FloatValueMergeFunction operation) {
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
            private static float mergeLevelsAdd(float min, float max, float value1, float value2) {
                return Mth.clamp(value1 + value2, min, max);
            }

            private static float mergeLevelsAddHalf(float min, float max, float value1, float value2) {
                float halfLower = (Math.min(value1, value2) / 2F);
                return Mth.clamp(value1 - halfLower + value2, min, max);
            }

            private static float mergeLevelsKeepHigher(float min, float max, float value1, float value2) {
                return Mth.clamp(Math.max(value1, value2), min, max);
            }

            private static float mergeLevelsIncIfEqualOrKeepHigher(float min, float max, float value1, float value2) {
                float level = value1 == value2 ? value1 + 1 : Math.max(value1, value2);
                return Mth.clamp(level, min, max);
            }
            //#endregion
        }

        public record MergeHandler(float minValue, float maxValue, MergeFunction mergeFunction) {
            public static final Codec<MergeHandler> CODEC = RecordCodecBuilder.create((instance) -> {
                return instance.group(
                        Codec.FLOAT.fieldOf("minValue").forGetter(MergeHandler::getMinValue),
                        Codec.FLOAT.fieldOf("maxValue").forGetter(MergeHandler::getMaxValue),
                        MergeFunction.CODEC.fieldOf("function").forGetter(MergeHandler::getMergeFunction)
                ).apply(instance, MergeHandler::new);
            });

            public float mergeValues(float v1, float v2) {
                return this.mergeFunction.apply(this.minValue, this.maxValue, v1, v2);
            }

            public float getMinValue() {
                return this.minValue;
            }

            public float getMaxValue() {
                return this.maxValue;
            }

            public MergeFunction getMergeFunction(){
                return this.mergeFunction;
            }
        }
    }

    @FunctionalInterface
    public interface IntValueMergeFunction {
        int apply(int min, int max, int val1, int val2);
    }

    @FunctionalInterface
    public interface FloatValueMergeFunction{
        float apply(float min, float max, float val1, float val2);
    }
}

