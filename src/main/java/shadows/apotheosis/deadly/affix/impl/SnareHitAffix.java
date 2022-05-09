package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import shadows.apotheosis.deadly.affix.AffixConfig;
import shadows.apotheosis.deadly.affix.IntAffixConfig;
import shadows.apotheosis.deadly.affix.impl.IntAffix;
import shadows.apotheosis.deadly.loot.LootCategory;

/**
 * Targets hit with an arrow are snared (by application of slowness 11)
 */
public class SnareHitAffix extends IntAffix {

	public SnareHitAffix(IntAffixConfig config) {
		super(config);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory.isRanged();
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public void onArrowImpact(AbstractArrow arrow, HitResult res, HitResult.Type type, Tag tag) {
		if (type == HitResult.Type.ENTITY) {
			Entity hitEntity = ((EntityHitResult) res).getEntity();
			if (hitEntity instanceof LivingEntity livingEntity) {
				livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * getIntOrDefault(tag, 0), 10));
			}
		}
	}
}