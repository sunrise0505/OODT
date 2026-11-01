package com.oodt.fruitguard.model;

import javafx.scene.paint.Color;

/** 可由玩家手动切换和瞄准的水果武器，图集索引按每张四格排列。 */
public enum FruitKind {
    ORANGE("橙光弹弓", 0, 34, 0.42, 650, Color.web("#ff9d24"),
            "精准重弹", "日耀贯穿", "锁定前排敌人，连续造成高额穿透伤害"),
    GRAPE("葡萄连发炮", 1, 13, 0.16, 760, Color.web("#a865e8"),
            "射速最快", "万籽弹幕", "向所有在场怪物倾泻一轮葡萄籽弹幕"),
    WATERMELON("西瓜重投器", 2, 55, 1.05, 470, Color.web("#42b85c"),
            "落点范围爆炸", "瓜星坠落", "在怪物最密集的位置制造巨型范围冲击"),
    LEMON("冰柠法杖", 3, 22, 0.70, 560, Color.web("#ffd84d"),
            "命中后减速", "绝对零度", "冻结全场并持续大幅降低怪物速度"),
    STRAWBERRY("草莓连锁杖", 4, 28, 0.62, 720, Color.web("#f45c89"),
            "连锁弹跳三目标", "莓雨连锁", "对前方五名敌人造成递减的连锁重击"),
    PINEAPPLE("菠萝穿甲枪", 5, 48, 0.78, 920, Color.web("#f3bf39"),
            "直线贯穿三目标", "金棘破阵", "重创全场铁甲与首领，并冲击其他怪物"),
    BLUEBERRY("蓝莓霜爆炮", 6, 26, 0.90, 610, Color.web("#7c9dff"),
            "范围霜爆减速", "极夜霜环", "对密集敌群造成寒霜爆炸与强力减速"),
    PEACH("蜜桃守护杖", 7, 46, 0.68, 680, Color.web("#ffb1a5"),
            "高伤花瓣弹", "桃源复苏", "恢复两点果仓生命，花瓣风暴伤害全场");

    private final String displayName;
    private final int atlasIndex;
    private final double damage;
    private final double cooldown;
    private final double projectileSpeed;
    private final Color color;
    private final String description;
    private final String ultimateName;
    private final String ultimateDescription;

    FruitKind(String displayName, int atlasIndex, double damage, double cooldown,
              double projectileSpeed, Color color, String description,
              String ultimateName, String ultimateDescription) {
        this.displayName = displayName;
        this.atlasIndex = atlasIndex;
        this.damage = damage;
        this.cooldown = cooldown;
        this.projectileSpeed = projectileSpeed;
        this.color = color;
        this.description = description;
        this.ultimateName = ultimateName;
        this.ultimateDescription = ultimateDescription;
    }

    public String displayName() { return displayName; }
    public int atlasIndex() { return atlasIndex; }
    public String atlasFile() { return atlasIndex < 4 ? "fruit-heroes-atlas.png" : "fruit-heroes-extra-atlas.png"; }
    public int atlasCell() { return atlasIndex % 4; }
    public double damage() { return damage; }
    public double cooldown() { return cooldown; }
    public double projectileSpeed() { return projectileSpeed; }
    public Color color() { return color; }
    public String description() { return description; }
    public String ultimateName() { return ultimateName; }
    public String ultimateDescription() { return ultimateDescription; }
}
