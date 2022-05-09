package shadows.apotheosis.deadly.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.random.Weight;
import net.minecraft.util.valueproviders.IntProvider;
import shadows.apotheosis.deadly.loot.LootRarity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IntAffixConfig extends AffixConfig {
    public static final Codec<IntAffixConfig> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                LootRarity.CODEC.optionalFieldOf("minRarity", LootRarity.COMMON).forGetter(IntAffixConfig::getMinRarity),
                Weight.CODEC.fieldOf("weight").forGetter(IntAffixConfig::getWeight),
                IntProvider.CODEC.fieldOf("value").forGetter(IntAffixConfig::getValueProvider),
                MergeHandler.CODEC.fieldOf("mergeHandler").forGetter(IntAffixConfig::getMergeHandler)
        ).apply(instance, IntAffixConfig::new);
    });

    private final IntProvider valueProvider;
    private final MergeHandler mergeHandler;

    public IntAffixConfig(LootRarity rarity, Weight weight, IntProvider valueProvider, MergeHandler mergeHandler) {
        super(rarity, weight);
        this.valueProvider = valueProvider;
        this.mergeHandler = mergeHandler;
    }

    public IntProvider getValueProvider() {
        return valueProvider;
    }

    public MergeHandler getMergeHandler() {
        return mergeHandler;
    }

    @Override
    public String toString() {
        return "IntAffixConfig *(" + this.getMinRarity() + ", " + this.getValueProvider() + "):" + this.getWeight();
    }

    public record MergeFunction(String name, IntValueMergeFunction operation) implements IntValueMergeFunction {
        public static final Codec<MergeFunction> CODEC = Codec.STRING.xmap(MergeFunction::byNameOrDefault, MergeFunction::getName);
        private static final Map<String, MergeFunction> FUNCTIONS = new HashMap<>();

        public static final MergeFunction ADD = register("add", MergeFunction::mergeLevelsAdd);
        public static final MergeFunction ADD_HALF = register("add_half", MergeFunction::mergeLevelsAddHalf);
        public static final MergeFunction KEEP_HIGHER = register("keep_higher", MergeFunction::mergeLevelsKeepHigher);
        public static final MergeFunction INC_IF_EQUAL_OR_KEEP_HIGHER = register("inc_if_equal_or_keep_higher", MergeFunction::mergeLevelsIncIfEqualOrKeepHigher);

        public String getName() {
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

        public MergeFunction getMergeFunction() {
            return this.mergeFunction;
        }

        public static MergeHandler of(int min, int max, MergeFunction func) {
            return new MergeHandler(min, max, func);
        }
    }

    @FunctionalInterface
    public interface IntValueMergeFunction {
        int apply(int min, int max, int val1, int val2);
    }
}
