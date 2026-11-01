package com.oodt.fruitguard.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnemyTest {
    @Test
    void enemyMovesAlongPathAndEventuallyReachesEnd() {
        Enemy enemy = new SlimeEnemy(List.of(new Vec2(0, 0), new Vec2(100, 0)), 1.0);
        enemy.update(1.0);
        assertTrue(enemy.position().x() > 0);
        assertFalse(enemy.reachedEnd());
        enemy.update(2.0);
        assertTrue(enemy.reachedEnd());
    }

    @Test
    void damageNeverProducesNegativeHealthAndRewardCanOnlyBeClaimedOnce() {
        Enemy enemy = new FoxEnemy(List.of(new Vec2(0, 0), new Vec2(100, 0)), 1.0);
        enemy.takeDamage(10_000);
        assertEquals(0, enemy.health());
        assertTrue(enemy.claimReward());
        assertFalse(enemy.claimReward());
    }

    @Test
    void slowEffectReducesMovementTemporarily() {
        List<Vec2> path = List.of(new Vec2(0, 0), new Vec2(1000, 0));
        Enemy normal = new SlimeEnemy(path, 1.0);
        Enemy slowed = new SlimeEnemy(path, 1.0);
        slowed.slow(0.5, 2.0);
        normal.update(1.0);
        slowed.update(1.0);
        assertTrue(slowed.position().x() < normal.position().x());
    }
}
