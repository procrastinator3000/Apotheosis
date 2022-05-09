package shadows.apotheosis.deadly.affix.impl;

import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixConfig;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Allows this tool to mine anything that a diamond shovel/axe/pickaxe could.
 */
public class OmniToolAffix extends Affix {

	public OmniToolAffix(AffixConfig config) {
		super(config);
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public boolean canApply(LootCategory lootCategory) { return lootCategory == LootCategory.BREAKER; }

}