package com.oodt.fruitguard.model;

public final class PineappleWeapon extends FruitWeapon {
    public PineappleWeapon() { super(FruitKind.PINEAPPLE); }

    @Override
    protected Projectile createProjectile(Vec2 from, Vec2 aimPoint, Enemy target) {
        return Projectile.piercing(from, aimPoint, damage(), 3,
                kind().projectileSpeed(), kind(), kind().color());
    }
}
