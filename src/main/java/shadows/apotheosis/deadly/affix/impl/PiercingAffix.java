package shadows.apotheosis.deadly.affix.impl;

import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.NoValueAffixConfig;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Baseline affix for all heavy weapons.  Damage is converted into armor piercing damage.
 */
public class PiercingAffix extends Affix {

	public PiercingAffix(NoValueAffixConfig config) {
		super(config);
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public boolean canApply(LootCategory type) {
		return type == LootCategory.HEAVY_WEAPON;
	}

}