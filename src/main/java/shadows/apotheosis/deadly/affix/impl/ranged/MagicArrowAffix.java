package shadows.apotheosis.deadly.affix.impl.ranged;

import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixConfig;
import shadows.apotheosis.deadly.affix.AffixConfig.NoneValueConfig;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Arrow damage is converted into magic damage.
 */
public class MagicArrowAffix extends Affix {

	@Override
	public boolean isPrefix() {
		return false;
	}

	public MagicArrowAffix(NoneValueConfig config) {
		super(config);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory.isRanged();
	}

}