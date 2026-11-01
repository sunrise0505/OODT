package com.oodt.fruitguard.model;

public final class PeachWeapon extends FruitWeapon {
    public PeachWeapon() { super(FruitKind.PEACH); }

    @Override
    protected Projectile createProjectile(Vec2 from, Vec2 aimPoint, Enemy target) {
        return Projectile.single(from, aimPoint, target, damage(),
                kind().projectileSpeed(), kind(), kind().color());
    }
}
