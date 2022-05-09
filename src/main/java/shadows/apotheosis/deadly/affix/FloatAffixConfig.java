package shadows.apotheosis.deadly.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.random.Weight;
import net.minecraft.util.valueproviders.FloatProvider;
import shadows.apotheosis.deadly.loot.LootRarity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FloatAffixConfig extends AffixConfig {
    public static final Codec<FloatAffixConfig> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                LootRarity.CODEC.optionalFieldOf("minRarity", LootRarity.COMMON).forGetter(FloatAffixConfig::getMinRarity),
                Weight.CODEC.fieldOf("weight").forGetter((FloatAffixConfig::getWeight)),
                FloatProvider.CODEC.fieldOf("value").forGetter(FloatAffixConfig::getValueProvider),
                MergeHandler.CODEC.fieldOf("mergeHandler").forGetter(FloatAffixConfig::getMergeHandler)
        ).apply(instance, FloatAffixConfig::new);
    });

    private final FloatProvider valueProvider;
    private final MergeHandler mergeHandler;

    public FloatAffixConfig(LootRarity rarity, Weight weight, FloatProvider valueProvider, MergeHandler mergeHandler) {
        super(rarity, weight);
        this.valueProvider = valueProvider;
        this.mergeHandler = mergeHandler;
    }

    public FloatProvider getValueProvider() {
        return valueProvider;
    }

    public MergeHandler getMergeHandler() {
        return mergeHandler;
    }

    @Override
    public String toString() {
        return "FloatAffixConfig *(" + this.getMinRarity() + ", " + this.getValueProvider() + "):" + this.getWeight();
    }

    public record MergeFunction(String name, FloatValueMergeFunction operation) {
        public static final Codec<MergeFunction> CODEC = Codec.STRING.xmap(MergeFunction::byNameOrDefault, MergeFunction::getName);

        private static final Map<String, MergeFunction> FUNCTIONS = new HashMap<>();

        public static final MergeFunction ADD = register("add", MergeFunction::mergeLevelsAdd);
        public static final MergeFunction ADD_HALF = register("add_half", MergeFunction::mergeLevelsAddHalf);
        public static final MergeFunction KEEP_HIGHER = register("keep_higher", MergeFunction::mergeLevelsKeepHigher);
//        public static final MergeFunction INC_IF_EQUAL_OR_KEEP_HIGHER = register("inc_if_equal_or_keep_higher", MergeFunction::mergeLevelsIncIfEqualOrKeepHigher);

        public String getName() {
            return name;
        }

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
//
//        private static float mergeLevelsIncIfEqualOrKeepHigher(float min, float max, float value1, float value2) {
//            float level = value1 == value2 ? value1 + 1 : Math.max(value1, value2);
//            return Mth.clamp(level, min, max);
//        }
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

        public MergeFunction getMergeFunction() {
            return this.mergeFunction;
        }
    }

    @FunctionalInterface
    public interface FloatValueMergeFunction{
        float apply(float min, float max, float val1, float val2);
    }
}

