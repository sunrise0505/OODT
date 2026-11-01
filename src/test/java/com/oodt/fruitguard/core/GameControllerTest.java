package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.GameState;
import com.oodt.fruitguard.model.FruitKind;
import com.oodt.fruitguard.model.Vec2;
import com.oodt.fruitguard.model.BossEnemy;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {
    @Test
    void selectedFruitWeaponCanBeUpgradedWithEarnedCoins() {
        GameController controller = new GameController();
        controller.startLevel(1);
        int startingCoins = controller.scoreManager().coins();
        controller.selectKind(FruitKind.GRAPE);
        int cost = controller.selectedWeapon().upgradeCost();

        assertTrue(controller.upgradeSelectedWeapon());
        assertEquals(2, controller.selectedWeapon().level());
        assertEquals(startingCoins - cost, controller.scoreManager().coins());
    }

    @Test
    void firstWaveSpawnsAndPlayerCanAimAtEnemy() {
        GameController controller = new GameController();
        controller.startLevel(1);

        for (int i = 0; i < 40; i++) controller.update(0.05);

        assertEquals(GameState.RUNNING, controller.state());
        assertFalse(controller.enemies().isEmpty());
        assertTrue(controller.shootAt(controller.enemies().getFirst().position()));
        assertFalse(controller.projectiles().isEmpty());
        assertEquals(new Vec2(450, 485), controller.projectiles().getFirst().position());
    }

    @Test
    void everyFruitHasAnExecutableUltimate() {
        for (FruitKind kind : FruitKind.values()) {
            GameController controller = new GameController();
            controller.startLevel(1);
            for (int i = 0; i < 40; i++) controller.update(.05);
            controller.scoreManager().reward(new BossEnemy(List.of(new Vec2(0, 0), new Vec2(1, 0)), 1));
            controller.scoreManager().reward(new BossEnemy(List.of(new Vec2(0, 0), new Vec2(1, 0)), 1));
            controller.selectKind(kind);
            double before = controller.enemies().stream().mapToDouble(enemy -> enemy.health()).sum();
            assertTrue(controller.useUltimate(), kind.name());
            assertEquals(0, controller.scoreManager().energy());
            assertEquals(kind, controller.ultimateVisualKind());
            assertTrue(controller.enemies().stream().mapToDouble(enemy -> enemy.health()).sum() < before);
        }
    }

    @Test
    void peachUltimateRestoresLivesWithoutExceedingMaximum() {
        GameController controller = new GameController();
        controller.startLevel(1);
        for (int i = 0; i < 40; i++) controller.update(.05);
        controller.enemies().getFirst().update(1000);
        controller.update(.05);
        assertEquals(7, controller.lives());
        for (int i = 0; i < 20; i++) controller.update(.05);
        controller.scoreManager().reward(new BossEnemy(List.of(new Vec2(0, 0), new Vec2(1, 0)), 1));
        controller.scoreManager().reward(new BossEnemy(List.of(new Vec2(0, 0), new Vec2(1, 0)), 1));
        controller.selectKind(FruitKind.PEACH);
        assertTrue(controller.useUltimate());
        assertEquals(8, controller.lives());
    }
}
