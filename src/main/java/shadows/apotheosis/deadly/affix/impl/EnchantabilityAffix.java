package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.util.random.Weight;
import net.minecraft.util.valueproviders.UniformInt;
import shadows.apotheosis.deadly.affix.IntAffixConfig;
import shadows.apotheosis.deadly.affix.impl.IntAffix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

public class EnchantabilityAffix extends IntAffix {
	private static final IntAffixConfig DEFAULT_CONFIG =  new IntAffixConfig(
			LootRarity.COMMON,
			Weight.of(5),
			UniformInt.of(9, 25),
			IntAffixConfig.MergeHandler.of(2, 30, IntAffixConfig.MergeFunction.ADD_HALF));

	public EnchantabilityAffix(IntAffixConfig config) {
		super(config);
	}

	public EnchantabilityAffix() {
		this(DEFAULT_CONFIG);
	}

	@Override
	public boolean isPrefix() {
		return false;
	}

	@Override
	public boolean canApply(LootCategory category) {
		return true;
	}

}