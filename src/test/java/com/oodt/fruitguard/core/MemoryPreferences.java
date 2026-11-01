package com.oodt.fruitguard.core;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;

/** 隔离测试存档，完整通关测试不会更改玩家的真实记录。 */
final class MemoryPreferences extends AbstractPreferences {
    private final Map<String, String> values = new HashMap<>();
    private final Map<String, MemoryPreferences> children = new HashMap<>();
    MemoryPreferences() { super(null, ""); }
    private MemoryPreferences(AbstractPreferences parent, String name) { super(parent, name); }
    protected void putSpi(String key, String value) { values.put(key, value); }
    protected String getSpi(String key) { return values.get(key); }
    protected void removeSpi(String key) { values.remove(key); }
    protected void removeNodeSpi() { values.clear(); children.clear(); }
    protected String[] keysSpi() { return values.keySet().toArray(String[]::new); }
    protected String[] childrenNamesSpi() { return children.keySet().toArray(String[]::new); }
    protected AbstractPreferences childSpi(String name) { return children.computeIfAbsent(name, n -> new MemoryPreferences(this, n)); }
    protected void syncSpi() {}
    protected void flushSpi() {}
}
