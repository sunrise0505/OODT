package com.oodt.fruitguard.model;

public final class WatermelonWeapon extends FruitWeapon {
    public WatermelonWeapon() { super(FruitKind.WATERMELON); }

    @Override
    protected Projectile createProjectile(Vec2 from, Vec2 aimPoint, Enemy lockedTarget) {
        return Projectile.splash(from, aimPoint, lockedTarget, damage(), 82,
                kind().projectileSpeed(), kind(), kind().color());
    }
}
