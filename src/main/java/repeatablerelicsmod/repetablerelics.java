package repeatablerelicsmod;

import basemod.BaseMod;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostDungeonInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import repeatablerelicsmod.util.KeywordInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class repetablerelics implements
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        PostInitializeSubscriber,
        PostDungeonInitializeSubscriber,
        PostUpdateSubscriber {

    public static final String MOD_ID = "repeatablerelicsmod";
    public static final Logger logger = LogManager.getLogger(MOD_ID);

    private static final Set<String> seenRelics = new HashSet<>();
    private static boolean relicPoolRefreshed = false;

    public static String makeID(String id) {
        return MOD_ID + ":" + id;
    }

    public static void initialize() {
        logger.info("Initializing repetablerelicsmod..."); // Debug statement to confirm initialization
        new repetablerelics();
    }

    public repetablerelics() {
        logger.info("Subscribing repetablerelicsmod to BaseMod...");
        BaseMod.subscribe(this);
        logger.info(MOD_ID + " successfully subscribed to BaseMod.");
    }

    @Override
    public void receivePostInitialize() {
        logger.info("Post-initialization started...");
        // Add any additional debug information here if needed
        logger.info("Post-initialization complete.");
    }

    @Override
    public void receivePostDungeonInitialize() {
        logger.info("Post-dungeon initialization started...");
        seenRelics.clear();
        relicPoolRefreshed = false;
        logger.info("Cleared seen relics and reset relic pool refresh flag.");
    }

    @Override
    public void receivePostUpdate() {
        logger.debug("Post-update check: relicPoolRefreshed = " + relicPoolRefreshed);
        if (!relicPoolRefreshed && allRelicsSeen()) {
            logger.info("All relics have been seen. Refreshing relic pools...");
            refreshRelicPools();
            relicPoolRefreshed = true;
            logger.info("Relic pools refreshed.");
        }
    }

    @Override
    public void receiveEditStrings() {
        logger.info("Loading localization strings...");
        String relicStringsPath = getLocalizationPath("RelicStrings.json");
        String uiStringsPath = getLocalizationPath("UIStrings.json");

        BaseMod.loadCustomStringsFile(RelicStrings.class, relicStringsPath);
        BaseMod.loadCustomStringsFile(UIStrings.class, uiStringsPath);

        logger.info("Localization files loaded successfully.");
    }

    @Override
    public void receiveEditKeywords() {
        logger.info("Loading keywords...");
        String keywordPath = getLocalizationPath("KeywordStrings.json");
        Gson gson = new Gson();
        FileHandle file = Gdx.files.internal(keywordPath);

        if (!file.exists()) {
            logger.error("Keyword file not found: " + keywordPath);
            return;
        }

        String json = file.readString(String.valueOf(StandardCharsets.UTF_8));
        KeywordInfo[] keywords = gson.fromJson(json, KeywordInfo[].class);

        for (KeywordInfo keyword : keywords) {
            keyword.prep();
            BaseMod.addKeyword(MOD_ID, keyword.NAMES, keyword.DESCRIPTION);
            logger.debug("Loaded keyword: " + keyword.ID);
        }
        logger.info("Keywords loaded successfully.");
    }

    private boolean allRelicsSeen() {
        logger.debug("Checking if all relics have been seen...");
        return allRelicsSeenInPool(AbstractDungeon.commonRelicPool)
                && allRelicsSeenInPool(AbstractDungeon.uncommonRelicPool)
                && allRelicsSeenInPool(AbstractDungeon.rareRelicPool)
                && allRelicsSeenInPool(AbstractDungeon.shopRelicPool)
                && allRelicsSeenInPool(AbstractDungeon.bossRelicPool);
    }

    private boolean allRelicsSeenInPool(List<String> relicPool) {
        if (relicPool == null) {
            logger.warn("Relic pool is null.");
            return true;
        }
        for (String relicID : relicPool) {
            if (!seenRelics.contains(relicID)) {
                logger.debug("Relic not seen: " + relicID);
                return false;
            }
        }
        return true;
    }

    private void refreshRelicPools() {
        logger.info("Refreshing all relic pools...");
        refreshRelicPool(AbstractDungeon.commonRelicPool);
        refreshRelicPool(AbstractDungeon.uncommonRelicPool);
        refreshRelicPool(AbstractDungeon.rareRelicPool);
        refreshRelicPool(AbstractDungeon.shopRelicPool);
        refreshRelicPool(AbstractDungeon.bossRelicPool);
    }

    private void refreshRelicPool(List<String> relicPool) {
        if (relicPool == null) {
            logger.warn("Relic pool is null. Skipping refresh.");
            return;
        }
        relicPool.clear();
        logger.info("Cleared relic pool.");
        for (String relicID : AbstractDungeon.relicsToRemoveOnStart) {
            relicPool.add(relicID);
            logger.debug("Added relic to pool: " + relicID);
        }
    }

    private String getLocalizationPath(String fileName) {
        String lang = Settings.language.name().toLowerCase();
        String path = MOD_ID + "/localization/" + lang + "/" + fileName;
        logger.debug("Localization path: " + path);
        return path;
    }
}
