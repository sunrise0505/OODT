package com.oodt.fruitguard.core;

import com.oodt.fruitguard.model.EnemyKind;
import com.oodt.fruitguard.model.Vec2;

import java.util.List;

/** 关卡仓库：怪物路线与波次数据和控制逻辑分离。 */
public final class LevelRepository {
    private LevelRepository() {}

    public static List<LevelConfig> all() {
        return List.of(level1(), level2(), level3(), level4(), level5(), level6(), level7(), level8());
    }

    private static LevelConfig level1() {
        return new LevelConfig(1, "青苹果原野", "短路线 · 新手练习",
                List.of(p(-40, 300), p(135, 300), p(280, 345), p(455, 330),
                        p(620, 285), p(760, 325), p(920, 310)),
                List.of(
                        wave(e(EnemyKind.SLIME, 8, .78)),
                        wave(e(EnemyKind.SLIME, 7, .64), e(EnemyKind.FOX, 5, .75)),
                        wave(e(EnemyKind.BOAR, 4, .95), e(EnemyKind.FOX, 5, .65), e(EnemyKind.BOSS, 1, 1.0))
                ), 290, 1.0);
    }

    private static LevelConfig level2() {
        return new LevelConfig(2, "葡萄峡谷", "双折线路 · 中等难度",
                List.of(p(-40, 245), p(150, 275), p(300, 365), p(480, 335),
                        p(615, 255), p(770, 300), p(920, 350)),
                List.of(
                        wave(e(EnemyKind.SLIME, 10, .62), e(EnemyKind.FOX, 4, .7)),
                        wave(e(EnemyKind.FOX, 11, .46), e(EnemyKind.BOAR, 4, .9)),
                        wave(e(EnemyKind.BOAR, 8, .7), e(EnemyKind.SLIME, 8, .42)),
                        wave(e(EnemyKind.FOX, 10, .35), e(EnemyKind.BOAR, 6, .62), e(EnemyKind.BOSS, 1, 1.0))
                ), 310, 1.20);
    }

    private static LevelConfig level3() {
        return new LevelConfig(3, "西瓜火山", "重甲推进 · 贯穿练习",
                List.of(p(-40, 360), p(130, 330), p(285, 255), p(455, 290),
                        p(585, 375), p(735, 350), p(920, 285)),
                List.of(
                        wave(e(EnemyKind.FOX, 12, .42), e(EnemyKind.SLIME, 10, .48)),
                        wave(e(EnemyKind.BOAR, 9, .64), e(EnemyKind.FOX, 10, .38)),
                        wave(e(EnemyKind.BOAR, 11, .55), e(EnemyKind.SLIME, 14, .34)),
                        wave(e(EnemyKind.SLIME, 16, .32), e(EnemyKind.FOX, 8, .45)),
                        wave(e(EnemyKind.FOX, 14, .28), e(EnemyKind.BOAR, 9, .5), e(EnemyKind.BOSS, 2, 1.2))
                ), 340, 1.42);
    }

    private static LevelConfig level4() {
        return new LevelConfig(4, "草莓花径", "密集短波 · 连锁清场",
                List.of(p(-40, 255), p(130, 270), p(290, 330), p(450, 260),
                        p(600, 315), p(760, 255), p(920, 315)),
                List.of(
                        wave(e(EnemyKind.SLIME, 18, .30)),
                        wave(e(EnemyKind.FOX, 14, .30), e(EnemyKind.SLIME, 12, .25)),
                        wave(e(EnemyKind.SLIME, 22, .22), e(EnemyKind.BOAR, 7, .55)),
                        wave(e(EnemyKind.FOX, 16, .26), e(EnemyKind.BOAR, 8, .48), e(EnemyKind.BOSS, 1, 1.0))
                ), 390, 1.48);
    }

