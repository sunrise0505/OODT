package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.*;

import java.util.List;

/** 工厂模式：集中屏蔽具体怪物子类的创建逻辑。 */
public final class EnemyFactory {
    private EnemyFactory() {}

    public static Enemy create(EnemyKind kind, List<Vec2> path, double difficultyScale) {
        return switch (kind) {
            case SLIME -> new SlimeEnemy(path, difficultyScale);
            case FOX -> new FoxEnemy(path, difficultyScale);
            case BOAR -> new BoarEnemy(path, difficultyScale);
            case BOSS -> new BossEnemy(path, difficultyScale);
        };
    }
}
