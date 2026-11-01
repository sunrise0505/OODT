package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.Enemy;

/** 将积分、金币和技能能量的规则封装为独立领域对象。 */
public final class ScoreManager {
    public static final double MAX_ENERGY = 100;
    private int score;
    private int coins;
    private double energy;
    private int tacticalPoints;

    public ScoreManager(int startingCoins) {
        this.coins = startingCoins;
    }

    public void reward(Enemy enemy) {
        score += enemy.scoreReward();
        tacticalPoints += enemy.scoreReward();
        coins += enemy.coinReward();
        energy = Math.min(MAX_ENERGY, energy + enemy.scoreReward() * 0.34);
    }

    public boolean spendCoins(int amount) {
        if (amount < 0 || coins < amount) return false;
        coins -= amount;
        return true;
    }

    public void addCoins(int amount) { coins += Math.max(0, amount); }

    public boolean spendTacticalPoints(int amount) {
        if (amount < 0 || tacticalPoints < amount) return false;
        tacticalPoints -= amount;
        return true;
    }

    public boolean consumeUltimate() {
        if (energy < MAX_ENERGY) return false;
        energy = 0;
        return true;
    }

    public int score() { return score; }
    public int coins() { return coins; }
    public double energy() { return energy; }
    public int tacticalPoints() { return tacticalPoints; }
}