    private static LevelConfig level5() {
        return new LevelConfig(5, "冰蓝湖畔", "疾风突袭 · 霜爆控场",
                List.of(p(-40, 335), p(125, 290), p(265, 250), p(420, 315),
                        p(580, 250), p(740, 310), p(920, 260)),
                List.of(
                        wave(e(EnemyKind.FOX, 14, .38)),
                        wave(e(EnemyKind.SLIME, 18, .30), e(EnemyKind.FOX, 10, .32)),
                        wave(e(EnemyKind.FOX, 20, .27), e(EnemyKind.BOAR, 5, .60)),
                        wave(e(EnemyKind.BOAR, 9, .55), e(EnemyKind.FOX, 14, .28)),
                        wave(e(EnemyKind.SLIME, 24, .23), e(EnemyKind.BOSS, 1, 1.0)),
                        wave(e(EnemyKind.FOX, 22, .24), e(EnemyKind.BOAR, 10, .48), e(EnemyKind.BOSS, 2, 1.2))
                ), 440, 1.60);
    }

    private static LevelConfig level6() {
        return new LevelConfig(6, "菠萝古堡", "重甲方阵 · 破阵攻坚",
                List.of(p(-40, 285), p(150, 285), p(335, 285), p(500, 285),
                        p(650, 325), p(780, 325), p(920, 325)),
                List.of(
                        wave(e(EnemyKind.BOAR, 12, .48)),
                        wave(e(EnemyKind.SLIME, 16, .25), e(EnemyKind.BOAR, 14, .44)),
                        wave(e(EnemyKind.BOAR, 16, .40), e(EnemyKind.BOSS, 1, 1.0)),
                        wave(e(EnemyKind.FOX, 18, .25), e(EnemyKind.BOAR, 16, .38)),
                        wave(e(EnemyKind.BOAR, 20, .35), e(EnemyKind.BOSS, 3, 1.4))
                ), 500, 1.72);
    }

    private static LevelConfig level7() {
        return new LevelConfig(7, "蜜桃云境", "七波持久战 · 守护复苏",
                List.of(p(-40, 260), p(135, 350), p(290, 290), p(450, 235),
                        p(600, 325), p(750, 350), p(920, 280)),
                List.of(
                        wave(e(EnemyKind.SLIME, 18, .30), e(EnemyKind.FOX, 8, .35)),
                        wave(e(EnemyKind.BOAR, 12, .48), e(EnemyKind.FOX, 14, .28)),
                        wave(e(EnemyKind.SLIME, 28, .21), e(EnemyKind.BOSS, 1, 1.0)),
                        wave(e(EnemyKind.FOX, 24, .24), e(EnemyKind.BOAR, 8, .42)),
                        wave(e(EnemyKind.BOAR, 18, .38), e(EnemyKind.BOSS, 1, 1.0)),
                        wave(e(EnemyKind.FOX, 20, .22), e(EnemyKind.SLIME, 22, .20)),
                        wave(e(EnemyKind.BOAR, 18, .36), e(EnemyKind.FOX, 16, .24), e(EnemyKind.BOSS, 3, 1.3))
                ), 560, 1.84);
    }

    private static LevelConfig level8() {
        return new LevelConfig(8, "丰收王庭", "八波终局 · 全员集结",
                List.of(p(-40, 330), p(130, 245), p(285, 340), p(450, 260),
                        p(610, 345), p(765, 265), p(920, 325)),
                List.of(
                        wave(e(EnemyKind.FOX, 20, .28), e(EnemyKind.SLIME, 16, .25)),
                        wave(e(EnemyKind.BOAR, 16, .42), e(EnemyKind.FOX, 12, .25)),
                        wave(e(EnemyKind.SLIME, 30, .20), e(EnemyKind.BOSS, 1, 1.0)),
                        wave(e(EnemyKind.FOX, 28, .22), e(EnemyKind.BOAR, 10, .38)),
                        wave(e(EnemyKind.BOAR, 20, .34), e(EnemyKind.BOSS, 2, 1.1)),
                        wave(e(EnemyKind.SLIME, 32, .18), e(EnemyKind.FOX, 20, .22)),
                        wave(e(EnemyKind.BOAR, 22, .32), e(EnemyKind.FOX, 20, .22)),
                        wave(e(EnemyKind.FOX, 24, .20), e(EnemyKind.BOAR, 20, .30), e(EnemyKind.BOSS, 4, 1.5))
                ), 640, 2.0);
    }

    private static Vec2 p(double x, double y) { return new Vec2(x, y); }
    private static WaveEntry e(EnemyKind kind, int count, double interval) { return new WaveEntry(kind, count, interval); }
    private static WaveConfig wave(WaveEntry... entries) { return new WaveConfig(List.of(entries)); }
}
