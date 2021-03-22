package com.pavelurusov.jfractal;

/** @author Pavel Urusov, me@pavelurusov.com
 * This is a singleton class that holds the global settings
 */

public final class Settings {

    private static Settings instance = null;

    public final int IMG_WIDTH = 3200;
    public final int IMG_HEIGHT = 3200;
    public final int MAX_ITERATIONS = 96;

    public int colorOffset = 0;

    private Settings() {

    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }
}
