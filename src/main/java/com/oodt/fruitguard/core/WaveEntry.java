package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.EnemyKind;

public record WaveEntry(EnemyKind kind, int count, double intervalSeconds) {
    public WaveEntry {
        if (count <= 0 || intervalSeconds <= 0) throw new IllegalArgumentException("波次参数必须为正数");
    }
}
