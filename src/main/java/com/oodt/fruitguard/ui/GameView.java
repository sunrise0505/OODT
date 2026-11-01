package com.oodt.fruitguard.ui;

import com.oodt.fruitguard.core.GameController;
import com.oodt.fruitguard.core.LevelConfig;
import com.oodt.fruitguard.core.MusicManager;
import com.oodt.fruitguard.core.BattleStatistics;
import com.oodt.fruitguard.model.*;
import javafx.animation.AnimationTimer;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** 三段式界面：原创封面、关卡选择和手动瞄准战场。 */
public final class GameView extends StackPane {
    private final GameController controller = new GameController();
    private final MusicManager music = new MusicManager();
    private final Image coverImage = asset("cover-orchard.png");
    private final Image battleImage = asset("battle-orchard.png");
    private final Image heroAtlas = asset("fruit-heroes-atlas.png");
    private final Image extraHeroAtlas = asset("fruit-heroes-extra-atlas.png");
    private final Image monsterAtlas = asset("monsters-atlas.png");
    private final GameCanvas canvas = new GameCanvas(controller);

    private final StackPane coverScreen = new StackPane();
    private final StackPane levelScreen = new StackPane();
    private final BorderPane battleScreen = new BorderPane();
    private final StackPane battleStack = new StackPane();
    private final VBox overlay = new VBox(15);
    private final TilePane levelCards = new TilePane(16, 16);
    private final Map<FruitKind, Button> weaponButtons = new EnumMap<>(FruitKind.class);
    private final Map<FruitKind, Label> weaponStatLabels = new EnumMap<>(FruitKind.class);
    private final Map<SpecialAttack, Button> specialButtons = new EnumMap<>(SpecialAttack.class);
    private final List<Button> musicButtons = new ArrayList<>();

    private final Label livesLabel = stat("❤ 8");
    private final Label coinsLabel = stat("● 0");
    private final Label scoreLabel = stat("★ 0");
    private final Label waveLabel = stat("波次 0/0");
    private final Label levelLabel = stat("果园");
    private final Label messageLabel = new Label("移动准星，点击怪物发射水果");
    private final Label weaponStatus = new Label();
    private final ProgressBar energyBar = new ProgressBar(0);
    private final Button pauseButton = new Button("暂停");
    private final Button speedButton = new Button("速度 ×1");
    private final Button upgradeButton = new Button("升级武器");
    private final Button ultimateButton = new Button("专属大招  SPACE");
    private final Button aimModeButton = new Button("点击瞄准 M");
    private final Label tacticalBalance = new Label();
    private final Label liveStatistics = new Label();
    private final Label coverRecord = new Label();
    private final ProgressBar reloadBar = new ProgressBar(1);

    private AnimationTimer timer;
    private long lastFrame;
    private double speed = 1;
    private GameState lastState = GameState.MENU;

    public GameView() {
        getStyleClass().add("app-root");
        coverScreen.getChildren().addAll(fullBackground(coverImage, false), coverShade(), buildCoverContent());
        levelScreen.getChildren().addAll(fullBackground(battleImage, true), levelShade(), buildLevelContent());
        buildBattleScreen();
        getChildren().addAll(coverScreen, levelScreen, battleScreen);
        showCover();
        installKeys();
    }

    private static Image asset(String name) {
        return new Image(GameView.class.getResourceAsStream("/com/oodt/fruitguard/assets/" + name));
    }

    private ImageView fullBackground(Image image, boolean muted) {
        ImageView view = new ImageView(image);
        view.setPreserveRatio(false);
        view.fitWidthProperty().bind(widthProperty());
        view.fitHeightProperty().bind(heightProperty());
        if (muted) {
            ColorAdjust adjust = new ColorAdjust();
            adjust.setBrightness(-.18);
            adjust.setSaturation(-.15);
            view.setEffect(adjust);
        }
        return view;
    }

    private Region coverShade() {
        Region shade = new Region();
        shade.getStyleClass().add("cover-shade");
        return shade;
    }

    private Region levelShade() {
        Region shade = new Region();
        shade.getStyleClass().add("level-shade");
        return shade;
    }

