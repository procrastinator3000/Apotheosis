package shadows.apotheosis.util;

import com.google.gson.annotations.Expose;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import org.jetbrains.annotations.NotNull;

public class Weighted implements WeightedEntry {

	@Expose(deserialize = false)
	private final Weight _weight;

	protected final int weight;

	public Weighted(int pWeight) {
		this._weight = Weight.of(pWeight);
		this.weight = pWeight;
	}

	public Weighted(Weight pWeight) {
		this._weight = pWeight;
		this.weight = pWeight.asInt();
	}

	@Override
	public @NotNull Weight getWeight() {
		return this._weight;
	}
}
