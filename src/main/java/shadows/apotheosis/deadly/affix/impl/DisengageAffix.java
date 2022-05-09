package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixConfig;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

public class DisengageAffix extends Affix {
	public DisengageAffix(AffixConfig config) {
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
		Entity tSource = source.getEntity();
		if (tSource != null && tSource.distanceToSqr(entity) <= 9) {
			var look = entity.getLookAngle();
			entity.setDeltaMovement(new Vec3(1 * -look.x, 0.25, 1 * -look.z));
			entity.hurtMarked = true;
			entity.setOnGround(false);
		}
		return amount;
	}

}
