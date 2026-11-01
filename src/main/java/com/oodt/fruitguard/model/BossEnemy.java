package com.oodt.fruitguard.model;

import java.util.List;

public final class BossEnemy extends Enemy {
    public BossEnemy(List<Vec2> path, double difficultyScale) {
        super(EnemyKind.BOSS, path, difficultyScale, 26);
    }
}
