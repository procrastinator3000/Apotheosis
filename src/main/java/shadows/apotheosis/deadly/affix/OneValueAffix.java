package shadows.apotheosis.deadly.affix;

import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public abstract class OneValueAffix<T extends Affix.AffixInstanceData, C extends AffixConfig.OneValueAffixConfig<T>> extends Affix {

    public OneValueAffix(C config) {
        super(config);
    }

    public abstract Optional<T> mergeValues(T instance1, T instance2);
    public abstract T generate(Random random, @Nullable AffixModifier modifier);
}
