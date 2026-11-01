package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.*;

import java.util.*;

/** 游戏门面：统一协调手动瞄准、武器、波次、积分和状态转换。 */
public final class GameController {
    public static final Vec2 LAUNCHER_POSITION = new Vec2(450, 485);

    private final List<LevelConfig> levels = LevelRepository.all();
    private final SaveManager saveManager;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final EnumMap<FruitKind, FruitWeapon> weapons = new EnumMap<>(FruitKind.class);
    private final EnumSet<EnemyKind> introducedKinds = EnumSet.noneOf(EnemyKind.class);
    private final ArrayDeque<CombatFeedback> feedback = new ArrayDeque<>();
    private BattleStatistics statistics = new BattleStatistics();

    private GameState state = GameState.MENU;
    private LevelConfig level;
    private ScoreManager scoreManager;
    private int lives;
    private int nextWaveIndex;
    private int entryIndex;
    private int remainingInEntry;
    private double spawnTimer;
    private double betweenWaveTimer;
    private boolean spawningWave;
    private int activeWaveNumber;
    private int clearedWaves;
    private FruitKind selectedKind = FruitKind.ORANGE;
    private FruitKind ultimateVisualKind;
    private double ultimateFlash;
    private EnemyKind introKind;
    private double introRemaining;
    private Vec2 lastAimPoint = new Vec2(520, 300);
    private String message = "准备进入果园";

    public GameController() {
        this(new SaveManager());
    }

    GameController(SaveManager saveManager) {
        this.saveManager = saveManager;
        resetWeapons();
    }

    private void resetWeapons() {
        weapons.clear();
        for (FruitKind kind : FruitKind.values()) weapons.put(kind, FruitWeaponFactory.create(kind));
    }

    public void startLevel(int levelNumber) {
        if (levelNumber < 1 || levelNumber > levels.size() || levelNumber > saveManager.unlockedLevel()) {
            message = "请先通关前一关";
            return;
        }
        level = levels.get(levelNumber - 1);
        scoreManager = new ScoreManager(level.startingCoins());
        enemies.clear();
        projectiles.clear();
        introducedKinds.clear();
        feedback.clear();
        statistics = new BattleStatistics();
        resetWeapons();
        selectedKind = FruitKind.ORANGE;
        lives = 8;
        nextWaveIndex = 0;
        entryIndex = 0;
        remainingInEntry = 0;
        spawnTimer = 0;
        betweenWaveTimer = 1.5;
        spawningWave = false;
        activeWaveNumber = 0;
        clearedWaves = 0;
        ultimateFlash = 0;
        ultimateVisualKind = null;
        introKind = null;
        introRemaining = 0;
        lastAimPoint = new Vec2(450, 280);
        state = GameState.RUNNING;
        message = "移动准星并点击怪物，发射橙子弹！";
    }

    public void update(double deltaSeconds) {
        if (!Double.isFinite(deltaSeconds) || deltaSeconds <= 0) return;
        double remaining = Math.min(deltaSeconds, .25);
        while (remaining > 1e-9 && state == GameState.RUNNING) {
            double step = Math.min(remaining, .05);
            updateStep(step);
            remaining -= step;
        }
    }

    private void updateStep(double dt) {
        if (state != GameState.RUNNING || level == null) return;
        statistics.advance(dt);
        if (ultimateFlash > 0) ultimateFlash -= dt;
        if (introRemaining > 0) {
            introRemaining -= dt;
            if (introRemaining <= 0) introKind = null;
        }
        weapons.values().forEach(weapon -> weapon.update(dt));
        updateWave(dt);

        Map<Enemy, Double> previousHealth = healthSnapshot();
        for (Enemy enemy : enemies) enemy.update(dt);
        for (Projectile projectile : projectiles) {
            boolean hitBefore = projectile.hasHit();
            projectile.update(dt, enemies);
            if (!hitBefore && projectile.hasHit()) statistics.recordHit();
        }
        captureDamage(previousHealth);
        projectiles.removeIf(projectile -> !projectile.isAlive());

        List<Enemy> escaped = enemies.stream().filter(Enemy::reachedEnd).toList();
        if (!escaped.isEmpty()) {
            lives -= escaped.stream().mapToInt(enemy -> enemy.kind() == EnemyKind.BOSS ? 3 : 1).sum();
            enemies.removeAll(escaped);
            for (Enemy enemy : escaped) emit(new CombatFeedback(enemy.position(), "果仓 -"
                    + (enemy.kind() == EnemyKind.BOSS ? 3 : 1), CombatFeedback.Type.ESCAPE, selectedKind));
            message = "有怪物冲进果仓，生命值下降！";
        }

        for (Enemy enemy : enemies) {
            if (enemy.claimReward()) {
                scoreManager.reward(enemy);
                statistics.recordKill();
                emit(new CombatFeedback(enemy.position(), "+" + enemy.scoreReward() + " 分  +"
                        + enemy.coinReward() + " 金币", CombatFeedback.Type.REWARD, selectedKind));
            }
        }
        enemies.removeIf(Enemy::isDead);
        if (!spawningWave && enemies.isEmpty()) clearedWaves = nextWaveIndex;

        if (lives <= 0) finish(false);
        else if (!spawningWave && nextWaveIndex >= level.waves().size() && enemies.isEmpty()) finish(true);
    }

