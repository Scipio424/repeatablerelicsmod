package repeatablerelicsmod.relics;

import basemod.abstracts.CustomRelic;
import repeatablerelicsmod.util.GeneralUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.RelicStrings;

public abstract class BaseRelic extends CustomRelic {
    public AbstractCard.CardColor pool = null;
    protected String imageName;

    // Constructor for character-specific relics
    public BaseRelic(String id, String imageName, AbstractCard.CardColor pool, RelicTier tier, LandingSound sfx) {
        this(id, imageName, tier, sfx);
        setPool(pool);
    }

    public BaseRelic(String id, RelicTier tier, LandingSound sfx) {
        this(id, GeneralUtils.removePrefix(id), tier, sfx);
    }

    // Constructor to use a base game relic image
    public BaseRelic(String id, String imageName, RelicTier tier, LandingSound sfx) {
        super(testStrings(id), "", tier, sfx); // Removed imagePath reference
        this.imageName = imageName;
        // Commented out texture-related logic
        // loadTexture();
    }

    // Commented out texture loading logic
    // protected void loadTexture() {
    //     this.img = TextureLoader.getTextureNull(relicPath(imageName + ".png"), true);
    //     if (img != null) {
    //         outlineImg = TextureLoader.getTextureNull(relicPath(imageName + "Outline.png"), true);
    //         if (outlineImg == null)
    //             outlineImg = img;
    //     }
    // }

    private void setPool(AbstractCard.CardColor pool) {
        switch (pool) {
            case RED:
                break;
            case GREEN:
                break;
            case BLUE:
                break;
            case PURPLE:
                break;
            default:
                this.pool = pool;
                break;
        }
    }

    private static String testStrings(String ID) {
        RelicStrings text = CardCrawlGame.languagePack.getRelicStrings(ID);
        if (text == null) {
            throw new RuntimeException("The \"" + ID + "\" relic does not have associated text. Make sure " +
                    "there's no issue with the RelicStrings.json file, and that the ID in the json file matches the " +
                    "relic's ID. It should look like \"${modID}:" + GeneralUtils.removePrefix(ID) + "\".");
        }
        return ID;
    }
}
