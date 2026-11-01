package com.oodt.fruitguard.model;

import java.util.List;

public final class SlimeEnemy extends Enemy {
    public SlimeEnemy(List<Vec2> path, double difficultyScale) {
        super(EnemyKind.SLIME, path, difficultyScale, 16);
    }
}
