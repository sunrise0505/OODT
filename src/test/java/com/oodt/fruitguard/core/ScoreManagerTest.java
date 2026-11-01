package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.Enemy;
import com.oodt.fruitguard.model.SlimeEnemy;
import com.oodt.fruitguard.model.Vec2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreManagerTest {
    @Test
    void coinsCannotBeOverspent() {
        ScoreManager manager = new ScoreManager(100);
        assertFalse(manager.spendCoins(101));
        assertEquals(100, manager.coins());
        assertTrue(manager.spendCoins(80));
        assertEquals(20, manager.coins());
    }

    @Test
    void defeatedEnemyAddsAllRewards() {
        ScoreManager manager = new ScoreManager(50);
        Enemy enemy = new SlimeEnemy(List.of(new Vec2(0, 0), new Vec2(10, 0)), 1.0);
        manager.reward(enemy);
        assertEquals(25, manager.score());
        assertEquals(62, manager.coins());
        assertTrue(manager.energy() > 0);
    }

    @Test
    void ultimateRequiresFullEnergyAndConsumesIt() {
        ScoreManager manager = new ScoreManager(0);
        Enemy enemy = new SlimeEnemy(List.of(new Vec2(0, 0), new Vec2(10, 0)), 1.0);
        for (int i = 0; i < 20; i++) manager.reward(enemy);
        assertEquals(100, manager.energy());
        assertTrue(manager.consumeUltimate());
        assertEquals(0, manager.energy());
        assertFalse(manager.consumeUltimate());
    }

    @Test
    void tacticalSpendingDoesNotReduceScoreAndCannotOverdraw() {
        ScoreManager manager = new ScoreManager(0);
        Enemy enemy = new SlimeEnemy(List.of(new Vec2(0, 0), new Vec2(10, 0)), 1);
        for (int i = 0; i < 8; i++) manager.reward(enemy);
        assertEquals(200, manager.tacticalPoints());
        assertTrue(manager.spendTacticalPoints(180));
        assertEquals(20, manager.tacticalPoints());
        assertEquals(200, manager.score());
        assertFalse(manager.spendTacticalPoints(120));
        assertFalse(manager.spendTacticalPoints(-1));
        assertEquals(20, manager.tacticalPoints());
    }
}
