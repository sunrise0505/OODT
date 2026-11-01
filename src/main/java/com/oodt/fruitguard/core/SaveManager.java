package com.oodt.fruitguard.core;

import java.util.prefs.Preferences;

/** 使用 JDK Preferences 保存最高分和已解锁关卡，不依赖数据库。 */
public final class SaveManager {
    private static final String HIGH_SCORE = "highScore";
    private static final String UNLOCKED_LEVEL = "unlockedLevel";
    private static final int LEVEL_COUNT = LevelRepository.all().size();
    private final Preferences preferences;

    public SaveManager() { this(Preferences.userNodeForPackage(SaveManager.class)); }
    SaveManager(Preferences preferences) { this.preferences = preferences; }

    public int highScore() { return preferences.getInt(HIGH_SCORE, 0); }
    public int unlockedLevel() { return Math.clamp(preferences.getInt(UNLOCKED_LEVEL, 1), 1, LEVEL_COUNT); }

    public void recordResult(int level, int score, boolean victory) {
        recordResult(level, score, victory, victory ? 1 : 0);
    }

    public void recordResult(int level, int score, boolean victory, int stars) {
        if (level < 1 || level > LEVEL_COUNT) return;
        if (score > highScore()) preferences.putInt(HIGH_SCORE, score);
        if (score > bestScore(level)) preferences.putInt("level." + level + ".bestScore", score);
        if (victory) preferences.putInt("level." + level + ".stars", Math.max(stars(level), Math.clamp(stars, 1, 3)));
        if (victory) preferences.putInt(UNLOCKED_LEVEL, Math.max(unlockedLevel(), Math.min(LEVEL_COUNT, level + 1)));
    }

    public int stars(int level) { return Math.clamp(preferences.getInt("level." + level + ".stars", 0), 0, 3); }
    public int bestScore(int level) { return Math.max(0, preferences.getInt("level." + level + ".bestScore", 0)); }
}
