package com.oodt.fruitguard.core;

import java.util.List;

public record WaveConfig(List<WaveEntry> entries) {
    public WaveConfig {
        entries = List.copyOf(entries);
    }
}
