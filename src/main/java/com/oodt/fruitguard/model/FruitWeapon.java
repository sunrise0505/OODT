package com.oodt.fruitguard.model;

import java.util.Optional;

/** 水果武器抽象基类：统一管理冷却和升级，子类决定弹药效果。 */
public abstract class FruitWeapon {
    private final FruitKind kind;
    private double cooldownRemaining;
    private int level = 1;

    protected FruitWeapon(FruitKind kind) {
        this.kind = kind;
    }

    public void update(double deltaSeconds) {
        cooldownRemaining = Math.max(0, cooldownRemaining - deltaSeconds);
    }

    public Optional<Projectile> fire(Vec2 from, Vec2 aimPoint, Enemy lockedTarget) {
        if (cooldownRemaining > 0) return Optional.empty();
        cooldownRemaining = fireInterval();
        return Optional.of(createProjectile(from, aimPoint, lockedTarget));
    }

    protected abstract Projectile createProjectile(Vec2 from, Vec2 aimPoint, Enemy lockedTarget);

    public boolean upgrade() {
        if (level >= 3) return false;
        level++;
        return true;
    }

    public int upgradeCost() {
        return level >= 3 ? 0 : 85 + kind.ordinal() * 18 + level * 45;
    }

    public double damage() { return kind.damage() * (1 + (level - 1) * 0.32); }
    public double fireInterval() { return kind.cooldown() * Math.pow(0.9, level - 1); }
    public double cooldownRatio() { return Math.min(1, cooldownRemaining / fireInterval()); }
    public boolean ready() { return cooldownRemaining <= 0; }
    public FruitKind kind() { return kind; }
    public int level() { return level; }
}
