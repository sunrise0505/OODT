package com.oodt.fruitguard.ui;

import com.oodt.fruitguard.core.GameController;
import com.oodt.fruitguard.model.*;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.*;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.ArrayList;

/** 手动瞄准战场：绘制原创背景、角色、怪物、弹道和准星。 */
public final class GameCanvas extends Canvas {
    public static final double WIDTH = 900;
    public static final double HEIGHT = 560;

    private final GameController controller;
    private final Image battleBackground = image("battle-orchard.png");
    private final Image heroAtlas = image("fruit-heroes-atlas.png");
    private final Image extraHeroAtlas = image("fruit-heroes-extra-atlas.png");
    private final Image monsterAtlas = image("monsters-atlas.png");
    private Vec2 cursor = new Vec2(520, 300);
    private double elapsed;
    private double recoil;
    private boolean slingMode;
    private boolean holding;
    private boolean pulling;
    private Vec2 pullPoint = GameController.LAUNCHER_POSITION;
    private final List<FloatingEffect> effects = new ArrayList<>();

    private static final class FloatingEffect {
        final CombatFeedback event;
        double age;
        FloatingEffect(CombatFeedback event) { this.event = event; }
    }

    public GameCanvas(GameController controller) {
        super(WIDTH, HEIGHT);
        this.controller = controller;
        setFocusTraversable(true);
        setOnMouseMoved(event -> aim(new Vec2(event.getX(), event.getY())));
        setOnMouseDragged(event -> {
            aim(new Vec2(event.getX(), event.getY()));
            if (pulling) updatePull(cursor);
        });
        setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY || controller.state() != GameState.RUNNING) return;
            aim(new Vec2(event.getX(), event.getY()));
            if (slingMode) {
                pulling = cursor.distanceTo(GameController.LAUNCHER_POSITION) <= 105;
                if (pulling) updatePull(cursor);
            } else {
                holding = true;
                if (controller.shootAt(cursor)) recoil = 1;
            }
            requestFocus();
        });
        setOnMouseReleased(event -> {
            if (event.getButton() != MouseButton.PRIMARY) return;
            if (pulling && pullDistance() >= 12 && controller.shootSlingAt(slingAim(), pullDistance() / 105)) recoil = 1;
            cancelInput();
        });
        setOnMouseExited(event -> holding = false);
    }

    private void aim(Vec2 point) { cursor = point; controller.aimAt(point); }
    private void updatePull(Vec2 point) {
        Vec2 start = GameController.LAUNCHER_POSITION;
        double distance = start.distanceTo(point);
        pullPoint = distance > 105 ? start.interpolate(point, 105 / distance) : point;
        controller.aimAt(slingAim());
    }
    private double pullDistance() { return GameController.LAUNCHER_POSITION.distanceTo(pullPoint); }
    private Vec2 slingAim() {
        Vec2 start = GameController.LAUNCHER_POSITION;
        return new Vec2(Math.clamp(start.x() + (start.x() - pullPoint.x()) * 4.5, 10, WIDTH - 10),
                Math.clamp(start.y() + (start.y() - pullPoint.y()) * 4.5, 25, HEIGHT - 10));
    }
    public void updateInput() {
        if (controller.state() != GameState.RUNNING) { cancelInput(); return; }
        if (holding && !slingMode && controller.selectedWeapon().ready() && controller.shootAt(cursor)) recoil = 1;
    }
    public void cancelInput() { holding = false; pulling = false; }
    public void setSlingMode(boolean value) { slingMode = value; cancelInput(); }
    public boolean slingMode() { return slingMode; }
    public void resetEffects() { effects.clear(); cancelInput(); }

    private static Image image(String name) {
        return new Image(GameCanvas.class.getResourceAsStream("/com/oodt/fruitguard/assets/" + name));
    }

    public void render(double deltaSeconds) {
        elapsed += deltaSeconds;
        recoil = Math.max(0, recoil - deltaSeconds * 5.5);
        GraphicsContext g = getGraphicsContext2D();
        drawBackground(g);
        if (controller.level() == null) return;
        drawRouteGuide(g, controller.level().path());
        drawProjectiles(g);
        drawEnemies(g);
        drawFruitHero(g);
        drawAimGuide(g);
        drawBattleProgress(g);
        drawHoveredEnemy(g);
        drawEnemyIntroduction(g);
        drawFeedback(g, deltaSeconds);
        drawUltimateEffect(g);
    }

    private void drawBackground(GraphicsContext g) {
        g.drawImage(battleBackground, 0, 0, WIDTH, HEIGHT);
        Color tint = switch (controller.level() == null ? 1 : controller.level().number()) {
            case 2 -> Color.rgb(87, 52, 120, .10);
            case 3 -> Color.rgb(185, 74, 36, .13);
            case 4 -> Color.rgb(235, 95, 140, .12);
            case 5 -> Color.rgb(75, 145, 225, .15);
            case 6 -> Color.rgb(215, 165, 40, .14);
            case 7 -> Color.rgb(235, 153, 176, .14);
            case 8 -> Color.rgb(244, 181, 52, .17);
            default -> Color.rgb(29, 113, 68, .04);
        };
        g.setFill(tint);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(9, 30, 22, .28)),
                new Stop(.25, Color.TRANSPARENT),
                new Stop(1, Color.rgb(10, 23, 15, .28))));
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawRouteGuide(GraphicsContext g, List<Vec2> path) {
        g.setLineCap(StrokeLineCap.ROUND);
        g.setStroke(Color.rgb(255, 244, 183, .38));
        g.setLineWidth(3);
        g.setLineDashes(8, 16);
        g.beginPath();
        g.moveTo(path.getFirst().x(), path.getFirst().y());
        for (int i = 1; i < path.size(); i++) g.lineTo(path.get(i).x(), path.get(i).y());
        g.stroke();
        g.setLineDashes();

        Vec2 end = path.getLast();
        g.setFill(Color.rgb(255, 225, 103, .18 + Math.sin(elapsed * 3) * .04));
        g.fillOval(end.x() - 35, end.y() - 35, 70, 70);
        g.setStroke(Color.web("#ffe184"));
        g.setLineWidth(2);
        g.strokeOval(end.x() - 24, end.y() - 24, 48, 48);
    }

    private void drawFruitHero(GraphicsContext g) {
        FruitKind kind = controller.selectedKind();
        Image atlas = kind.atlasIndex() < 4 ? heroAtlas : extraHeroAtlas;
        double cell = atlas.getWidth() / 2.0;
        double sx = (kind.atlasCell() % 2) * cell;
        double sy = (kind.atlasCell() / 2) * cell;
        double bob = Math.sin(elapsed * 2.6) * 2;
        double kick = recoil * 8;
        Vec2 launch = GameController.LAUNCHER_POSITION;
        double left = launch.x() - 112;

        g.setFill(Color.rgb(17, 45, 27, .68));
        g.fillRoundRect(left, 395, 224, 153, 28, 28);
        g.setStroke(Color.rgb(255, 229, 137, .72));
        g.setLineWidth(2);
        g.strokeRoundRect(left, 395, 224, 153, 28, 28);
        g.setGlobalAlpha(.98);
        g.drawImage(atlas, sx, sy, cell, cell, launch.x() - 83, 386 + bob + kick, 166, 156);
        g.setGlobalAlpha(1);

        g.setFill(Color.rgb(14, 40, 25, .86));
        g.fillRoundRect(launch.x() - 92, 514, 184, 28, 14, 14);
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("System", FontWeight.BOLD, 13));
        g.fillText(kind.displayName() + "  Lv." + controller.selectedWeapon().level(), launch.x(), 528);
        g.setFill(kind.color());
        g.fillOval(launch.x() - 5, launch.y() - 5, 10, 10);
    }

    private void drawAimGuide(GraphicsContext g) {
        if (controller.state() != GameState.RUNNING) return;
        Vec2 start = GameController.LAUNCHER_POSITION;
        Enemy hovered = slingMode ? null : hoveredEnemy();
        Vec2 aim = pulling ? slingAim() : (hovered == null ? cursor : hovered.position());
        Color color = hovered == null ? Color.rgb(255, 255, 255, .48) : Color.rgb(255, 222, 91, .9);

        g.setStroke(color);
        g.setLineWidth(1.5);
        g.setLineDashes(6, 10);
        g.strokeLine(start.x(), start.y(), aim.x(), aim.y());
        g.setLineDashes();

        double pulse = 2 + Math.sin(elapsed * 5) * 2;
        double radius = (hovered == null ? 17 : hovered.radius() + 14) + pulse;
        g.setLineWidth(2.5);
        g.strokeOval(aim.x() - radius, aim.y() - radius, radius * 2, radius * 2);
        g.strokeLine(aim.x() - radius - 8, aim.y(), aim.x() - radius / 2, aim.y());
        g.strokeLine(aim.x() + radius / 2, aim.y(), aim.x() + radius + 8, aim.y());
        g.strokeLine(aim.x(), aim.y() - radius - 8, aim.x(), aim.y() - radius / 2);
        g.strokeLine(aim.x(), aim.y() + radius / 2, aim.x(), aim.y() + radius + 8);
        if (hovered != null) {
            g.setFill(Color.web("#ffe65f"));
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 12));
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText("已锁定", aim.x(), aim.y() - radius - 13);
        }
        if (slingMode) {
            g.setStroke(Color.web("#ffd66d"));
            g.setLineWidth(4);
            Vec2 pouch = pulling ? pullPoint : start;
            g.strokeLine(start.x() - 42, start.y() - 28, pouch.x(), pouch.y());
            g.strokeLine(start.x() + 42, start.y() - 28, pouch.x(), pouch.y());
            g.setFill(controller.selectedKind().color());
            g.fillOval(pouch.x() - 10, pouch.y() - 10, 20, 20);
            g.setFont(Font.font("System", FontWeight.BOLD, 12));
            g.setFill(Color.web("#fff2c3"));
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText(pulling ? "蓄力 " + Math.round(pullDistance() / 105 * 100) + "% · 松手发射"
                    : "从中央拖动弹弓，松手发射", start.x(), 382);
        }
    }

    private void drawBattleProgress(GraphicsContext g) {
        g.setFill(Color.rgb(13, 36, 25, .83));
        g.fillRoundRect(16, 16, 214, 69, 16, 16);
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.TOP);
        g.setFont(Font.font("System", FontWeight.BOLD, 14));
        g.setFill(Color.web("#ffe49a"));
        String line = controller.waveCountdown() > 0
                ? "下一波 " + (controller.clearedWaves() + 1) + " · " + String.format("%.1f", controller.waveCountdown()) + " 秒"
                : "第 " + controller.displayedWave() + " 波 · 剩余 " + controller.remainingEnemies() + " 只";
        g.fillText(line, 29, 28);
        g.setFont(Font.font("System", 11));
        g.setFill(Color.web("#d6e7d5"));
        g.fillText("已击败 " + controller.statistics().kills() + " 只 · "
                + (slingMode ? "弹弓蓄力模式" : "点击 / 长按连发"), 29, 54);
        g.setFill(Color.rgb(255, 255, 255, .18));
        g.fillRoundRect(29, 72, 185, 4, 4, 4);
        g.setFill(Color.web("#f5ce63"));
        g.fillRoundRect(29, 72, 185.0 * controller.clearedWaves() / controller.level().waves().size(), 4, 4, 4);
    }

    private void drawHoveredEnemy(GraphicsContext g) {
        if (controller.state() != GameState.RUNNING || pulling) return;
        Enemy enemy = hoveredEnemy();
        if (enemy == null || enemy.isDead()) return;
        double x = Math.clamp(cursor.x() + 26, 12, WIDTH - 238);
        double y = Math.clamp(cursor.y() + 30, 96, HEIGHT - 95);
        g.setFill(Color.rgb(12, 33, 22, .94));
        g.fillRoundRect(x, y, 224, 64, 14, 14);
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.TOP);
        g.setFont(Font.font("System", FontWeight.BOLD, 14));
        g.setFill(Color.web("#ffe39c"));
        g.fillText(enemy.kind().displayName(), x + 12, y + 9);
        g.setFont(Font.font("System", 11));
        g.setFill(Color.WHITE);
        g.fillText("血量 " + (int) Math.ceil(enemy.health()) + "/" + (int) Math.ceil(enemy.maxHealth())
                + " · 速度 " + Math.round(enemy.currentSpeed()) + " px/s", x + 12, y + 32);
        g.fillText("击败获得 " + enemy.scoreReward() + " 分", x + 12, y + 47);
    }

    private void drawFeedback(GraphicsContext g, double dt) {
        for (CombatFeedback event : controller.drainFeedback()) effects.add(new FloatingEffect(event));
        if (effects.size() > 90) effects.subList(0, effects.size() - 90).clear();
        effects.forEach(effect -> effect.age += dt);
        effects.removeIf(effect -> effect.age > 1.15);
        for (FloatingEffect effect : effects) {
            CombatFeedback event = effect.event;
            Color color = switch (event.type()) {
                case DAMAGE -> Color.WHITE;
                case REWARD -> Color.web("#ffe178");
                case ESCAPE -> Color.web("#ff8880");
                case HEAL -> Color.web("#a7ffbf");
                case SPECIAL -> Color.web("#a5eaff");
            };
            double alpha = Math.min(1, (1.15 - effect.age) * 2);
            g.setGlobalAlpha(alpha);
            Vec2 point = event.position();
            if (effect.age < .4) {
                g.setStroke(event.type() == CombatFeedback.Type.DAMAGE ? event.fruit().color() : color);
                g.setLineWidth(3 * (1 - effect.age / .4));
                double radius = 12 + effect.age * 100;
                g.strokeOval(point.x() - radius, point.y() - radius, radius * 2, radius * 2);
            }
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.setFont(Font.font("System", FontWeight.BOLD, event.type() == CombatFeedback.Type.DAMAGE ? 17 : 13));
            g.setFill(Color.rgb(0, 0, 0, .65));
            double y = point.y() - 45 - effect.age * 35;
            if (event.type() == CombatFeedback.Type.REWARD) y -= 22;
            g.fillText(event.text(), point.x() + 1, y + 1);
            g.setFill(color);
            g.fillText(event.text(), point.x(), y);
        }
        g.setGlobalAlpha(1);
    }

    private Enemy hoveredEnemy() {
        return controller.enemies().stream()
                .filter(enemy -> enemy.position().distanceTo(cursor) <= enemy.radius() + 34)
                .min(java.util.Comparator.comparingDouble(enemy -> enemy.position().distanceTo(cursor)))
                .orElse(null);
    }

    private void drawEnemies(GraphicsContext g) {
        for (Enemy enemy : controller.enemies()) {
            Vec2 p = enemy.position();
            double r = enemy.radius();
            double cell = monsterAtlas.getWidth() / 2.0;
            int index = enemy.kind().ordinal();
            double sx = (index % 2) * cell;
            double sy = (index / 2) * cell;
            double size = r * (enemy.kind() == EnemyKind.BOSS ? 4.1 : 3.35);
            double bob = Math.sin(elapsed * 5 + enemy.routeProgress()) * 3;

            if (enemy.isSlowed()) {
                g.setFill(Color.rgb(111, 226, 255, .28));
                g.fillOval(p.x() - r * 1.55, p.y() - r * 1.45, r * 3.1, r * 2.9);
            }
            g.setGlobalAlpha(.98);
            g.drawImage(monsterAtlas, sx, sy, cell, cell,
                    p.x() - size / 2, p.y() - size * .56 + bob, size, size);
            g.setGlobalAlpha(1);

            double barWidth = Math.max(52, r * 2.8);
            double barY = p.y() - r * 1.7 + bob;
            g.setFill(Color.rgb(20, 27, 20, .72));
            g.fillRoundRect(p.x() - barWidth / 2, barY, barWidth, 8, 8, 8);
            g.setFill(enemy.healthRatio() > .45 ? Color.web("#72ed76") : Color.web("#ff625e"));
            g.fillRoundRect(p.x() - barWidth / 2 + 1, barY + 1,
                    Math.max(0, (barWidth - 2) * enemy.healthRatio()), 6, 6, 6);
        }
    }

    private void drawProjectiles(GraphicsContext g) {
        for (Projectile projectile : controller.projectiles()) {
            Vec2 p = projectile.position();
            g.setFill(Color.rgb(255, 255, 255, .26));
            g.fillOval(p.x() - 12, p.y() - 12, 24, 24);
            switch (projectile.fruitKind()) {
                case ORANGE -> {
                    g.setFill(projectile.color());
                    g.fillOval(p.x() - 8, p.y() - 8, 16, 16);
                    g.setFill(Color.web("#65ad45"));
                    g.fillOval(p.x() + 1, p.y() - 11, 8, 5);
                }
                case GRAPE -> {
                    g.setFill(projectile.color());
                    g.fillOval(p.x() - 7, p.y() - 7, 9, 9);
                    g.fillOval(p.x() + 1, p.y() - 4, 8, 8);
                    g.fillOval(p.x() - 3, p.y() + 2, 8, 8);
                }
                case WATERMELON -> {
                    g.setFill(Color.web("#38a953"));
                    g.fillOval(p.x() - 11, p.y() - 11, 22, 22);
                    g.setStroke(Color.web("#174e2a"));
                    g.setLineWidth(2);
                    g.strokeArc(p.x() - 8, p.y() - 10, 8, 20, 75, 210, ArcType.OPEN);
                    g.strokeArc(p.x(), p.y() - 10, 8, 20, -105, 210, ArcType.OPEN);
                }
                case LEMON -> {
                    g.setFill(Color.web("#c8f4ff"));
                    g.fillPolygon(new double[]{p.x(), p.x() + 8, p.x(), p.x() - 8},
                            new double[]{p.y() - 12, p.y(), p.y() + 12, p.y()}, 4);
                    g.setStroke(Color.web("#59c7ee"));
                    g.strokeLine(p.x(), p.y() - 10, p.x(), p.y() + 10);
                }
                case STRAWBERRY -> {
                    g.setFill(projectile.color());
                    g.fillPolygon(new double[]{p.x() - 9, p.x() + 9, p.x()},
                            new double[]{p.y() - 6, p.y() - 6, p.y() + 12}, 3);
                    g.setFill(Color.web("#fff5b1"));
                    g.fillOval(p.x() - 3, p.y() - 1, 3, 3);
                    g.fillOval(p.x() + 2, p.y() + 3, 3, 3);
                }
                case PINEAPPLE -> {
                    Vec2 destination = projectile.destination();
                    double angle = Math.toDegrees(Math.atan2(destination.y() - p.y(), destination.x() - p.x()));
                    g.save();
                    g.translate(p.x(), p.y());
                    g.rotate(angle);
                    g.setFill(projectile.color());
                    g.fillPolygon(new double[]{-15, 13, -15, -7}, new double[]{-5, 0, 5, 0}, 4);
                    g.setStroke(Color.web("#fff3b8"));
                    g.strokeLine(-20, 0, 7, 0);
                    g.restore();
                }
                case BLUEBERRY -> {
                    g.setFill(projectile.color());
                    g.fillOval(p.x() - 10, p.y() - 10, 20, 20);
                    g.setStroke(Color.web("#d9f6ff"));
                    g.setLineWidth(2);
                    for (int i = 0; i < 3; i++) {
                        double angle = i * Math.PI / 3;
                        g.strokeLine(p.x() - 13 * Math.cos(angle), p.y() - 13 * Math.sin(angle),
                                p.x() + 13 * Math.cos(angle), p.y() + 13 * Math.sin(angle));
                    }
                }
                case PEACH -> {
                    g.setFill(projectile.color());
                    g.fillOval(p.x() - 10, p.y() - 7, 12, 16);
                    g.fillOval(p.x() - 2, p.y() - 7, 12, 16);
                    g.setStroke(Color.web("#e77d90"));
                    g.strokeLine(p.x(), p.y() - 4, p.x(), p.y() + 7);
                    g.setFill(Color.web("#8bca74"));
                    g.fillOval(p.x(), p.y() - 12, 10, 5);
                }
            }
        }
    }

    private void drawEnemyIntroduction(GraphicsContext g) {
        EnemyKind kind = controller.introKind();
        if (kind == null) return;
        double alpha = Math.min(1, controller.introProgress() * 2.8);
        g.setGlobalAlpha(alpha);
        g.setFill(Color.rgb(15, 35, 24, .90));
        g.fillRoundRect(255, 20, 390, 92, 24, 24);
        g.setStroke(Color.rgb(255, 224, 126, .82));
        g.setLineWidth(2);
        g.strokeRoundRect(255, 20, 390, 92, 24, 24);

        double cell = monsterAtlas.getWidth() / 2.0;
        int index = kind.ordinal();
        g.drawImage(monsterAtlas, (index % 2) * cell, (index / 2) * cell, cell, cell,
                267, 25, 82, 82);
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.TOP);
        g.setFill(Color.web("#ffe27b"));
        g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 18));
        g.fillText("新敌人  ·  " + kind.displayName(), 358, 34);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("System", FontWeight.NORMAL, 12));
        double scale = controller.level().difficultyScale();
        g.fillText("生命 " + (int) Math.ceil(kind.health() * scale) + "   速度 " + Math.round(kind.speed() * (.92 + scale * .08))
                + "   击杀积分 " + kind.scoreReward(), 358, 62);
        g.setFill(Color.web("#cfe5d3"));
        g.fillText(kind.description(), 358, 84);
        g.setGlobalAlpha(1);
    }

    private void drawUltimateEffect(GraphicsContext g) {
        if (!controller.ultimateFlashing() || controller.ultimateVisualKind() == null) return;
        FruitKind kind = controller.ultimateVisualKind();
        Color glow = kind.color().deriveColor(0, 1, 1, .34);
        g.setFill(glow);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 42));
        g.setFill(Color.WHITE);
        g.setEffect(new javafx.scene.effect.DropShadow(18, kind.color()));
        g.fillText(kind.ultimateName(), WIDTH / 2, HEIGHT / 2);
        g.setEffect(null);
    }
}
