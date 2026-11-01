package com.oodt.fruitguard.model;

public final class GrapeWeapon extends FruitWeapon {
    public GrapeWeapon() { super(FruitKind.GRAPE); }

    @Override
    protected Projectile createProjectile(Vec2 from, Vec2 aimPoint, Enemy lockedTarget) {
        return Projectile.single(from, aimPoint, lockedTarget, damage(),
                kind().projectileSpeed(), kind(), kind().color());
    }
}
