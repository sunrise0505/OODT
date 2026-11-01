package com.oodt.fruitguard.model;

/** 不可变二维坐标，集中处理地图中的向量计算。 */
public record Vec2(double x, double y) {
    public double distanceTo(Vec2 other) {
        return Math.hypot(other.x - x, other.y - y);
    }

    public Vec2 interpolate(Vec2 other, double ratio) {
        return new Vec2(x + (other.x - x) * ratio, y + (other.y - y) * ratio);
    }
}
