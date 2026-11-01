package com.oodt.fruitguard.model;

public final class StrawberryWeapon extends FruitWeapon {
    public StrawberryWeapon() { super(FruitKind.STRAWBERRY); }

    @Override
    protected Projectile createProjectile(Vec2 from, Vec2 aimPoint, Enemy target) {
        return Projectile.chain(from, aimPoint, target, damage(), 170,
                kind().projectileSpeed(), kind(), kind().color());
    }
}