    private VBox buildCoverContent() {
        Label eyebrow = new Label("A LITTLE ORCHARD. A GRAND ADVENTURE.");
        eyebrow.getStyleClass().add("cover-eyebrow");
        Label title = new Label("保卫水果");
        title.getStyleClass().add("cover-title");
        Label english = new Label("FRUIT  GUARD");
        english.getStyleClass().add("cover-title-en");
        Label desc = new Label("选择水果英雄，亲手瞄准来袭怪物。\n每一次点击，都是守住果仓的一发关键投掷。");
        desc.getStyleClass().add("cover-description");
        desc.setWrapText(true);

        HBox badges = new HBox(9,
                badge(FruitKind.values().length + " 位水果英雄"), badge(controller.levels().size() + " 个原创关卡"),
                badge("专属大招"), badge("中央瞄准"));
        Button start = new Button("开始守护  ▶");
        start.getStyleClass().add("cover-primary");
        start.setOnAction(e -> showLevelSelect());
        Button codex = new Button("角色图鉴");
        codex.getStyleClass().add("cover-secondary");
        codex.setOnAction(e -> showEncyclopedia());
        Button help = new Button("玩法说明");
        help.getStyleClass().add("cover-secondary");
        help.setOnAction(e -> showHelp());
        Button musicButton = musicButton();
        musicButton.getStyleClass().add("cover-secondary");
        HBox actions = new HBox(10, start, codex, help, musicButton);

        coverRecord.getStyleClass().add("cover-record");
        VBox box = new VBox(8, eyebrow, title, english, desc, badges, actions, coverRecord);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(600);
        StackPane.setAlignment(box, Pos.CENTER_LEFT);
        StackPane.setMargin(box, new Insets(20, 30, 20, 72));
        return box;
    }

    private Label badge(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("feature-badge");
        return label;
    }

    private VBox buildLevelContent() {
        Label eyebrow = new Label("CHOOSE YOUR ORCHARD");
        eyebrow.getStyleClass().add("cover-eyebrow");
        Label title = new Label("选择守护关卡");
        title.getStyleClass().add("level-title");
        Label hint = new Label("8 片果园 · 3～8 波不同节奏 · 通关后解锁下一关");
        hint.getStyleClass().add("level-subtitle");

        levelCards.setAlignment(Pos.CENTER);
        levelCards.setPrefColumns(4);
        levelCards.setPrefTileWidth(236);
        levelCards.setPrefTileHeight(204);
        levelCards.setPrefWidth(1000);
        rebuildLevelCards();
        ScrollPane levelScroll = scroll(levelCards);
        levelScroll.setMaxWidth(1040);
        levelScroll.setMaxHeight(430);
        levelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Button back = new Button("← 返回封面");
        back.getStyleClass().add("cover-secondary");
        back.setOnAction(e -> showCover());
        VBox content = new VBox(13, eyebrow, title, hint, levelScroll, back);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(1080);
        return content;
    }

    private void rebuildLevelCards() {
        levelCards.getChildren().setAll(controller.levels().stream().map(this::createLevelCard).toList());
    }

    private Button createLevelCard(LevelConfig level) {
        boolean locked = level.number() > controller.unlockedLevel();
        Label number = new Label(locked ? "🔒" : "0" + level.number());
        number.getStyleClass().add("level-number");
        Label name = new Label(level.name());
        name.getStyleClass().add("level-card-name");
        Label subtitle = new Label(locked ? "通关前一关后解锁" : level.subtitle());
        subtitle.getStyleClass().add("level-card-subtitle");
        Label difficulty = new Label("难度  " + difficultyDots(level.number()));
        difficulty.getStyleClass().add("level-card-data");
        Label data = new Label("初始金币  " + level.startingCoins()
                + "  ·  " + level.waves().size() + " 波敌袭");
        data.setTextAlignment(TextAlignment.CENTER);
        data.getStyleClass().add("level-card-data");
        data.setMinHeight(Region.USE_PREF_SIZE);
        int stars = controller.levelStars(level.number());
        Label record = labelWithClass("★".repeat(stars) + "☆".repeat(3 - stars)
                + "  ·  最高 " + controller.levelBestScore(level.number()), "level-card-record");
        VBox content = new VBox(4, number, name, subtitle, new Separator(), difficulty, data, record);
        content.setAlignment(Pos.CENTER);
        Button button = new Button();
        button.setGraphic(content);
        button.setPrefSize(236, 204);
        button.getStyleClass().add("level-card");
        button.setDisable(locked);
        button.setOnAction(e -> showLevelBrief(level));
        return button;
    }

