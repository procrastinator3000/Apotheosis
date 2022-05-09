package shadows.apotheosis.deadly.affix;

import java.util.*;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.ItemUseEvent;
import shadows.apotheosis.deadly.affix.impl.FloatAffix;
import shadows.apotheosis.deadly.affix.impl.LootPinataAffix;
import shadows.apotheosis.deadly.affix.impl.TeleportDropsAffix;
import shadows.apotheosis.deadly.affix.impl.RadiusMiningAffix;
import shadows.apotheosis.deadly.objects.AffixTomeItem;

public class AffixEvents {

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof AbstractArrow arrow && !e.getEntity().getPersistentData().getBoolean("apoth.generated")) {
			Entity shooter = arrow.getOwner();
			if (shooter instanceof LivingEntity living) {
				ItemStack bow = living.getMainHandItem();
				Map<Affix, Tag> bowAffixes = AffixHelper.getAffixes(bow);
				bowAffixes.forEach((afx, tag) -> afx.onArrowFired(living, arrow, bow, tag));
				AffixHelper.setAffixes(arrow, bowAffixes);
			}
		}
	}

	@SubscribeEvent
	public void impact(ProjectileImpactEvent e) {
		if(e.getProjectile() instanceof AbstractArrow arrow) {
			Map<Affix, Tag> arrowAffixes = AffixHelper.getAffixes(arrow);
			HitResult hitResult = e.getRayTraceResult();
			arrowAffixes.forEach((affix, tag) -> affix.onArrowImpact(arrow, hitResult, hitResult.getType(), tag));
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDamage(LivingHurtEvent e) {
		if (e.getSource() instanceof IndirectEntityDamageSource src) {
			if ("arrow".equals(src.msgId)) {
				if(src.getDirectEntity() instanceof AbstractArrow arrow) {
					boolean isMagicArrow = AffixHelper.getAffixes(arrow).containsKey(Apoth.Affixes.MAGIC_ARROW);
					if (isMagicArrow) {
						e.setCanceled(true);
						DamageSource nSrc = new IndirectEntityDamageSource("apoth.magic_arrow", src.getDirectEntity(), src.getEntity()).bypassArmor().setMagic().setProjectile();
						e.getEntityLiving().invulnerableTime = 0;
						e.getEntityLiving().hurt(nSrc, e.getAmount());
					}
				}
			}
		}
		if (e.getSource().getEntity() instanceof LivingEntity attacker) {
			Map<Affix, Tag> affixes = AffixHelper.getAffixes(attacker.getMainHandItem());
			if (affixes.containsKey(Apoth.Affixes.PIERCING)) {
				e.getSource().bypassArmor();
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void afterDamage(LivingHurtEvent e) {
		if (e.getSource() instanceof EntityDamageSource src && src.getEntity() instanceof Player player) {
			if(!src.isMagic()) {
				float dmgDealt = Math.min(e.getAmount(), e.getEntityLiving().getHealth());

				float lifeSteal = (float)player.getAttributeValue(Apoth.Attributes.LIFE_STEAL);
				player.heal(dmgDealt * lifeSteal);

				if(player.getAbsorptionAmount() < 20) {
					float value = (float)player.getAttributeValue(Apoth.Attributes.OVERHEAL);
					player.setAbsorptionAmount(Math.min(20, player.getAbsorptionAmount() + dmgDealt * value));
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void drops(LivingDropsEvent e) {
		if (e.getSource() instanceof IndirectEntityDamageSource src &&
				src.getDirectEntity() instanceof AbstractArrow arrow &&
				src.getEntity() != null) {

			Map<Affix, Tag> arrowAffixes = AffixHelper.getAffixes(arrow);
			if(arrowAffixes.containsKey(Apoth.Affixes.TELEPORT_DROPS)) {
				Entity tSrc = src.getEntity();
				int canTeleport = TeleportDropsAffix.getIntOrDefault(arrowAffixes.get(Apoth.Affixes.TELEPORT_DROPS), 0);
				e.getDrops().stream().limit(canTeleport).forEach(item ->
						item.setPos(tSrc.getX(), tSrc.getY(), tSrc.getZ())
				);
			}
		}
		LivingEntity dead = e.getEntityLiving();
		if (e.getSource().getEntity() instanceof Player player &&
				!e.getDrops().isEmpty() &&
				dead.canChangeDimensions() &&
				!(dead instanceof Player)) {
			Tag tag = AffixHelper.getAffixes(player.getMainHandItem()).get(Apoth.Affixes.LOOT_PINATA);
			if(tag == null)
				return;
			if (player.level.random.nextFloat() < LootPinataAffix.getFloatOrDefault(tag, 0F)) {
				player.level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (player.level.random.nextFloat() - player.level.random.nextFloat()) * 0.2F) * 0.7F);
				((ServerLevel) player.level).sendParticles(ParticleTypes.EXPLOSION_EMITTER, dead.getX(), dead.getY(), dead.getZ(), 2, 1.0D, 0.0D, 0.0D, 0);
				List<ItemEntity> drops = new ArrayList<>(e.getDrops());
				for (int i = 0; i < 20; i++) {
					for (ItemEntity item : drops) {
						e.getDrops().add(new ItemEntity(player.level, item.getX(), item.getY(), item.getZ(), item.getItem().copy()));
					}
				}
				for (ItemEntity item : e.getDrops()) {
					if (!item.getItem().getItem().canBeDepleted()) {
						item.setPos(dead.getX(), dead.getY(), dead.getZ());
						item.setDeltaMovement(-0.3 + dead.level.random.nextDouble() * 0.6, 0.3 + dead.level.random.nextDouble() * 0.3, -0.3 + dead.level.random.nextDouble() * 0.6);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void update(LivingEntityUseItemEvent.Tick e) {
		if (e.getEntity() instanceof Player) {
			ItemStack stack = e.getItem();
			Map<Affix, Tag> affixes = AffixHelper.getAffixes(stack);
			if (affixes.containsKey(Apoth.Affixes.DRAW_SPEED)) {
				float f = FloatAffix.getFloatOrDefault(affixes.get(Apoth.Affixes.DRAW_SPEED), 0F);
				while (f > 0) {
					if (e.getEntity().tickCount % (int) Math.floor(1 / Math.min(1, f)) == 0) e.setDuration(e.getDuration() - 1);
					f--;
				}
			}
		}
	}

	@SubscribeEvent
	public void checkForCriticalStrike(CriticalHitEvent e) {
		float multiplier = (float)e.getPlayer().getAttributeValue(Apoth.Attributes.CRIT_DAMAGE);

		if(e.isVanillaCritical()){
			e.setDamageModifier(e.getDamageModifier() * multiplier);
			return;
		}

		var forceCrit = AffixHelper.getAffixes(e.getPlayer().getMainHandItem()).containsKey(Apoth.Affixes.MAX_CRIT);
		if (forceCrit || e.getPlayer().level.random.nextFloat() < (e.getPlayer().getAttributeValue(Apoth.Attributes.CRIT_CHANCE))) {
			e.setDamageModifier(e.getDamageModifier() * multiplier);
			e.setResult(Result.ALLOW);
		}
	}

	@SubscribeEvent
	public void onItemUse(ItemUseEvent e) {
		ItemStack s = e.getItemStack();
		if (!s.isEmpty()) {
			Map<Affix, Tag> affixes = AffixHelper.getAffixes(s);
			for (Map.Entry<Affix, Tag> ent : affixes.entrySet()) {
				InteractionResult interactionResult = ent.getKey().onItemUse(e.getContext(), ent.getValue());
				if (interactionResult != null) {
					e.setCanceled(true);
					e.setCancellationResult(interactionResult);
				}
			}
		}
	}

	@SubscribeEvent
	public void harvest(PlayerEvent.HarvestCheck e) {
		ItemStack stack = e.getPlayer().getMainHandItem();
		if (!stack.isEmpty()) {
			Map<Affix, Tag> affixes = AffixHelper.getAffixes(stack);
			if (affixes.containsKey(Apoth.Affixes.OMNITOOL)) {
				BlockState targetBlock = e.getTargetBlock();
				if (Items.DIAMOND_PICKAXE.isCorrectToolForDrops(targetBlock) ||
						Items.DIAMOND_SHOVEL.isCorrectToolForDrops(targetBlock) ||
						Items.DIAMOND_AXE.isCorrectToolForDrops(targetBlock))
					e.setCanHarvest(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void modifyBreakSpeed(PlayerEvent.BreakSpeed e) {
		ItemStack stack = e.getPlayer().getMainHandItem();
		if (!stack.isEmpty()) {
			Map<Affix, Tag> affixes = AffixHelper.getAffixes(stack);
			if (affixes.containsKey(Apoth.Affixes.OMNITOOL)) {
				float shovel = getBaseSpeed(e.getPlayer(), Items.DIAMOND_SHOVEL, e.getState(), e.getPos());
				float axe = getBaseSpeed(e.getPlayer(), Items.DIAMOND_AXE, e.getState(), e.getPos());
				float pickaxe = getBaseSpeed(e.getPlayer(), Items.DIAMOND_PICKAXE, e.getState(), e.getPos());
				e.setNewSpeed(Math.max(shovel, Math.max(axe, Math.max(pickaxe, e.getOriginalSpeed()))));
			}
		}
	}

	static float getBaseSpeed(Player player, Item tool, BlockState state, BlockPos pos) {
		float f = tool.getDestroySpeed(ItemStack.EMPTY, state);
		if (f > 1.0F) {
			int i = EnchantmentHelper.getBlockEfficiency(player);
			ItemStack itemstack = player.getMainHandItem();
			if (i > 0 && !itemstack.isEmpty()) {
				f += i * i + 1;
			}
		}

		if (MobEffectUtil.hasDigSpeed(player)) {
			f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
		}

		if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			float f1;
			switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
			case 0:
				f1 = 0.3F;
				break;
			case 1:
				f1 = 0.09F;
				break;
			case 2:
				f1 = 0.0027F;
				break;
			case 3:
			default:
				f1 = 8.1E-4F;
			}

			f *= f1;
		}

		if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
			f /= 5.0F;
		}

		if (!player.isOnGround()) {
			f /= 5.0F;
		}
		return f;
	}

//
//	@SubscribeEvent(priority = EventPriority.LOW)
//	public void trades(WandererTradesEvent e) {
//		if (DeadlyConfig.affixTrades) for (int i = 0; i < 3; i++)
//			e.getGenericTrades().add(new AffixTrade());
//	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void sortModifiers(ItemAttributeModifierEvent e) {
		if (e.getModifiers() == null || e.getModifiers().isEmpty() || FMLEnvironment.dist == Dist.DEDICATED_SERVER) return;
		Multimap<Attribute, AttributeModifier> map = TreeMultimap.create((k1, k2) -> k1.getRegistryName().compareTo(k2.getRegistryName()), (v1, v2) -> {
			int compOp = Integer.compare(v1.getOperation().ordinal(), v2.getOperation().ordinal());
			int compValue = Double.compare(v2.getAmount(), v1.getAmount());
			return compOp == 0 ? compValue == 0 ? v1.getName().compareTo(v2.getName()) : compValue : compOp;
		});
		for (Map.Entry<Attribute, AttributeModifier> ent : e.getModifiers().entries()) {
			if (ent.getKey() != null && ent.getValue() != null) map.put(ent.getKey(), ent.getValue());
			else DeadlyModule.LOGGER.error("Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}", e.getItemStack(), ent.getKey(), ent.getValue());
		}
		ObfuscationReflectionHelper.setPrivateValue(ItemAttributeModifierEvent.class, e, map, "unmodifiableModifiers");
	}

	@SubscribeEvent
	public void affixModifiers(ItemAttributeModifierEvent e) {
		ItemStack stack = e.getItemStack();
		if(stack.getItem() instanceof IAffixSensitiveItem affixSensitiveItem && !affixSensitiveItem.receivesAttributes(stack)) return;
		if (stack.hasTag()) {
			Map<Affix, Tag> affixes = AffixHelper.getAffixes(stack);
			affixes.forEach((afx, lvl) -> afx.addModifiers(stack, lvl, e.getSlotType(), e::addModifier));
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void affixTooltips(ItemTooltipEvent e) {
		ItemStack stack = e.getItemStack();
		if (stack.getItem() instanceof IAffixSensitiveItem && !((IAffixSensitiveItem) stack.getItem()).receivesTooltips(stack)) return;
		if (stack.hasTag()) {
			Map<Affix, Tag> affixes = AffixHelper.getAffixes(stack);
			List<Component> components = new ArrayList<>();
			affixes.forEach((afx, lvl) -> afx.addInformation(lvl, components::add));
			e.getToolTip().addAll(1, components);
		}
	}

	@SubscribeEvent
	public void shieldBlock(ShieldBlockEvent e) {
		ItemStack stack = e.getEntityLiving().getUseItem();
		if (stack.getItem() instanceof ShieldItem && stack.hasTag()) {
			Map<Affix, Tag> affixes = AffixHelper.getAffixes(stack);
			float blocked = e.getBlockedDamage();
			for (Map.Entry<Affix, Tag> ent : affixes.entrySet()) {
				blocked = ent.getKey().onShieldBlock(e.getEntityLiving(), stack, e.getDamageSource(), blocked, ent.getValue());
			}
			if (blocked != e.getOriginalBlockedDamage()) e.setBlockedDamage(blocked);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBreak(BlockEvent.BreakEvent e) {
		Player player = e.getPlayer();
		ItemStack tool = player.getMainHandItem();
		Level world = player.level;
		if (!world.isClientSide && tool.hasTag()) {
			Tag tag = AffixHelper.getAffixes(tool).get(Apoth.Affixes.RADIUS_MINING);
			if (tag != null) {
				float hardness = e.getState().getDestroySpeed(e.getWorld(), e.getPos());
				RadiusMiningAffix.breakExtraBlocks((ServerPlayer) player, e.getPos(), tool, tag, hardness);
			}
		}
	}

	@SubscribeEvent
	public void anvilEvent(AnvilUpdateEvent e) { AffixTomeItem.updateAnvil(e); }
}