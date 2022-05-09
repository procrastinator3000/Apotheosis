package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import shadows.apotheosis.deadly.affix.FloatAffixConfig;
import shadows.apotheosis.deadly.loot.LootCategory;

import java.util.function.Consumer;

/**
 * Targets below a certain percent HP threshold are instantly killed.
 */
public class ExecuteAffix extends FloatAffix {

	private static final DamageSource EXECUTION = new DamageSource("apoth.execute").bypassInvul().bypassMagic();

	public ExecuteAffix(FloatAffixConfig config) {
		super(config);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory == LootCategory.HEAVY_WEAPON;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, Tag tag) {
		if (target instanceof LivingEntity livingTarget) {
			if (livingTarget.getHealth() / livingTarget.getMaxHealth() < getFloatOrDefault(tag, 0F)) {
				livingTarget.hurt(EXECUTION, Float.MAX_VALUE);
			}
		}
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public void addInformation(Tag tag, Consumer<Component> list) {
		super.addInformation(tag, list, true);
	}

	@Override
	public Component getDisplayName(Tag tag) {
		return super.getDisplayName(tag, true);
	}
}