    private String difficultyDots(int level) {
        int rating = Math.min(5, (level + 2) / 2);
        return "●".repeat(rating) + "○".repeat(5 - rating);
    }

    private void buildBattleScreen() {
        battleScreen.getStyleClass().add("battle-root");
        battleScreen.setTop(buildHeader());
        battleScreen.setCenter(buildBattleCenter());
        battleScreen.setRight(buildSidebar());
        battleScreen.setBottom(buildFooter());
        BorderPane.setMargin(battleStack, new Insets(10, 8, 7, 18));
    }

    private HBox buildHeader() {
        Label mark = new Label("FG");
        mark.getStyleClass().add("brand-mark");
        Label brand = new Label("FRUIT GUARD");
        brand.getStyleClass().add("brand");
        Label subtitle = new Label("手动瞄准作战中心");
        subtitle.getStyleClass().add("brand-cn");
        VBox title = new VBox(0, brand, subtitle);
        HBox identity = new HBox(10, mark, title);
        identity.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox stats = new HBox(8, levelLabel, waveLabel, livesLabel, coinsLabel, scoreLabel);
        stats.setAlignment(Pos.CENTER_RIGHT);
        HBox header = new HBox(18, identity, spacer, stats);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.getStyleClass().add("header");
        return header;
    }

    private StackPane buildBattleCenter() {
        StackPane frame = new StackPane(canvas);
        frame.getStyleClass().add("canvas-frame");
        frame.setMinSize(0, 0);
        var scale = javafx.beans.binding.Bindings.createDoubleBinding(() -> Math.max(.1,
                        Math.min((frame.getWidth() - 14) / GameCanvas.WIDTH,
                                (frame.getHeight() - 14) / GameCanvas.HEIGHT)),
                frame.widthProperty(), frame.heightProperty());
        canvas.scaleXProperty().bind(scale);
        canvas.scaleYProperty().bind(scale);
        overlay.setAlignment(Pos.CENTER);
        overlay.setMaxWidth(600);
        overlay.setMaxHeight(470);
        overlay.getStyleClass().add("overlay-card");
        overlay.setVisible(false);
        battleStack.getChildren().addAll(frame, overlay);
        battleStack.setAlignment(Pos.CENTER);
        return battleStack;
    }

    private VBox buildSidebar() {
        Label heading = new Label("水果武器库");
        heading.getStyleClass().add("section-title");
        Label hint = new Label("1—8 切换英雄 · 从中央向两侧瞄准");
        hint.setWrapText(true);
        hint.getStyleClass().add("muted");

        GridPane cards = new GridPane();
        cards.setHgap(6);
        cards.setVgap(6);
        for (int column = 0; column < 2; column++) {
            ColumnConstraints constraint = new ColumnConstraints();
            constraint.setPercentWidth(50);
            cards.getColumnConstraints().add(constraint);
        }
        for (FruitKind kind : FruitKind.values()) {
            Button button = createWeaponCard(kind);
            weaponButtons.put(kind, button);
            cards.add(button, kind.ordinal() % 2, kind.ordinal() / 2);
        }
        ScrollPane cardScroll = scroll(cards);
        cardScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cardScroll.setPrefViewportHeight(300);
        cardScroll.setMinHeight(160);
        VBox.setVgrow(cardScroll, Priority.ALWAYS);
        TabPane arsenal = new TabPane();
        arsenal.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        arsenal.getTabs().addAll(new Tab("水果武器", cardScroll), new Tab("积分道具", scroll(buildTactics())));
        arsenal.setMinHeight(165);
        arsenal.setPrefHeight(300);
        VBox.setVgrow(arsenal, Priority.ALWAYS);

        reloadBar.setMaxWidth(Double.MAX_VALUE);
        reloadBar.setMinHeight(7);
        reloadBar.setMaxHeight(7);
        reloadBar.getStyleClass().add("reload-bar");

        weaponStatus.getStyleClass().add("weapon-status");
        weaponStatus.setWrapText(true);
        weaponStatus.setMinHeight(Region.USE_PREF_SIZE);
        upgradeButton.setMaxWidth(Double.MAX_VALUE);
        upgradeButton.setOnAction(e -> controller.upgradeSelectedWeapon());

        energyBar.setMaxWidth(Double.MAX_VALUE);
        energyBar.getStyleClass().add("energy-bar");
        ultimateButton.setMaxWidth(Double.MAX_VALUE);
        ultimateButton.getStyleClass().add("ultimate-button");
        ultimateButton.setOnAction(e -> controller.useUltimate());

        Button codex = new Button("敌情与英雄档案");
        codex.setMaxWidth(Double.MAX_VALUE);
        codex.setOnAction(e -> showEncyclopedia());

        VBox sidebar = new VBox(8, heading, hint, arsenal, reloadBar, weaponStatus, upgradeButton,
                new Separator(), labelWithClass("专属大招能量", "section-title-small"),
                energyBar, ultimateButton, codex);
        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(315);
        sidebar.getStyleClass().add("sidebar");
        BorderPane.setMargin(sidebar, new Insets(10, 18, 7, 5));
        return sidebar;
    }

