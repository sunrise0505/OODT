package com.oodt.fruitguard.model;

/** 领域层发布瞬时反馈，由界面消费；不让模型依赖 Canvas 或动画。 */
public record CombatFeedback(Vec2 position, String text, Type type, FruitKind fruit) {
    public enum Type { DAMAGE, REWARD, ESCAPE, HEAL, SPECIAL }
}
