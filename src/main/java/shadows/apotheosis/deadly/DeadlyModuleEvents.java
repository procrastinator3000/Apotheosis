package shadows.apotheosis.deadly;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.deadly.commands.*;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.LootController;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.deadly.reload.AffixLootManager;
import shadows.apotheosis.deadly.reload.BossItemManager;

public class DeadlyModuleEvents {

	@SubscribeEvent
	public void reloads(AddReloadListenerEvent e) {
	}

	@SubscribeEvent
	public void cmds(RegisterCommandsEvent e) {
		RarityCommand.register(e.getDispatcher());
		CategoryCheckCommand.register(e.getDispatcher());
		LootifyCommand.register(e.getDispatcher());
		ModifierCommand.register(e.getDispatcher());
		ApothBossSpawnCommand.register(e.getDispatcher());
	}

	private static final Set<Float> values = ImmutableSet.of(0.1F, 0.2F, 0.25F, 0.33F, 0.5F, 1.0F, 1.1F, 1.2F, 1.25F, 1.33F, 1.5F, 2.0F, 2.1F, 2.25F, 2.33F, 2.5F, 3F);

	/**
	 * This event handler makes the Draw Speed attribute work as intended.
	 * Modifiers targetting this attribute should use the MULTIPLY_BASE operation.
	 */
	@SubscribeEvent
	public void drawSpeed(LivingEntityUseItemEvent.Tick e) {
		if (e.getEntity() instanceof Player player) {
			double t = player.getAttribute(Apoth.Attributes.DRAW_SPEED).getValue() - 1;
			if (t == 0 || !LootCategory.forItem(e.getItem()).isRanged()) return;
			float clamped = values.stream().filter(f -> f >= t).min(Float::compareTo).orElse(3F);
			while (clamped > 0) {
				if (e.getEntity().tickCount % (int) Math.floor(1 / Math.min(1, t)) == 0) e.setDuration(e.getDuration() - 1);
				clamped--;
			}
		}
	}


	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void surfaceBosses(LivingSpawnEvent.CheckSpawn e) {
		if (e.getSpawnReason() == MobSpawnType.NATURAL || e.getSpawnReason() == MobSpawnType.CHUNK_GENERATION) {
			LivingEntity entity = e.getEntityLiving();
			Random rand = e.getWorld().getRandom();
			if (!e.getWorld().isClientSide() && entity instanceof Monster && e.getResult() == Event.Result.DEFAULT) {
				if (DeadlyConfig.surfaceBossChance > 0 && rand.nextInt(DeadlyConfig.surfaceBossChance) == 0 && e.getWorld().canSeeSky(new BlockPos(e.getX(), e.getY(), e.getZ()))) {
					Player player = e.getWorld().getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
					if (player == null) return; //Should never be null, but we check anyway since nothing makes sense around here.
					var item = BossItemManager.INSTANCE.getRandomItem(rand);
					if(item.isEmpty())
					{
						DeadlyModule.LOGGER.error("Failed to procure random boss entry! Cannot spawn");
						return;
					}
					Mob boss = item.get().createBoss((ServerLevelAccessor) e.getWorld(), new BlockPos(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), rand);
					if (canSpawn(e.getWorld(), boss, player.distanceToSqr(boss))) {
						e.getWorld().addFreshEntity(boss);
						e.setResult(Event.Result.DENY);
						DeadlyModule.debugLog(boss.blockPosition(), "Surface Boss - " + boss.getName().getString());
						if (DeadlyConfig.surfaceBossLightning) {
							LightningBolt le = EntityType.LIGHTNING_BOLT.create(((ServerLevelAccessor) e.getWorld()).getLevel());
							le.setPos(boss.getX(), boss.getY(), boss.getZ());
							le.setVisualOnly(true);
							e.getWorld().addFreshEntity(le);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void addAffixGear(LivingSpawnEvent.SpecialSpawn e) {
		if (e.getSpawnReason() == MobSpawnType.NATURAL || e.getSpawnReason() == MobSpawnType.CHUNK_GENERATION) {
			LivingEntity entity = e.getEntityLiving();
			Random rand = e.getWorld().getRandom();
			if (!e.getWorld().isClientSide() && entity instanceof Monster) {
				if (entity.getMainHandItem().isEmpty() && DeadlyConfig.randomAffixItem > 0 && rand.nextInt(DeadlyConfig.randomAffixItem) == 0) {
					var entry= AffixLootManager.getRandomEntry(rand);
					if(entry.isPresent())
					{
						LootRarity rarity = LootRarity.random(rand);
						ItemStack loot = LootController.lootifyItem(entry.get().getStack().copy(), rarity, rand);
						EquipmentSlot slot = entry.get().getStack().getEquipmentSlot();
						if(slot == null)
							slot = LivingEntity.getEquipmentSlotForItem(loot);
						loot.getOrCreateTag().putBoolean("apoth_rspawn", true);
						entity.setItemSlot(slot, loot);
						((Mob) entity).setDropChance(slot, 2);
					}
					else
						DeadlyModule.LOGGER.error("Failed to get random affix loot entry, cannot add affix gear to random spawn!");
				}
			}
		}
	}



//#region ------------------ HELPER METHODS ----------------------
	private static boolean canSpawn(LevelAccessor world, Mob entity, double playerDist) {
		if (playerDist > entity.getType().getCategory().getDespawnDistance() * entity.getType().getCategory().getDespawnDistance() && entity.removeWhenFarAway(playerDist)) {
			return false;
		} else {
			return entity.checkSpawnRules(world, MobSpawnType.NATURAL) && entity.checkSpawnObstruction(world);
		}
	}
//#endregion
}
