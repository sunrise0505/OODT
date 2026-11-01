package com.oodt.fruitguard.model;

import java.util.List;

/** 怪物抽象基类：封装路径移动、生命值、减速与奖励状态。 */
public abstract class Enemy {
    private final EnemyKind kind;
    private final double maxHealth;
    private final double baseSpeed;
    private final int coinReward;
    private final int scoreReward;
    private final double radius;
    private final List<Vec2> path;
    private double health;
    private int segmentIndex;
    private double segmentProgress;
    private Vec2 position;
    private boolean reachedEnd;
    private boolean rewardClaimed;
    private double slowFactor = 1.0;
    private double slowRemaining;

    protected Enemy(EnemyKind kind, List<Vec2> path, double difficultyScale, double radius) {
        this.kind = kind;
        this.path = List.copyOf(path);
        this.maxHealth = kind.health() * difficultyScale;
        this.health = maxHealth;
        this.baseSpeed = kind.speed() * (0.92 + difficultyScale * 0.08);
        this.coinReward = kind.coinReward();
        this.scoreReward = kind.scoreReward();
        this.radius = radius;
        this.position = path.getFirst();
    }

    public void update(double deltaSeconds) {
        if (isDead() || reachedEnd) return;
        if (slowRemaining > 0) {
            slowRemaining -= deltaSeconds;
            if (slowRemaining <= 0) slowFactor = 1.0;
        }
        double remainingDistance = baseSpeed * slowFactor * deltaSeconds;
        while (remainingDistance > 0 && segmentIndex < path.size() - 1) {
            Vec2 from = path.get(segmentIndex);
            Vec2 to = path.get(segmentIndex + 1);
            double segmentLength = from.distanceTo(to);
            double available = segmentLength * (1.0 - segmentProgress);
            if (remainingDistance < available) {
                segmentProgress += remainingDistance / segmentLength;
                position = from.interpolate(to, segmentProgress);
                remainingDistance = 0;
            } else {
                remainingDistance -= available;
                segmentIndex++;
                segmentProgress = 0;
                position = path.get(segmentIndex);
            }
        }
        if (segmentIndex >= path.size() - 1) reachedEnd = true;
    }

    public void takeDamage(double amount) {
        health = Math.max(0, health - Math.max(0, amount));
    }

    public void slow(double factor, double seconds) {
        if (factor < slowFactor || slowRemaining <= 0) slowFactor = Math.max(0.25, factor);
        slowRemaining = Math.max(slowRemaining, seconds);
    }

    public double routeProgress() {
        return segmentIndex + segmentProgress;
    }

    public boolean claimReward() {
        if (!isDead() || rewardClaimed) return false;
        rewardClaimed = true;
        return true;
    }

    public EnemyKind kind() { return kind; }
    public double health() { return health; }
    public double maxHealth() { return maxHealth; }
    public double healthRatio() { return health / maxHealth; }
    public double baseSpeed() { return baseSpeed; }
    public double currentSpeed() { return baseSpeed * slowFactor; }
    public int coinReward() { return coinReward; }
    public int scoreReward() { return scoreReward; }
    public double radius() { return radius; }
    public Vec2 position() { return position; }
    public boolean isDead() { return health <= 0; }
    public boolean reachedEnd() { return reachedEnd; }
    public boolean isSlowed() { return slowFactor < 1.0; }
}
