package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.*;

/** 工厂模式：根据枚举创建具体水果武器。 */
public final class FruitWeaponFactory {
    private FruitWeaponFactory() {}

    public static FruitWeapon create(FruitKind kind) {
        return switch (kind) {
            case ORANGE -> new OrangeWeapon();
            case GRAPE -> new GrapeWeapon();
            case WATERMELON -> new WatermelonWeapon();
            case LEMON -> new LemonWeapon();
            case STRAWBERRY -> new StrawberryWeapon();
            case PINEAPPLE -> new PineappleWeapon();
            case BLUEBERRY -> new BlueberryWeapon();
            case PEACH -> new PeachWeapon();
        };
    }
}
