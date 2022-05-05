package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.FloatProvider;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixConfig.FloatValueConfig;
import shadows.apotheosis.deadly.affix.AffixConfig.FloatValueConfig.MergeHandler;
import shadows.apotheosis.deadly.affix.OneValueAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public abstract class OneFloatAffix extends OneValueAffix<OneFloatAffix.InstanceData, FloatValueConfig> {

	protected final MergeHandler mergeHandler;
	protected final FloatProvider valueProvider;

	public OneFloatAffix(FloatValueConfig config) {
		super(config);
		this.valueProvider = config.getValueProvider();
		this.mergeHandler = config.getMergeHandler();
	}

	@Override
	public InstanceData generate(Random random, @Nullable AffixModifier modifier) {
		float lvl = valueProvider.sample(random);
		if (modifier != null)
			lvl = modifier.editLevel(lvl, valueProvider.getMinValue(), valueProvider.getMaxValue());
		return new InstanceData(this.getRegistryName(), Mth.clamp(lvl, valueProvider.getMinValue(), valueProvider.getMaxValue()));
	}

	@Override
	public Optional<InstanceData> mergeValues(InstanceData instance1, InstanceData instance2) {
		if(!instance1.getKey().equals(instance2.getKey()))
			return Optional.empty();
		return Optional.of(new InstanceData(this.getRegistryName(), mergeHandler.mergeValues(instance1.getValue(), instance2.getValue())));
	}

	@Override
	public CompoundTag getAsTag() {
		return super.getAsTag();
	}

	public static class InstanceData extends Affix.AffixInstanceData {
		private final float value;

		public InstanceData(ResourceLocation key, float value) {
			super(key);
			this.value = value;
		}

		public float getValue() {
			return value;
		}
	}
}