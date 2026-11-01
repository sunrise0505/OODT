package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CampaignFlowTest {
    private GameController game() { return new GameController(new SaveManager(new MemoryPreferences())); }
    private void advance(GameController game, int ticks) { for (int i = 0; i < ticks; i++) game.update(.05); }
    private void points(GameController game) {
        game.scoreManager().reward(new BossEnemy(List.of(new Vec2(0, 0), new Vec2(900, 0)), 1));
        game.scoreManager().reward(new BossEnemy(List.of(new Vec2(0, 0), new Vec2(900, 0)), 1));
    }

    @Test
    void twoTimesSpeedKeepsAllTimeWhileUsingSafeSteps() {
        GameController game = game();
        game.startLevel(1);
        for (int i = 0; i < 20; i++) game.update(.1);
        assertEquals(2, game.statistics().elapsedSeconds(), .00001);
        assertFalse(game.enemies().isEmpty());
        game.update(Double.NaN);
        game.update(-1);
        assertEquals(2, game.statistics().elapsedSeconds(), .00001);
    }

    @Test
    void waveNumberDoesNotAdvanceWhileLastEnemiesAreStillAlive() {
        GameController game = game();
        game.startLevel(1);
        advance(game, 160);
        assertEquals(8, game.enemies().size());
        assertEquals(1, game.displayedWave());
        game.enemies().forEach(enemy -> enemy.takeDamage(1_000_000));
        game.update(.05);
        assertEquals(1, game.clearedWaves());
        assertTrue(game.waveCountdown() > 0);
        advance(game, 50);
        assertEquals(2, game.displayedWave());
    }

    @Test
    void pausedGameDoesNotMoveShootUpgradeOrSpendPoints() {
        GameController game = game();
        game.startLevel(1);
        advance(game, 40);
        points(game);
        double time = game.statistics().elapsedSeconds();
        Vec2 point = game.enemies().getFirst().position();
        int balance = game.scoreManager().tacticalPoints();
        game.togglePause();
        game.update(.1);
        assertEquals(time, game.statistics().elapsedSeconds());
        assertEquals(point, game.enemies().getFirst().position());
        assertFalse(game.shootAt(point));
        assertFalse(game.upgradeSelectedWeapon());
        assertFalse(game.useSpecialAttack(SpecialAttack.FROST));
        assertEquals(balance, game.scoreManager().tacticalPoints());
    }

    @Test
    void specialAttacksValidateTargetsAndSpendAvailablePointsOnly() {
        GameController game = game();
        game.startLevel(1);
        points(game);
        int balance = game.scoreManager().tacticalPoints();
        int score = game.scoreManager().score();
        assertFalse(game.useSpecialAttack(SpecialAttack.FROST));
        assertFalse(game.useSpecialAttack(SpecialAttack.REPAIR));
        assertEquals(balance, game.scoreManager().tacticalPoints());
        advance(game, 40);
        assertTrue(game.useSpecialAttack(SpecialAttack.FROST));
        assertEquals(balance - 120, game.scoreManager().tacticalPoints());
        assertEquals(score, game.scoreManager().score());
        assertTrue(game.enemies().getFirst().isSlowed());
        game.aimAt(game.enemies().getFirst().position());
        assertTrue(game.useSpecialAttack(SpecialAttack.BOMB));
        assertTrue(game.enemies().getFirst().isDead());
        assertFalse(game.useSpecialAttack(SpecialAttack.FROST));
    }

    @Test
    void tacticalRepairCannotExceedEightLives() {
        GameController game = game();
        game.startLevel(1);
        advance(game, 40);
        game.enemies().getFirst().update(1000);
        game.update(.05);
        points(game);
        assertEquals(7, game.lives());
        assertTrue(game.useSpecialAttack(SpecialAttack.REPAIR));
        assertEquals(8, game.lives());
        int pointsLeft = game.scoreManager().tacticalPoints();
        assertFalse(game.useSpecialAttack(SpecialAttack.REPAIR));
        assertEquals(pointsLeft, game.scoreManager().tacticalPoints());
    }

    @Test
    void deadEnemyIsRewardedOnceAndFeedbackCanBeDrained() {
        GameController game = game();
        game.startLevel(1);
        advance(game, 40);
        Enemy enemy = game.enemies().getFirst();
        enemy.takeDamage(10000);
        game.update(.05);
        assertEquals(25, game.scoreManager().score());
        assertEquals(1, game.statistics().kills());
        assertEquals(1, game.drainFeedback().stream().filter(event -> event.type() == CombatFeedback.Type.REWARD).count());
        assertTrue(game.drainFeedback().isEmpty());
        game.update(.05);
        assertEquals(25, game.scoreManager().score());
    }

    @Test
    void allEightLevelsCanFinishUnlockAndRetainTheirBestResults() {
        MemoryPreferences preferences = new MemoryPreferences();
        GameController game = new GameController(new SaveManager(preferences));
        for (int level = 1; level <= 8; level++) {
            game.startLevel(level);
            for (int tick = 0; tick < 20000 && game.state() == GameState.RUNNING; tick++) {
                game.enemies().forEach(enemy -> enemy.takeDamage(1_000_000));
                game.update(.05);
            }
            assertEquals(GameState.VICTORY, game.state(), "关卡 " + level);
            assertEquals(game.level().waves().size(), game.clearedWaves());
            assertEquals(Math.min(8, level + 1), game.unlockedLevel());
            assertEquals(3, game.levelStars(level));
            assertTrue(game.levelBestScore(level) > 0);
        }
        GameController restarted = new GameController(new SaveManager(preferences));
        assertEquals(8, restarted.unlockedLevel());
        assertEquals(3, restarted.levelStars(8));
        assertTrue(restarted.highScore() > 0);
    }
}
