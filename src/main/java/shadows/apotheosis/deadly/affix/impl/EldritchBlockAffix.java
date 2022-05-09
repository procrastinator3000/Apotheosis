package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixConfig;
import shadows.apotheosis.deadly.affix.NoValueAffixConfig;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Applies Weakness/Sundering to the attacker.
 */
public class EldritchBlockAffix extends Affix {

	public EldritchBlockAffix(NoValueAffixConfig config) {
		super(config);
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory == LootCategory.SHIELD;
	}

	@Override
	public float onShieldBlock(LivingEntity entity, ItemStack stack, DamageSource source, float amount, Tag tag) {
		if (source.getEntity() instanceof LivingEntity attacker) {
			attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
			if (Apoth.Effects.SUNDERING != null) attacker.addEffect(new MobEffectInstance(Apoth.Effects.SUNDERING, 200, 1));
		}
		return amount;
	}

}
