package com.oodt.fruitguard.model;

import java.util.List;

public final class BoarEnemy extends Enemy {
    public BoarEnemy(List<Vec2> path, double difficultyScale) {
        super(EnemyKind.BOAR, path, difficultyScale, 19);
    }
}
