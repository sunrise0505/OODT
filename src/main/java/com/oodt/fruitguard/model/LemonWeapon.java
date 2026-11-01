package com.oodt.fruitguard.model;

public final class LemonWeapon extends FruitWeapon {
    public LemonWeapon() { super(FruitKind.LEMON); }

    @Override
    protected Projectile createProjectile(Vec2 from, Vec2 aimPoint, Enemy lockedTarget) {
        return Projectile.slow(from, aimPoint, lockedTarget, damage(), 0.48, 2.6,
                kind().projectileSpeed(), kind(), kind().color());
    }
}
