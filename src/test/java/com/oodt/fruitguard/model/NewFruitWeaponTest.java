package com.oodt.fruitguard.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class NewFruitWeaponTest {
    private Enemy slime(double x, double y) {
        return new SlimeEnemy(List.of(new Vec2(x, y), new Vec2(x + 900, y)), 1);
    }

    @Test
    void strawberryChainsToTwoNearbyEnemiesButNotDistantOnes() {
        List<Enemy> enemies = List.of(slime(200, 200), slime(300, 200), slime(400, 200), slime(700, 200));
        Projectile shot = new StrawberryWeapon().fire(new Vec2(200, 200), enemies.getFirst().position(),
                enemies.getFirst()).orElseThrow();
        shot.update(.05, enemies);
        assertEquals(57, enemies.get(0).health(), .001);
        assertEquals(64, enemies.get(1).health(), .001);
        assertEquals(69.25, enemies.get(2).health(), .001);
        assertEquals(85, enemies.get(3).health(), .001);
    }

    @Test
    void pineapplePiercesThreeEnemiesAndNeverDamagesAnEnemyTwice() {
        List<Enemy> enemies = List.of(slime(100, 200), slime(180, 200), slime(260, 200), slime(340, 200));
        Projectile shot = new PineappleWeapon().fire(new Vec2(0, 200), new Vec2(500, 200), null).orElseThrow();
        for (int i = 0; i < 100; i++) shot.update(.01, enemies);
        for (int i = 0; i < 3; i++) assertEquals(37, enemies.get(i).health(), .001);
        assertEquals(85, enemies.get(3).health(), .001);
        assertFalse(shot.isAlive());
    }

    @Test
    void blueberrySlowsOnlyEnemiesInsideItsBlast() {
        List<Enemy> enemies = List.of(slime(200, 200), slime(270, 200), slime(450, 200));
        Projectile shot = new BlueberryWeapon().fire(new Vec2(200, 200), enemies.getFirst().position(),
                enemies.getFirst()).orElseThrow();
        shot.update(.05, enemies);
        assertTrue(enemies.get(0).isSlowed());
        assertTrue(enemies.get(1).isSlowed());
        assertFalse(enemies.get(2).isSlowed());
        assertEquals(85, enemies.get(2).health());
    }

    @Test
    void fullyChargedSlingshotDealsOneAndAHalfTimesBaseDamage() {
        Enemy target = slime(200, 200);
        Projectile shot = new OrangeWeapon().fire(target.position(), target.position(), null).orElseThrow().charged(1);
        shot.update(.05, List.of(target));
        assertEquals(34, target.health(), .0001);
        assertTrue(shot.hasHit());
    }

    @Test
    void upgradedWatermelonOneShotsFoxButArmoredBoarNeedsMoreHits() {
        WatermelonWeapon weapon = new WatermelonWeapon();
        weapon.upgrade();
        Enemy fox = new FoxEnemy(List.of(new Vec2(200, 200), new Vec2(1000, 200)), 1);
        Enemy boar = new BoarEnemy(List.of(new Vec2(450, 200), new Vec2(1000, 200)), 1);
        Projectile first = weapon.fire(fox.position(), fox.position(), fox).orElseThrow();
        first.update(.05, List.of(fox, boar));
        assertTrue(fox.isDead());
        weapon.update(2);
        Projectile second = weapon.fire(boar.position(), boar.position(), boar).orElseThrow();
        second.update(.05, List.of(boar));
        assertFalse(boar.isDead());
        assertTrue(boar.health() < boar.maxHealth());
    }
}