    private void updateWave(double dt) {
        if (!spawningWave) {
            if (!enemies.isEmpty() || nextWaveIndex >= level.waves().size()) return;
            betweenWaveTimer -= dt;
            if (betweenWaveTimer <= 0) beginWave();
            return;
        }

        spawnTimer -= dt;
        if (spawnTimer > 0) return;
        WaveConfig wave = level.waves().get(nextWaveIndex);
        WaveEntry entry = wave.entries().get(entryIndex);
        Enemy enemy = EnemyFactory.create(entry.kind(), level.path(), level.difficultyScale());
        enemies.add(enemy);
        announceIfNew(entry.kind());
        remainingInEntry--;
        spawnTimer = entry.intervalSeconds();
        if (remainingInEntry <= 0) {
            entryIndex++;
            if (entryIndex >= wave.entries().size()) {
                spawningWave = false;
                nextWaveIndex++;
                betweenWaveTimer = 2.2;
            } else {
                remainingInEntry = wave.entries().get(entryIndex).count();
                spawnTimer = Math.max(spawnTimer, 0.5);
            }
        }
    }

    private void announceIfNew(EnemyKind kind) {
        if (introducedKinds.add(kind)) {
            introKind = kind;
            introRemaining = 3.2;
            message = "新敌人出现：" + kind.displayName() + " — " + kind.description();
        }
    }

    private void beginWave() {
        spawningWave = true;
        activeWaveNumber = nextWaveIndex + 1;
        entryIndex = 0;
        remainingInEntry = level.waves().get(nextWaveIndex).entries().getFirst().count();
        spawnTimer = 0;
        message = "第 " + (nextWaveIndex + 1) + " 波来袭，瞄准怪物开火！";
    }

    public boolean shootAt(Vec2 click) {
        return fireAt(click, false, 0);
    }

    public boolean shootSlingAt(Vec2 aim, double power) {
        return fireAt(aim, true, power);
    }

    private boolean fireAt(Vec2 click, boolean sling, double power) {
        if (state != GameState.RUNNING) return false;
        if (!Double.isFinite(click.x()) || !Double.isFinite(click.y()) || !Double.isFinite(power)) return false;
        lastAimPoint = click;
        Enemy target = sling ? null : enemies.stream()
                .filter(enemy -> !enemy.isDead() && !enemy.reachedEnd())
                .filter(enemy -> enemy.position().distanceTo(click) <= enemy.radius() + 34)
                .min(Comparator.comparingDouble(enemy -> enemy.position().distanceTo(click)))
                .orElse(null);
        Vec2 aimPoint = target == null ? click : target.position();
        FruitWeapon weapon = selectedWeapon();
        Optional<Projectile> shot = weapon.fire(LAUNCHER_POSITION, aimPoint, target);
        if (shot.isEmpty()) {
            message = weapon.kind().displayName() + "正在装填";
            return false;
        }
        projectiles.add(sling ? shot.get().charged(power) : shot.get());
        statistics.recordShot();
        message = target == null
                ? "已向准星位置发射，注意预判怪物移动"
                : "锁定 " + target.kind().displayName() + "，发射！";
        return true;
    }

    public boolean upgradeSelectedWeapon() {
        if (state != GameState.RUNNING || scoreManager == null) return false;
        FruitWeapon weapon = selectedWeapon();
        if (weapon.level() >= 3) {
            message = weapon.kind().displayName() + "已达到最高等级";
            return false;
        }
        int cost = weapon.upgradeCost();
        if (!scoreManager.spendCoins(cost)) {
            message = "金币不足，需要 " + cost + " 枚金币";
            return false;
        }
        weapon.upgrade();
        message = weapon.kind().displayName() + "升级到 Lv." + weapon.level();
        return true;
    }

