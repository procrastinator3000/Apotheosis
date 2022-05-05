package shadows.apotheosis.deadly.affix.modifiers;

import net.minecraft.util.Mth;
import net.minecraft.util.random.Weight;
import org.jetbrains.annotations.NotNull;
import shadows.apotheosis.util.Weighted;

public class AffixModifier extends Weighted {
    public static final AffixModifier HALF = new AffixModifier("half", AffixOp.MULTIPLY, 0.5F, 1);

//#region ------------------ FIELDS ----------------------
    /**
     * The language key for this modifier.
     */
    protected final String key;

    /**
     * What this modifier does to the underlying affix.
     */
    protected final AffixOp op;

    /**
     * The value of this modifier.
     */
    protected final float value;

    protected boolean editName = true;
//#endregion

    AffixModifier(String key, AffixOp op, float value, int weight) {
        super(weight);
        this.key = key;
        this.op = op;
        this.value = value;
    }

    /**
     * Adjusts the passed level, according to the operation of this modifier.
     */
    public float editLevel(float level, float min, float max) {
        float newLevel = op == AffixOp.ADD ? level + value : op == AffixOp.MULTIPLY ? level * value : value;
        return Mth.clamp(newLevel, min, max);
    }

    public int editLevel(int level, int min, int max) {
        float newLevel = op == AffixOp.ADD ? level + value : op == AffixOp.MULTIPLY ? level * value : value;
        return Mth.clamp(Math.round(newLevel), min, max);
    }

    /**
     * Get the translation key of this modifier.
     */
    public String getKey() {
        return key;
    }

    /**
     * If this modifier should edit the name of the affix.
     */
    public boolean editName() {
        return editName;
    }

    @Override
    public @NotNull Weight getWeight() {
        return null;
    }

    public static enum AffixOp {
        ADD,
        MULTIPLY,
        SET;
    }

}