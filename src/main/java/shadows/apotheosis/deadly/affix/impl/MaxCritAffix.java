package shadows.apotheosis.deadly.affix.impl;

import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.NoValueAffixConfig;
import shadows.apotheosis.deadly.loot.LootCategory;

public class MaxCritAffix extends Affix {
    public MaxCritAffix(NoValueAffixConfig config) {
        super(config);
    }

    @Override
    public boolean isPrefix() {
        return false;
    }

    @Override
    public boolean canApply(LootCategory type) {
        return false;
    }
}