    private Button createWeaponCard(FruitKind kind) {
        ImageView portrait = heroPortrait(kind, 40);
        Label name = new Label((kind.ordinal() + 1) + " " + kind.displayName());
        name.getStyleClass().add("weapon-name");
        Label stats = new Label("伤害 " + (int) kind.damage() + "\n" + kind.cooldown() + "s / 发");
        weaponStatLabels.put(kind, stats);
        stats.getStyleClass().add("weapon-card-detail");
        VBox text = new VBox(2, name, stats);
        HBox graphic = new HBox(4, portrait, text);
        graphic.setAlignment(Pos.CENTER_LEFT);
        Button button = new Button();
        button.setGraphic(graphic);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinWidth(0);
        button.setPrefHeight(69);
        button.setTooltip(new Tooltip(kind.description() + "\n" + kind.ultimateName() + "：" + kind.ultimateDescription()));
        button.getStyleClass().add("weapon-card");
        button.setOnAction(e -> controller.selectKind(kind));
        return button;
    }

    private ImageView heroPortrait(FruitKind kind, double size) {
        Image atlas = kind.atlasIndex() < 4 ? heroAtlas : extraHeroAtlas;
        double cell = atlas.getWidth() / 2.0;
        ImageView view = new ImageView(atlas);
        view.setViewport(new Rectangle2D((kind.atlasCell() % 2) * cell,
                (kind.atlasCell() / 2) * cell, cell, cell));
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        return view;
    }

    private HBox buildFooter() {
        messageLabel.getStyleClass().add("message");
        messageLabel.setMaxWidth(460);
        messageLabel.setMinWidth(0);
        messageLabel.setWrapText(true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        pauseButton.setOnAction(e -> controller.togglePause());
        speedButton.setOnAction(e -> {
            speed = speed == 1 ? 2 : 1;
            speedButton.setText("速度 ×" + (int) speed);
        });
        Button musicButton = musicButton();
        Button menu = new Button("返回选关");
        menu.setOnAction(e -> confirmReturnToLevels());
        aimModeButton.setOnAction(e -> toggleAimMode());
        HBox footer = new HBox(9, labelWithClass("⌖", "crosshair-icon"), messageLabel,
                spacer, aimModeButton, pauseButton, speedButton, musicButton, menu);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(7, 20, 12, 20));
        footer.getStyleClass().add("battle-footer");
        return footer;
    }

    private Button musicButton() {
        Button button = new Button(music.enabled() ? "音乐 开" : "音乐 关");
        button.setDisable(!music.available());
        if (!music.available()) button.setTooltip(new Tooltip("当前音频设备不可用"));
        button.setOnAction(e -> {
            music.toggle();
            updateMusicButtons();
        });
        musicButtons.add(button);
        return button;
    }

    private void updateMusicButtons() {
        String text = music.enabled() ? "音乐 开" : "音乐 关";
        musicButtons.forEach(button -> button.setText(text));
    }

