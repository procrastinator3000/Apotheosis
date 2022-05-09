package shadows.apotheosis.deadly.affix.impl;

import java.util.function.Consumer;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import shadows.apotheosis.deadly.affix.FloatAffixConfig;
import shadows.apotheosis.deadly.affix.impl.FloatAffix;
import shadows.apotheosis.deadly.loot.LootCategory;

/**
 * Slain monsters have a chance to explode into a loot pinata.
 */
public class LootPinataAffix extends FloatAffix {

	public LootPinataAffix(FloatAffixConfig config) {
		super(config);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory == LootCategory.SWORD;
	}

	@Override
	public boolean isPrefix() {
		return false;
	}

	@Override
	public void addInformation(Tag tag, Consumer<Component> list) {
		addInformation(tag, list, true);
	}

	@Override
	public Component getDisplayName(Tag tag) {
		return getDisplayName(tag, true);
	}
}