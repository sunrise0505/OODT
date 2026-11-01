package com.oodt.fruitguard.core;

/** 一局的射击与防守统计。一次范围攻击命中多人也只计一次命中。 */
public final class BattleStatistics {
    private int shots;
    private int hits;
    private int kills;
    private double elapsedSeconds;

    void recordShot() { shots++; }
    void recordHit() { hits++; }
    void recordKill() { kills++; }
    void advance(double dt) { elapsedSeconds += dt; }

    public int shots() { return shots; }
    public int hits() { return hits; }
    public int kills() { return kills; }
    public double accuracy() { return shots == 0 ? 0 : (double) hits / shots; }
    public double elapsedSeconds() { return elapsedSeconds; }
    public static int starsForLives(int lives) { return lives >= 7 ? 3 : lives >= 4 ? 2 : lives > 0 ? 1 : 0; }
}