    private void startLevel(int number) {
        controller.startLevel(number);
        if (controller.state() != GameState.RUNNING) return;
        showOnly(battleScreen);
        overlay.setVisible(false);
        canvas.resetEffects();
        lastState = GameState.RUNNING;
        requestFocus();
    }

    private void showCover() {
        controller.returnToMenu();
        canvas.cancelInput();
        coverRecord.setText("历史最高分  " + controller.highScore());
        showOnly(coverScreen);
        requestFocus();
    }

    private void showLevelSelect() {
        controller.returnToMenu();
        canvas.cancelInput();
        rebuildLevelCards();
        showOnly(levelScreen);
        requestFocus();
    }

    private void showOnly(Node node) {
        coverScreen.setVisible(node == coverScreen);
        coverScreen.setManaged(node == coverScreen);
        levelScreen.setVisible(node == levelScreen);
        levelScreen.setManaged(node == levelScreen);
        battleScreen.setVisible(node == battleScreen);
        battleScreen.setManaged(node == battleScreen);
    }

    public void startLoop() {
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastFrame == 0) lastFrame = now;
                double dt = Math.min(.05, (now - lastFrame) / 1_000_000_000.0);
                lastFrame = now;
                if (battleScreen.isVisible()) {
                    controller.update(dt * speed);
                    canvas.updateInput();
                    canvas.render(dt);
                    refreshHud();
                    updateOverlayForState();
                }
            }
        };
        timer.start();
    }

    public void shutdown() {
        if (timer != null) timer.stop();
        music.close();
    }

    private void installKeys() {
        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (!battleScreen.isVisible()) return;
            if (event.getCode() == KeyCode.SPACE) { controller.useUltimate(); event.consume(); }
            else if (event.getCode() == KeyCode.M) { toggleAimMode(); event.consume(); }
            else if (event.getCode() == KeyCode.Q) { controller.useSpecialAttack(SpecialAttack.FROST); event.consume(); }
            else if (event.getCode() == KeyCode.E) { controller.useSpecialAttack(SpecialAttack.BOMB); event.consume(); }
            else if (event.getCode() == KeyCode.R) { controller.useSpecialAttack(SpecialAttack.REPAIR); event.consume(); }
            else if (event.getCode() == KeyCode.P || event.getCode() == KeyCode.ESCAPE) {
                controller.togglePause(); event.consume();
            } else {
                String digit = event.getCode().getName().replace("Numpad ", "");
                if (digit.matches("[1-8]")) {
                    controller.selectKind(FruitKind.values()[Integer.parseInt(digit) - 1]);
                    event.consume();
                }
            }
        });
        setFocusTraversable(true);
    }

    private void refreshHud() {
        if (controller.level() == null || controller.scoreManager() == null) return;
        levelLabel.setText(controller.level().name());
        waveLabel.setText("波次 " + controller.displayedWave() + "/" + controller.level().waves().size());
        livesLabel.setText("❤ " + Math.max(0, controller.lives()));
        coinsLabel.setText("● " + controller.scoreManager().coins());
        scoreLabel.setText("★ " + controller.scoreManager().score());
        messageLabel.setText(controller.message());
        energyBar.setProgress(controller.scoreManager().energy() / 100.0);

        for (Map.Entry<FruitKind, Button> entry : weaponButtons.entrySet()) {
            entry.getValue().pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"),
                    entry.getKey() == controller.selectedKind());
            FruitWeapon current = controller.weapon(entry.getKey());
            weaponStatLabels.get(entry.getKey()).setText("击打 " + Math.round(current.damage()) + " · Lv." + current.level()
                    + "\n" + String.format("%.2f", current.fireInterval()) + "s / 发");
        }
        FruitWeapon weapon = controller.selectedWeapon();
        reloadBar.setProgress(1 - weapon.cooldownRatio());
        weaponStatus.setText((weapon.ready() ? "● 已就绪" : "◌ 装填中") + " · 击打点数 " + Math.round(weapon.damage())
                + "\n" + weapon.kind().ultimateName() + "：" + weapon.kind().ultimateDescription());
        upgradeButton.setText(weapon.level() >= 3 ? "武器已满级" : "升级到 Lv." + (weapon.level() + 1)
                + "  ·  " + weapon.upgradeCost() + " 金币");
        upgradeButton.setDisable(weapon.level() >= 3 || controller.state() != GameState.RUNNING
                || controller.scoreManager().coins() < weapon.upgradeCost());
        ultimateButton.setText(weapon.kind().ultimateName() + "  ·  SPACE");
        ultimateButton.setDisable(controller.scoreManager().energy() < 100 || controller.state() != GameState.RUNNING);
        tacticalBalance.setText("可用积分  " + controller.scoreManager().tacticalPoints());
        liveStatistics.setText("本局总积分 " + controller.scoreManager().score() + "\n击败 " + controller.statistics().kills()
                + " 只 · 命中 " + Math.round(controller.statistics().accuracy() * 100) + "%\n"
                + "道具只消耗可用积分，不扣除总成绩。");
        specialButtons.forEach((attack, button) -> button.setDisable(controller.state() != GameState.RUNNING
                || controller.scoreManager().tacticalPoints() < attack.cost()
                || (attack == SpecialAttack.REPAIR && controller.lives() >= 8)));
        pauseButton.setText(controller.state() == GameState.PAUSED ? "继续" : "暂停");
    }

    private void updateOverlayForState() {
        GameState state = controller.state();
        if (state == lastState) return;
        if (state == GameState.VICTORY) showResultOverlay(true);
        else if (state == GameState.DEFEAT) showResultOverlay(false);
        else if (state == GameState.PAUSED) showPauseOverlay();
        else if (state == GameState.RUNNING) overlay.setVisible(false);
        lastState = state;
    }

    private void showPauseOverlay() {
        overlay.getChildren().clear();
        Label title = labelWithClass("战斗暂停", "overlay-title");
        Label text = new Label("观察路线，切换水果，准备好后继续瞄准");
        Button resume = new Button("继续战斗");
        resume.getStyleClass().add("primary-button");
        resume.setOnAction(e -> controller.togglePause());
        Button help = new Button("查看玩法说明");
        help.setOnAction(e -> showHelp());
        overlay.getChildren().addAll(title, text, resume, help);
        overlay.setVisible(true);
    }

    private void showResultOverlay(boolean victory) {
        overlay.getChildren().clear();
        Label eyebrow = labelWithClass(victory ? "ORCHARD SECURED" : "ORCHARD LOST", "cover-eyebrow");
        Label title = labelWithClass(victory ? "守护成功" : "果仓失守", "overlay-title");
        int score = controller.scoreManager().score();
        BattleStatistics stats = controller.statistics();
        int stars = victory ? BattleStatistics.starsForLives(controller.lives()) : 0;
        Label starsLabel = labelWithClass("★".repeat(stars) + "☆".repeat(3 - stars), "result-stars");
        Label result = new Label((victory ? "这一片果园安全了！" : "怪物抢走了最后一筐水果。")
                + "\n本局积分  " + score + "  ·  本关最高  " + controller.levelBestScore(controller.level().number())
                + "\n击败 " + stats.kills() + " 只 · 发射 " + stats.shots() + " 次 · 命中率 " + Math.round(stats.accuracy() * 100) + "%"
                + "\n果仓生命 " + Math.max(0, controller.lives()) + "/8 · 用时 " + (int) stats.elapsedSeconds() + " 秒");
        result.setTextAlignment(TextAlignment.CENTER);
        Button retry = new Button("再试一次");
        retry.getStyleClass().add("primary-button");
        retry.setOnAction(e -> startLevel(controller.level().number()));
        Button levels = new Button("返回选关");
        levels.setOnAction(e -> showLevelSelect());
        HBox actions = new HBox(10, retry, levels);
        if (victory && controller.level().number() < controller.levels().size()) {
            Button next = new Button("下一关 →");
            next.getStyleClass().add("primary-button");
            next.setOnAction(e -> showLevelBrief(controller.levels().get(controller.level().number())));
            actions.getChildren().add(next);
        }
        actions.setAlignment(Pos.CENTER);
        overlay.getChildren().addAll(eyebrow, title, starsLabel, result, actions);
        overlay.setVisible(true);
    }

    private void showEncyclopedia() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("水果英雄与怪物档案");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox fruits = new VBox(10);
        fruits.setPadding(new Insets(10));
        for (FruitKind kind : FruitKind.values()) {
            fruits.getChildren().add(infoRow(kind.displayName(), heroPortrait(kind, 76),
                    "名称 " + kind.displayName() + "｜基础击打点数 " + (int) kind.damage() + "｜冷却 " + kind.cooldown() + " 秒｜弹速 "
                            + (int) kind.projectileSpeed() + "\n普通攻击：" + kind.description()
                            + "\n专属大招：" + kind.ultimateName() + " — " + kind.ultimateDescription()));
        }

        VBox monsters = new VBox(10);
        monsters.setPadding(new Insets(10));
        for (EnemyKind kind : EnemyKind.values()) {
            monsters.getChildren().add(infoRow(kind.displayName(), monsterPortrait(kind, 76),
                    "基础血量 " + (int) kind.health() + "｜基础速度 " + (int) kind.speed() + " px/s"
                            + "｜击杀积分 " + kind.scoreReward() + "｜金币 " + kind.coinReward()
                            + "\n" + kind.description()));
        }
        ScrollPane fruitScroll = scroll(fruits);
        ScrollPane monsterScroll = scroll(monsters);
        tabs.getTabs().addAll(new Tab("水果英雄", fruitScroll), new Tab("入侵怪物", monsterScroll));
        tabs.setPrefSize(720, 500);
        dialog.getDialogPane().setContent(tabs);
        showManagedDialog(dialog);
    }

    private VBox buildTactics() {
        tacticalBalance.getStyleClass().add("section-title-small");
        VBox box = new VBox(10, tacticalBalance);
        box.setPadding(new Insets(10, 0, 8, 0));
        for (SpecialAttack attack : SpecialAttack.values()) {
            Button button = new Button(attack.displayName() + "  [" + attack.shortcut() + "]  ·  " + attack.cost() + " 分");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> controller.useSpecialAttack(attack));
            button.setTooltip(new Tooltip(attack.description()));
            specialButtons.put(attack, button);
            Label hint = labelWithClass(attack.description(), "muted");
            hint.setWrapText(true);
            box.getChildren().addAll(button, hint);
        }
        liveStatistics.getStyleClass().add("muted");
        liveStatistics.setWrapText(true);
        box.getChildren().add(liveStatistics);
        return box;
    }

    private void toggleAimMode() {
        canvas.setSlingMode(!canvas.slingMode());
        aimModeButton.setText(canvas.slingMode() ? "弹弓蓄力 M" : "点击瞄准 M");
    }

    private ImageView monsterPortrait(EnemyKind kind, double size) {
        double cell = monsterAtlas.getWidth() / 2;
        ImageView view = new ImageView(monsterAtlas);
        view.setViewport(new Rectangle2D(kind.ordinal() % 2 * cell, kind.ordinal() / 2 * cell, cell, cell));
        view.setFitWidth(size);
        view.setFitHeight(size);
        return view;
    }

    private void showHelp() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("玩法说明与快捷键");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Label text = new Label("目标：阻止怪物到达果仓。果仓生命归零即失败，消灭全部波次即通关。\n\n"
                + "点击模式：移动鼠标瞄准，点击怪物锁定攻击；按住左键可持续发射。\n"
                + "弹弓模式：按 M 切换，从中央英雄处拖动弹弓，向后拉并松手，向相反方向发射。\n"
                + "拉得越远，伤害和弹速越高；极限蓄力为基础伤害的 1.5 倍。\n\n"
                + "1—8：切换水果。金币：升级武器。Space：释放当前水果的大招。\n"
                + "击杀增加总积分、可用积分、金币和大招能量。积分道具在右侧第二个页签。\n"
                + "Q 寒霜风暴：120 分；E 果实炸弹：180 分，在最后瞄准位置爆炸；R 修复：200 分。\n\n"
                + "P / Esc 暂停或继续；图鉴和说明窗口打开时战斗会自动暂停。\n"
                + "星级：剩余 7—8 点生命为三星，4—6 点为二星，1—3 点为一星。\n"
                + "关前可以查看实际怪物属性与阵容；通关后保存关卡成绩、星级并解锁下一关。");
        text.setWrapText(true);
        text.setMaxWidth(640);
        VBox content = new VBox(text);
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);
        showManagedDialog(dialog);
    }

    private void showLevelBrief(LevelConfig level) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("关前准备 · " + level.name());
        ButtonType enter = new ButtonType("进入关卡", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(enter, ButtonType.CANCEL);
        Label heading = labelWithClass(level.name() + " · " + level.waves().size() + " 波敌袭", "section-title");
        Label summary = new Label(level.subtitle() + "\n初始金币 " + level.startingCoins()
                + " · 果仓生命 8 · 三星目标：至少保留 7 点生命");
        ComboBox<FruitKind> selection = new ComboBox<>();
        selection.getItems().addAll(FruitKind.values());
        selection.setConverter(new javafx.util.StringConverter<>() {
            public String toString(FruitKind kind) { return kind == null ? "" : kind.displayName() + " · " + kind.description(); }
            public FruitKind fromString(String text) { throw new UnsupportedOperationException(); }
        });
        selection.setValue(controller.selectedKind());
        selection.setMaxWidth(Double.MAX_VALUE);
        VBox content = new VBox(10, heading, summary, new Label("首发水果（进入后仍可随时切换）"), selection,
                new Label("本关怪物属性（已计入关卡难度）"));
        var kinds = java.util.EnumSet.noneOf(EnemyKind.class);
        level.waves().forEach(wave -> wave.entries().forEach(entry -> kinds.add(entry.kind())));
        for (EnemyKind kind : kinds) {
            double scale = level.difficultyScale();
            content.getChildren().add(infoRow(kind.displayName(), monsterPortrait(kind, 48),
                    "血量 " + (int) Math.ceil(kind.health() * scale) + " · 速度 " + Math.round(kind.speed() * (.92 + scale * .08))
                            + " px/s · 击杀 " + kind.scoreReward() + " 分"));
        }
        for (int i = 0; i < level.waves().size(); i++) {
            String enemies = level.waves().get(i).entries().stream()
                    .map(entry -> entry.kind().displayName() + "×" + entry.count())
                    .collect(java.util.stream.Collectors.joining("  "));
            Label wave = new Label("第 " + (i + 1) + " 波：" + enemies);
            wave.setWrapText(true);
            content.getChildren().add(wave);
        }
        content.setPadding(new Insets(12));
        ScrollPane pane = scroll(content);
        pane.setPrefViewportWidth(640);
        pane.setPrefViewportHeight(490);
        dialog.getDialogPane().setContent(pane);
        if (showManagedDialog(dialog).orElse(ButtonType.CANCEL) == enter) {
            startLevel(level.number());
            controller.selectKind(selection.getValue());
        }
    }

    private <T> java.util.Optional<T> showManagedDialog(Dialog<T> dialog) {
        boolean resume = controller.state() == GameState.RUNNING;
        canvas.cancelInput();
        if (resume) controller.togglePause();
        if (getScene() != null) {
            dialog.initOwner(getScene().getWindow());
            dialog.getDialogPane().getStylesheets().addAll(getScene().getStylesheets());
        }
        try { return dialog.showAndWait(); }
        finally { if (resume && controller.state() == GameState.PAUSED) controller.togglePause(); }
    }

    private void confirmReturnToLevels() {
        if (controller.state() == GameState.RUNNING || controller.state() == GameState.PAUSED) {
            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, "返回选关会结束当前战斗，本局尚未结算的进度不会保存。",
                    ButtonType.OK, ButtonType.CANCEL);
            dialog.setTitle("结束当前战斗");
            dialog.setHeaderText("是否返回选关？");
            if (showManagedDialog(dialog).orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        }
        showLevelSelect();
    }

    private ScrollPane scroll(Node content) {
        ScrollPane pane = new ScrollPane(content);
        pane.setFitToWidth(true);
        return pane;
    }

    private HBox infoRow(String title, Node portrait, String detail) {
        Label titleLabel = labelWithClass(title, "info-title");
        Label detailLabel = new Label(detail);
        detailLabel.setWrapText(true);
        VBox text = new VBox(5, titleLabel, detailLabel);
        HBox box = portrait == null ? new HBox(text) : new HBox(12, portrait, text);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("info-row");
        return box;
    }

    private static Label stat(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("stat-pill");
        return label;
    }

    private static Label labelWithClass(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
