package com.oodt.fruitguard.model;

public final class OrangeWeapon extends FruitWeapon {
    public OrangeWeapon() { super(FruitKind.ORANGE); }

    @Override
    protected Projectile createProjectile(Vec2 from, Vec2 aimPoint, Enemy lockedTarget) {
        return Projectile.single(from, aimPoint, lockedTarget, damage(),
                kind().projectileSpeed(), kind(), kind().color());
    }
}
