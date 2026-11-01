package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.Vec2;

import java.util.List;

public record LevelConfig(int number, String name, String subtitle, List<Vec2> path,
                          List<WaveConfig> waves,
                          int startingCoins, double difficultyScale) {
    public LevelConfig {
        path = List.copyOf(path);
        waves = List.copyOf(waves);
    }
}
