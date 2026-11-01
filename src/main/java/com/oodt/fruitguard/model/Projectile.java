package com.oodt.fruitguard.model;

import javafx.scene.paint.Color;

import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/** 玩家瞄准后发射的弹药；负责飞行、碰撞和命中效果。 */
public final class Projectile {
    private Vec2 position;
    private Vec2 destination;
    private final Enemy lockedTarget;
    private double damage;
    private final double splashRadius;
    private final double slowFactor;
    private final double slowSeconds;
    private double speed;
    private final FruitKind fruitKind;
    private final Color color;
    private boolean alive = true;
    private double travelled;
    private double chainRadius;
    private int piercingTargets;
    private final Set<Enemy> hitTargets = new HashSet<>();
    private boolean hitConfirmed;

    public Projectile charged(double power) {
        double charge = Math.clamp(power, 0, 1);
        damage *= .75 + .75 * charge;
        speed *= .85 + .35 * charge;
        return this;
    }

    private Projectile(Vec2 position, Vec2 destination, Enemy lockedTarget, double damage,
                       double splashRadius, double slowFactor, double slowSeconds,
                       double speed, FruitKind fruitKind, Color color) {
        this.position = position;
        this.destination = destination;
        this.lockedTarget = lockedTarget;
        this.damage = damage;
        this.splashRadius = splashRadius;
        this.slowFactor = slowFactor;
        this.slowSeconds = slowSeconds;
        this.speed = speed;
        this.fruitKind = fruitKind;
        this.color = color;
    }

    public static Projectile single(Vec2 from, Vec2 destination, Enemy target, double damage,
                                    double speed, FruitKind kind, Color color) {
        return new Projectile(from, destination, target, damage, 0, 1, 0, speed, kind, color);
    }

    public static Projectile splash(Vec2 from, Vec2 destination, Enemy target, double damage,
                                    double radius, double speed, FruitKind kind, Color color) {
        return new Projectile(from, destination, target, damage, radius, 1, 0, speed, kind, color);
    }

    public static Projectile slow(Vec2 from, Vec2 destination, Enemy target, double damage,
                                  double slowFactor, double slowSeconds, double speed,
                                  FruitKind kind, Color color) {
        return new Projectile(from, destination, target, damage, 0, slowFactor, slowSeconds,
                speed, kind, color);
    }

    public static Projectile chain(Vec2 from, Vec2 destination, Enemy target, double damage,
                                   double radius, double speed, FruitKind kind, Color color) {
        Projectile shot = single(from, destination, target, damage, speed, kind, color);
        shot.chainRadius = radius;
        return shot;
    }

    public static Projectile piercing(Vec2 from, Vec2 aim, double damage, int targets,
                                      double speed, FruitKind kind, Color color) {
        double distance = from.distanceTo(aim);
        Vec2 end = distance < .001 ? new Vec2(from.x(), from.y() - 1200)
                : from.interpolate(aim, 1200 / distance);
        Projectile shot = single(from, end, null, damage, speed, kind, color);
        shot.piercingTargets = targets;
        return shot;
    }

    public static Projectile frostSplash(Vec2 from, Vec2 destination, Enemy target, double damage,
                                         double radius, double slowFactor, double slowSeconds,
                                         double speed, FruitKind kind, Color color) {
        return new Projectile(from, destination, target, damage, radius, slowFactor, slowSeconds,
                speed, kind, color);
    }

    public void update(double deltaSeconds, List<Enemy> enemies) {
        if (!alive) return;
        if (lockedTarget != null && !lockedTarget.isDead() && !lockedTarget.reachedEnd()) {
            destination = lockedTarget.position();
        }

        double distance = position.distanceTo(destination);
        double step = speed * deltaSeconds;
        if (distance <= step + 5) {
            position = destination;
            Enemy hit = findCollision(enemies, Math.max(18, splashRadius > 0 ? 34 : 22));
            impact(enemies, hit, destination);
            alive = false;
            return;
        }

        Vec2 next = position.interpolate(destination, step / distance);
        travelled += position.distanceTo(next);
        position = next;
        Enemy hit = findCollision(enemies, 13);
        if (hit != null) {
            impact(enemies, hit, hit.position());
            hitTargets.add(hit);
            if (piercingTargets == 0 || hitTargets.size() >= piercingTargets) alive = false;
        } else if (travelled > 1200) {
            alive = false;
        }
    }

    private Enemy findCollision(List<Enemy> enemies, double extraRadius) {
        return enemies.stream()
                .filter(enemy -> !enemy.isDead() && !enemy.reachedEnd())
                .filter(enemy -> !hitTargets.contains(enemy))
                .filter(enemy -> position.distanceTo(enemy.position()) <= enemy.radius() + extraRadius)
                .min(Comparator.comparingDouble(enemy -> position.distanceTo(enemy.position())))
                .orElse(null);
    }

    private void impact(List<Enemy> enemies, Enemy directHit, Vec2 point) {
        if (splashRadius > 0) {
            for (Enemy enemy : enemies) {
                if (!enemy.isDead() && enemy.position().distanceTo(point) <= splashRadius) {
                    hitConfirmed = true;
                    double ratio = enemy == directHit ? 1.0 : 0.62;
                    enemy.takeDamage(damage * ratio);
                    if (slowSeconds > 0 && !enemy.isDead()) enemy.slow(slowFactor, slowSeconds);
                }
            }
        } else if (directHit != null) {
            hitConfirmed = true;
            directHit.takeDamage(damage);
            if (slowSeconds > 0 && !directHit.isDead()) directHit.slow(slowFactor, slowSeconds);
            if (chainRadius > 0) {
                Set<Enemy> chained = new HashSet<>();
                chained.add(directHit);
                Vec2 origin = directHit.position();
                for (int bounce = 1; bounce <= 2; bounce++) {
                    Vec2 previous = origin;
                    Enemy next = enemies.stream()
                            .filter(enemy -> !enemy.isDead() && !enemy.reachedEnd() && !chained.contains(enemy))
                            .filter(enemy -> previous.distanceTo(enemy.position()) <= chainRadius)
                            .min(Comparator.comparingDouble(enemy -> previous.distanceTo(enemy.position())))
                            .orElse(null);
                    if (next == null) break;
                    next.takeDamage(damage * Math.pow(.75, bounce));
                    chained.add(next);
                    origin = next.position();
                }
            }
        }
    }

    public Vec2 position() { return position; }
    public Vec2 destination() { return destination; }
    public Color color() { return color; }
    public FruitKind fruitKind() { return fruitKind; }
    public boolean isAlive() { return alive; }
    public boolean hasHit() { return hitConfirmed; }
}
