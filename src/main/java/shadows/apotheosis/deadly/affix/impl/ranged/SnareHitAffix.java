package shadows.apotheosis.deadly.affix.impl.ranged;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import shadows.apotheosis.deadly.affix.AffixConfig;
import shadows.apotheosis.deadly.affix.impl.OneFloatAffix;
import shadows.apotheosis.deadly.affix.impl.OneIntAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Targets hit with an arrow are snared (by application of slowness 11)
 */
public class SnareHitAffix extends OneIntAffix {

	public SnareHitAffix(AffixConfig.IntValueConfig config) {
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
	public void onArrowImpact(AbstractArrow arrow, HitResult res, HitResult.Type type, float level) {
		if (type == HitResult.Type.ENTITY) {
			Entity hitEntity = ((EntityHitResult) res).getEntity();
			if (hitEntity instanceof LivingEntity livingEntity) {
				livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * (int) level, 10));
			}
		}
	}
}