    public boolean useUltimate() {
        if (state != GameState.RUNNING || enemies.stream().noneMatch(enemy -> !enemy.isDead() && !enemy.reachedEnd())) {
            message = "场上没有可攻击的怪物";
            return false;
        }
        if (!scoreManager.consumeUltimate()) {
            message = "专属大招能量尚未充满";
            return false;
        }

        Map<Enemy, Double> previousHealth = healthSnapshot();
        switch (selectedKind) {
            case ORANGE -> orangeUltimate();
            case GRAPE -> grapeUltimate();
            case WATERMELON -> watermelonUltimate();
            case LEMON -> lemonUltimate();
            case STRAWBERRY -> strawberryUltimate();
            case PINEAPPLE -> pineappleUltimate();
            case BLUEBERRY -> blueberryUltimate();
            case PEACH -> peachUltimate();
        }
        captureDamage(previousHealth);
        ultimateVisualKind = selectedKind;
        ultimateFlash = 0.85;
        message = selectedKind.ultimateName() + "！" + selectedKind.ultimateDescription();
        return true;
    }

    private void orangeUltimate() {
        List<Enemy> targets = enemies.stream()
                .filter(enemy -> !enemy.isDead())
                .sorted(Comparator.comparingDouble(Enemy::routeProgress).reversed())
                .limit(3).toList();
        double damage = 220;
        for (int i = 0; i < targets.size(); i++) targets.get(i).takeDamage(damage - i * 45);
    }

    private void grapeUltimate() {
        for (Enemy enemy : enemies) enemy.takeDamage(enemy.kind() == EnemyKind.BOSS ? 95 : 125);
    }

    private void watermelonUltimate() {
        Vec2 center = densestEnemyPosition(220);
        for (Enemy enemy : enemies) {
            if (enemy.position().distanceTo(center) <= 220) {
                enemy.takeDamage(enemy.kind() == EnemyKind.BOSS ? 230 : 320);
            }
        }
    }

    private void lemonUltimate() {
        for (Enemy enemy : enemies) {
            enemy.takeDamage(70);
            if (!enemy.isDead()) enemy.slow(0.28, 6.5);
        }
    }

    private void strawberryUltimate() {
        List<Enemy> targets = enemies.stream().filter(enemy -> !enemy.isDead())
                .sorted(Comparator.comparingDouble(Enemy::routeProgress).reversed()).limit(5).toList();
        for (int i = 0; i < targets.size(); i++) targets.get(i).takeDamage(230 - i * 25);
    }

    private void pineappleUltimate() {
        for (Enemy enemy : enemies) {
            boolean armored = enemy.kind() == EnemyKind.BOAR || enemy.kind() == EnemyKind.BOSS;
            enemy.takeDamage(armored ? 380 : 80);
        }
    }

    private Vec2 densestEnemyPosition(double radius) {
        return enemies.stream().filter(enemy -> !enemy.isDead())
                .max(Comparator.comparingLong(candidate -> enemies.stream()
                        .filter(enemy -> !enemy.isDead() && enemy.position().distanceTo(candidate.position()) <= radius)
                        .count())).orElseThrow().position();
    }

    private void blueberryUltimate() {
        Vec2 center = densestEnemyPosition(200);
        for (Enemy enemy : enemies) {
            if (enemy.position().distanceTo(center) <= 200) {
                enemy.takeDamage(210);
                if (!enemy.isDead()) enemy.slow(.28, 5);
            }
        }
    }

    private void peachUltimate() {
        int before = lives;
        lives = Math.min(8, lives + 2);
        emit(new CombatFeedback(LAUNCHER_POSITION, "生命 +" + (lives - before), CombatFeedback.Type.HEAL, selectedKind));
        for (Enemy enemy : enemies) enemy.takeDamage(95);
    }

    public boolean useSpecialAttack(SpecialAttack attack) {
        if (state != GameState.RUNNING || scoreManager == null) return false;
        if (attack == SpecialAttack.REPAIR && lives >= 8) {
            message = "果仓生命已满，无需修复";
            return false;
        }
        if (attack != SpecialAttack.REPAIR && enemies.stream().noneMatch(enemy -> !enemy.isDead() && !enemy.reachedEnd())) {
            message = "场上没有可攻击的怪物";
            return false;
        }
        if (!scoreManager.spendTacticalPoints(attack.cost())) {
            message = "可用积分不足，需要 " + attack.cost() + " 分";
            return false;
        }
        Map<Enemy, Double> previousHealth = healthSnapshot();
        switch (attack) {
            case FROST -> enemies.stream().filter(enemy -> !enemy.isDead()).forEach(enemy -> enemy.slow(.25, 4));
            case BOMB -> enemies.stream().filter(enemy -> enemy.position().distanceTo(lastAimPoint) <= 145)
                    .forEach(enemy -> enemy.takeDamage(260));
            case REPAIR -> lives = Math.min(8, lives + 2);
        }
        captureDamage(previousHealth);
        emit(new CombatFeedback(attack == SpecialAttack.BOMB ? lastAimPoint : LAUNCHER_POSITION,
                attack.displayName(), attack == SpecialAttack.REPAIR ? CombatFeedback.Type.HEAL : CombatFeedback.Type.SPECIAL,
                selectedKind));
        message = attack.displayName() + "已使用，消耗 " + attack.cost() + " 可用积分";
        return true;
    }

