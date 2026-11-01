package com.oodt.fruitguard.model;

/** 战斗内用击杀积分兑换的战术技能，与免费蓄能大招相互独立。 */
public enum SpecialAttack {
    FROST("寒霜风暴", 120, "全场怪物减速 4 秒", "Q"),
    BOMB("果实炸弹", 180, "在最后瞄准位置造成范围爆炸", "E"),
    REPAIR("果仓修复", 200, "恢复 2 点生命，上限 8 点", "R");

    private final String displayName;
    private final int cost;
    private final String description;
    private final String shortcut;

    SpecialAttack(String displayName, int cost, String description, String shortcut) {
        this.displayName = displayName;
        this.cost = cost;
        this.description = description;
        this.shortcut = shortcut;
    }

    public String displayName() { return displayName; }
    public int cost() { return cost; }
    public String description() { return description; }
    public String shortcut() { return shortcut; }
}
