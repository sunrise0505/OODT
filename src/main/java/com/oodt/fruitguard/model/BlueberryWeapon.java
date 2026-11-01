package com.oodt.fruitguard.model;

public final class BlueberryWeapon extends FruitWeapon {
    public BlueberryWeapon() { super(FruitKind.BLUEBERRY); }

    @Override
    protected Projectile createProjectile(Vec2 from, Vec2 aimPoint, Enemy target) {
        return Projectile.frostSplash(from, aimPoint, target, damage(), 100, .58, 2.8,
                kind().projectileSpeed(), kind(), kind().color());
    }
}