    private Map<Enemy, Double> healthSnapshot() {
        Map<Enemy, Double> snapshot = new IdentityHashMap<>();
        for (Enemy enemy : enemies) snapshot.put(enemy, enemy.health());
        return snapshot;
    }

    private void captureDamage(Map<Enemy, Double> previous) {
        previous.forEach((enemy, health) -> {
            double damage = health - enemy.health();
            if (damage > .001) emit(new CombatFeedback(enemy.position(), "−" + Math.round(damage),
                    CombatFeedback.Type.DAMAGE, selectedKind));
        });
    }

    private void emit(CombatFeedback event) {
        if (feedback.size() >= 256) feedback.removeFirst();
        feedback.addLast(event);
    }

    public List<CombatFeedback> drainFeedback() {
        List<CombatFeedback> events = List.copyOf(feedback);
        feedback.clear();
        return events;
    }

    private void finish(boolean victory) {
        state = victory ? GameState.VICTORY : GameState.DEFEAT;
        saveManager.recordResult(level.number(), scoreManager.score(), victory,
                victory ? BattleStatistics.starsForLives(lives) : 0);
        message = victory ? (level.number() == levels.size() ? "八片果园全部守护成功！" : "守护成功！新的果园已经解锁")
                : "水果被偷光了，再试一次吧";
    }

    public void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            message = "游戏已暂停";
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
            message = "继续瞄准怪物";
        }
    }

    public void returnToMenu() {
        state = GameState.MENU;
        enemies.clear();
        projectiles.clear();
        feedback.clear();
        message = "准备进入果园";
    }

    public void selectKind(FruitKind kind) {
        selectedKind = kind;
        message = "已装备 " + kind.displayName() + " — " + kind.description();
    }

    public int displayedWave() {
        if (level == null) return 0;
        return Math.max(1, activeWaveNumber);
    }

    public void aimAt(Vec2 point) {
        if (state == GameState.RUNNING && Double.isFinite(point.x()) && Double.isFinite(point.y())) lastAimPoint = point;
    }

    public double waveCountdown() { return level != null && !spawningWave && enemies.isEmpty() && nextWaveIndex < level.waves().size()
            ? Math.max(0, betweenWaveTimer) : 0; }
    public int clearedWaves() { return clearedWaves; }
    public int remainingEnemies() {
        int count = (int) enemies.stream().filter(enemy -> !enemy.isDead()).count();
        if (spawningWave) {
            count += remainingInEntry;
            for (int i = entryIndex + 1; i < level.waves().get(nextWaveIndex).entries().size(); i++)
                count += level.waves().get(nextWaveIndex).entries().get(i).count();
        }
        return count;
    }

    public List<LevelConfig> levels() { return levels; }
    public List<Enemy> enemies() { return List.copyOf(enemies); }
    public List<Projectile> projectiles() { return List.copyOf(projectiles); }
    public Collection<FruitWeapon> weapons() { return List.copyOf(weapons.values()); }
    public FruitWeapon weapon(FruitKind kind) { return weapons.get(kind); }
    public FruitWeapon selectedWeapon() { return weapons.get(selectedKind); }
    public GameState state() { return state; }
    public LevelConfig level() { return level; }
    public ScoreManager scoreManager() { return scoreManager; }
    public int lives() { return lives; }
    public FruitKind selectedKind() { return selectedKind; }
    public String message() { return message; }
    public int unlockedLevel() { return saveManager.unlockedLevel(); }
    public int highScore() { return saveManager.highScore(); }
    public int levelStars(int number) { return saveManager.stars(number); }
    public int levelBestScore(int number) { return saveManager.bestScore(number); }
    public BattleStatistics statistics() { return statistics; }
    public boolean ultimateFlashing() { return ultimateFlash > 0; }
    public FruitKind ultimateVisualKind() { return ultimateVisualKind; }
    public EnemyKind introKind() { return introKind; }
    public double introProgress() { return Math.max(0, introRemaining / 3.2); }
    public Vec2 lastAimPoint() { return lastAimPoint; }
}
