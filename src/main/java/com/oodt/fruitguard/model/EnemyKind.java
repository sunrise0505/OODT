package com.oodt.fruitguard.model;

public enum EnemyKind {
    SLIME("果冻怪", 85, 58, 12, 25, "普通怪物，能力均衡"),
    FOX("疾风狐", 62, 94, 16, 32, "移动迅速但血量较低"),
    BOAR("铁甲野猪", 210, 39, 28, 58, "高血量重甲单位"),
    BOSS("贪吃王", 780, 30, 90, 180, "关底首领，血量极高");

    private final String displayName;
    private final double health;
    private final double speed;
    private final int coinReward;
    private final int scoreReward;
    private final String description;

    EnemyKind(String displayName, double health, double speed, int coinReward, int scoreReward, String description) {
        this.displayName = displayName;
        this.health = health;
        this.speed = speed;
        this.coinReward = coinReward;
        this.scoreReward = scoreReward;
        this.description = description;
    }

    public String displayName() { return displayName; }
    public double health() { return health; }
    public double speed() { return speed; }
    public int coinReward() { return coinReward; }
    public int scoreReward() { return scoreReward; }
    public String description() { return description; }
}
