package repeatablerelicsmod.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class TextureLoader {
    private static final HashMap<String, Texture> textures = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(TextureLoader.class);

    /**
     * @param filePath - String path to the texture you want to load relative to resources.
     * @return <b>com.badlogic.gdx.graphics.Texture</b> - The texture from the path provided, or null if it doesn't exist.
     */
    public static Texture getTexture(final String filePath) {
        return getTexture(filePath, true);
    }

    public static Texture getTexture(final String filePath, boolean linear) {
        if (textures.get(filePath) == null) {
            try {
                loadTexture(filePath, linear);
            } catch (GdxRuntimeException e) {
                logger.info("Failed to find texture " + filePath, e);
                return null; // Return null as images are not required
            }
        }
        return textures.get(filePath);
    }

    private static void loadTexture(final String textureString, boolean linearFilter) throws GdxRuntimeException {
        Texture texture = new Texture(textureString);
        if (linearFilter) {
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } else {
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
        logger.info("Loaded texture " + textureString);
        textures.put(textureString, texture);
    }
}
