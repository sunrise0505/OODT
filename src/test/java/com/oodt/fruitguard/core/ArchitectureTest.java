package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArchitectureTest {
    @Test
    void courseRequirementsAreRepresentedInData() {
        assertTrue(LevelRepository.all().size() >= 2);
        assertTrue(FruitKind.values().length >= 2);
        assertTrue(EnemyKind.values().length >= 2);
        assertEquals(8, LevelRepository.all().size());
        assertEquals(8, FruitKind.values().length);
        assertEquals(java.util.List.of(3, 4, 5, 4, 6, 5, 7, 8),
                LevelRepository.all().stream().map(level -> level.waves().size()).toList());
        for (int i = 0; i < LevelRepository.all().size(); i++) {
            var level = LevelRepository.all().get(i);
            assertEquals(i + 1, level.number());
            assertTrue(level.path().stream().allMatch(point -> point.y() < 395),
                    "路线不能穿过中央英雄面板");
            assertTrue(level.waves().stream().allMatch(wave -> !wave.entries().isEmpty()));
        }
    }

    @Test
    void weaponFactoryReturnsDifferentPolymorphicImplementations() {
        FruitWeapon orange = FruitWeaponFactory.create(FruitKind.ORANGE);
        FruitWeapon melon = FruitWeaponFactory.create(FruitKind.WATERMELON);
        assertInstanceOf(OrangeWeapon.class, orange);
        assertInstanceOf(WatermelonWeapon.class, melon);
        assertNotEquals(orange.kind(), melon.kind());
        for (FruitKind kind : FruitKind.values()) assertEquals(kind, FruitWeaponFactory.create(kind).kind());
    }
}
