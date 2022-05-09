package shadows.apotheosis.deadly.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.Weight;
import shadows.apotheosis.deadly.loot.LootRarity;

public class NoValueAffixConfig extends AffixConfig {
    public static final Codec<NoValueAffixConfig> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                LootRarity.CODEC.optionalFieldOf("minRarity", LootRarity.COMMON).forGetter(AffixConfig::getMinRarity),
                Weight.CODEC.fieldOf("weight").forGetter(NoValueAffixConfig::getWeight)
        ).apply(instance, NoValueAffixConfig::new);
    });

    public NoValueAffixConfig(LootRarity rarity, Weight weight) {
        super(rarity, weight);
    }

    @Override
    public String toString() {
        return "NoValueAffixConfig *(" + this.getMinRarity() + "):" + this.getWeight();
    }
}
