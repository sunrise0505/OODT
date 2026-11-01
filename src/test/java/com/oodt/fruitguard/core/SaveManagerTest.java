package com.oodt.fruitguard.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest {
    @Test
    void weakerReplayNeverLowersBestStarsOrScore() {
        SaveManager save = new SaveManager(new MemoryPreferences());
        save.recordResult(1, 800, true, 3);
        save.recordResult(1, 200, true, 1);
        save.recordResult(1, 100, false, 0);
        assertEquals(3, save.stars(1));
        assertEquals(800, save.bestScore(1));
        assertEquals(800, save.highScore());
        assertEquals(2, save.unlockedLevel());
    }

    @Test
    void oldProgressIsPreservedAndInvalidLevelsAreIgnored() {
        MemoryPreferences prefs = new MemoryPreferences();
        prefs.putInt("unlockedLevel", 5);
        prefs.putInt("highScore", 999);
        SaveManager save = new SaveManager(prefs);
        assertEquals(5, save.unlockedLevel());
        assertEquals(999, save.highScore());
        assertEquals(0, save.stars(3));
        save.recordResult(99, 10000, true, 3);
        assertEquals(999, save.highScore());
        save.recordResult(8, 600, true, 3);
        assertEquals(8, save.unlockedLevel());
    }
}
