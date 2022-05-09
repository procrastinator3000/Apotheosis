package shadows.apotheosis.deadly.affix.impl;

import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.NoValueAffixConfig;
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

	public MagicArrowAffix(NoValueAffixConfig config) {
		super(config);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory.isRanged();
	}

}