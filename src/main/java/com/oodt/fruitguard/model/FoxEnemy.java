package com.oodt.fruitguard.model;

import java.util.List;

public final class FoxEnemy extends Enemy {
    public FoxEnemy(List<Vec2> path, double difficultyScale) {
        super(EnemyKind.FOX, path, difficultyScale, 15);
    }
}
