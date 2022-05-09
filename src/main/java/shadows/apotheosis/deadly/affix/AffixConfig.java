package shadows.apotheosis.deadly.affix;

import net.minecraft.util.random.Weight;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.util.Weighted;

public abstract class AffixConfig extends Weighted {

    private final LootRarity minRarity;

    public AffixConfig(LootRarity minRarity, Weight pWeight) {
        super(pWeight);
        this.minRarity = minRarity;
    }

    public LootRarity getMinRarity() {
        return minRarity;
    }
}