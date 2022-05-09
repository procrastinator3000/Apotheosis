package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.FloatAffixConfig;
import shadows.apotheosis.deadly.affix.impl.FloatAffix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

public class SpikedAffix extends FloatAffix {

	public SpikedAffix(FloatAffixConfig config) {
		super(config);
	}

	@Override
	public boolean canApply(LootCategory type) { return type == LootCategory.SHIELD; }

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public float onShieldBlock(LivingEntity entity, ItemStack stack, DamageSource source, float amount, Tag tag) {
		Entity tSource = source.getEntity();
		if (tSource != null && tSource.distanceToSqr(entity) <= 9) {
			tSource.hurt(causeSpikeDamage(entity), getFloatOrDefault(tag, 0F) * amount);
		}
		return super.onShieldBlock(entity, stack, source, amount, tag);
	}

	public static DamageSource causeSpikeDamage(Entity source) {
		return new EntityDamageSource("apoth_spiked", source).setThorns().setMagic();
	}
}
