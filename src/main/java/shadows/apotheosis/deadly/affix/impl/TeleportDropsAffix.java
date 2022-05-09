package shadows.apotheosis.deadly.affix.impl;

import shadows.apotheosis.deadly.affix.IntAffixConfig;
import shadows.apotheosis.deadly.affix.impl.FloatAffix;
import shadows.apotheosis.deadly.affix.impl.IntAffix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Drops from killed enemies are teleported to the shooter.
 */
public class TeleportDropsAffix extends IntAffix {

	public TeleportDropsAffix(IntAffixConfig config) {
		super(config);
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory.isRanged();
	}

}