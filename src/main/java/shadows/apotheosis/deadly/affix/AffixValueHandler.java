package shadows.apotheosis.deadly.affix;

import net.minecraft.util.Mth;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public abstract class AffixValueHandler<N extends Number> {

    //region FIELDS
    protected final N generatorMin;
    protected final N generatorMax;

    protected final N mergeMin;
    protected final N mergeMax;
    //endregion

    protected AffixValueHandler(N genMin, N genMax, N mergeMin, N mergeMax){
        //TODO: validation?
        this.generatorMin = genMin;
        this.generatorMax = genMax;
        this.mergeMin = mergeMin;
        this.mergeMax = mergeMax;
    }

    public abstract N generateLevel(Random random, @Nullable AffixModifier modifier);
    public abstract N mergeLevel(N value1, N value2);

    //#region -------------------------- GETTERS --------------------------
    public N getGeneratorMin() {
        return this.generatorMin;
    }

    public N getGeneratorMax() {
        return this.generatorMax;
    }

    public N getMergeMin() {
        return this.mergeMin;
    }

    public N getMergeMax() {
        return this.mergeMax;
    }
    //#endregion

//    public static class IntValue extends AffixValueHandler<Integer>{
//
//        protected final IntValueMergeFunction mergeStrategy;
//
//        IntValue(int genMin, int genMax, int mergeMin, int mergeMax, IntValueMergeFunction mergeStrategy) {
//            super(genMin, genMax, mergeMin, mergeMax);
//            this.mergeStrategy = mergeStrategy;
//        }
//
//        public static IntValue create(int min, int max){
//            return create(min, max, min, max);
//        }
//
//        public static IntValue create(int genMin, int genMax, int mergeMin, int mergeMax){
//            return create(genMin, genMax, mergeMin, mergeMax, MergeStrategy.);
//        }
//
//        public static IntValue create(int genMin, int genMax, int mergeMin, int mergeMax, IntValueMergeFunction mergeStrategy){
//            return new IntValue(genMin, genMax, mergeMin, mergeMax, mergeStrategy);
//        }
//
//        @Override
//        public Integer generateLevel(Random random, @Nullable AffixModifier modifier){
//            int level = Mth.randomBetweenInclusive(random, this.generatorMin, this.generatorMax);
//            if(modifier!=null)
//                level = modifier.editLevel(level, this.generatorMin, this.generatorMax);
//            return level;
//        }
//
//        @Override
//        public Integer mergeLevel(Integer value1, Integer value2){
//            return mergeStrategy.apply(this.mergeMin, this.mergeMax, value1, value2);
//        }
//
//    }

}

