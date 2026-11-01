from pathlib import Path

from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "docs" / "保卫水果实验报告.docx"
existing_cover_values = None
if OUTPUT.exists():
    previous = Document(OUTPUT)
    if previous.tables and len(previous.tables[0].rows) == 5:
        existing_cover_values = [row.cells[1].text for row in previous.tables[0].rows]


def set_cell_shading(cell, fill):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = tc_pr.find(qn("w:shd"))
    if shd is None:
        shd = OxmlElement("w:shd")
        tc_pr.append(shd)
    shd.set(qn("w:fill"), fill)


def set_cell_margins(cell, top=120, start=130, bottom=120, end=130):
    tc = cell._tc
    tc_pr = tc.get_or_add_tcPr()
    tc_mar = tc_pr.first_child_found_in("w:tcMar")
    if tc_mar is None:
        tc_mar = OxmlElement("w:tcMar")
        tc_pr.append(tc_mar)
    for name, value in (("top", top), ("start", start), ("bottom", bottom), ("end", end)):
        node = tc_mar.find(qn(f"w:{name}"))
        if node is None:
            node = OxmlElement(f"w:{name}")
            tc_mar.append(node)
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")


def set_repeat_table_header(row):
    tr_pr = row._tr.get_or_add_trPr()
    tbl_header = OxmlElement("w:tblHeader")
    tbl_header.set(qn("w:val"), "true")
    tr_pr.append(tbl_header)


def set_run_font(run, east_asia="宋体", western="Arial", size=11, bold=False, color="000000"):
    compatible_font = "Hiragino Sans GB" if east_asia not in ("等距更纱黑体 SC",) else western
    run.font.name = compatible_font
    run.font.size = Pt(size)
    run.bold = bold
    run.font.color.rgb = RGBColor.from_string(color)
    run._element.rPr.rFonts.set(qn("w:ascii"), compatible_font)
    run._element.rPr.rFonts.set(qn("w:hAnsi"), compatible_font)
    run._element.rPr.rFonts.set(qn("w:eastAsia"), compatible_font)
    run._element.rPr.rFonts.set(qn("w:cs"), compatible_font)


def set_paragraph_spacing(paragraph, before=0, after=6, line=1.35):
    fmt = paragraph.paragraph_format
    fmt.space_before = Pt(before)
    fmt.space_after = Pt(after)
    fmt.line_spacing = line


def add_body(doc, text, bold_lead=None):
    paragraph = doc.add_paragraph()
    paragraph.paragraph_format.first_line_indent = Pt(22)
    set_paragraph_spacing(paragraph, after=7, line=1.45)
    if bold_lead and text.startswith(bold_lead):
        lead = paragraph.add_run(bold_lead)
        set_run_font(lead, bold=True)
        rest = paragraph.add_run(text[len(bold_lead):])
        set_run_font(rest)
    else:
        run = paragraph.add_run(text)
        set_run_font(run)
    return paragraph


def add_heading(doc, text, level=1):
    paragraph = doc.add_heading(text, level=level)
    paragraph.paragraph_format.keep_with_next = True
    paragraph.paragraph_format.space_before = Pt(14 if level == 1 else 10)
    paragraph.paragraph_format.space_after = Pt(7)
    for run in paragraph.runs:
        set_run_font(run, east_asia="黑体", size=16 if level == 1 else 13, bold=True)
    return paragraph


def add_bullet(doc, text):
    paragraph = doc.add_paragraph(style="List Bullet")
    set_paragraph_spacing(paragraph, after=3, line=1.3)
    run = paragraph.add_run(text)
    set_run_font(run)
    return paragraph


def add_table(doc, headers, rows, widths=None):
    table = doc.add_table(rows=1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.style = "Table Grid"
    header = table.rows[0]
    set_repeat_table_header(header)
    for index, text in enumerate(headers):
        cell = header.cells[index]
        set_cell_shading(cell, "244A38")
        set_cell_margins(cell)
        cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(str(text))
        set_run_font(r, east_asia="黑体", size=10.5, bold=True, color="FFFFFF")
    for row_index, values in enumerate(rows):
        cells = table.add_row().cells
        for index, value in enumerate(values):
            cell = cells[index]
            set_cell_margins(cell)
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
            if row_index % 2 == 1:
                set_cell_shading(cell, "F1F6F2")
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER if index < 2 else WD_ALIGN_PARAGRAPH.LEFT
            r = p.add_run(str(value))
            set_run_font(r, size=10)
    if widths:
        for row in table.rows:
            for index, width in enumerate(widths):
                row.cells[index].width = Inches(width)
    for row in table.rows:
        tr_pr = row._tr.get_or_add_trPr()
        tr_pr.append(OxmlElement("w:cantSplit"))
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return table


def add_code_lines(doc, lines):
    for line in lines:
        paragraph = doc.add_paragraph()
        paragraph.paragraph_format.left_indent = Inches(0.32)
        paragraph.paragraph_format.space_after = Pt(1)
        run = paragraph.add_run(line)
        set_run_font(run, east_asia="等距更纱黑体 SC", western="Menlo", size=9.2)


doc = Document()
section = doc.sections[0]
section.page_width = Inches(8.5)
section.page_height = Inches(11)
section.top_margin = Inches(0.78)
section.bottom_margin = Inches(0.72)
section.left_margin = Inches(0.86)
section.right_margin = Inches(0.86)

styles = doc.styles
normal = styles["Normal"]
normal.font.name = "Hiragino Sans GB"
normal.font.size = Pt(11)
normal._element.rPr.rFonts.set(qn("w:ascii"), "Hiragino Sans GB")
normal._element.rPr.rFonts.set(qn("w:hAnsi"), "Hiragino Sans GB")
normal._element.rPr.rFonts.set(qn("w:eastAsia"), "Hiragino Sans GB")
for style_name in ("Title", "Heading 1", "Heading 2"):
    style = styles[style_name]
    style.font.color.rgb = RGBColor(0, 0, 0)
    style.font.name = "Hiragino Sans GB"
    style._element.rPr.rFonts.set(qn("w:ascii"), "Hiragino Sans GB")
    style._element.rPr.rFonts.set(qn("w:hAnsi"), "Hiragino Sans GB")
    style._element.rPr.rFonts.set(qn("w:eastAsia"), "Hiragino Sans GB")
    p_pr = style._element.get_or_add_pPr()
    p_bdr = p_pr.find(qn("w:pBdr"))
    if p_bdr is not None:
        p_pr.remove(p_bdr)

# Cover
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
p.paragraph_format.space_before = Pt(86)
r = p.add_run("山东大学软件学院")
set_run_font(r, east_asia="黑体", size=18, bold=True)

p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
p.paragraph_format.space_before = Pt(54)
p.style = doc.styles["Title"]
r = p.add_run("面向对象技术课程实验报告")
set_run_font(r, east_asia="黑体", size=26, bold=True)

p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
p.paragraph_format.space_before = Pt(20)
r = p.add_run("基于 JavaFX 的保卫水果手动瞄准游戏设计与实现")
set_run_font(r, east_asia="黑体", size=17, bold=True)

cover_table = doc.add_table(rows=5, cols=2)
cover_table.alignment = WD_TABLE_ALIGNMENT.CENTER
cover_table.autofit = False
labels = ["课程名称", "学生姓名", "学号班级", "小组成员与分工", "完成日期"]
values = existing_cover_values or ["面向对象技术", "待填写", "待填写", "待填写", "待填写"]
for i, (label, value) in enumerate(zip(labels, values)):
    cover_table.rows[i].cells[0].width = Inches(1.75)
    cover_table.rows[i].cells[1].width = Inches(3.65)
    for cell in cover_table.rows[i].cells:
        cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
        set_cell_margins(cell, top=160, bottom=160)
    p0 = cover_table.rows[i].cells[0].paragraphs[0]
    p0.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    r0 = p0.add_run(label)
    set_run_font(r0, east_asia="黑体", size=11, bold=True)
    p1 = cover_table.rows[i].cells[1].paragraphs[0]
    r1 = p1.add_run(value)
    set_run_font(r1, size=11)
for row in cover_table.rows:
    for cell in row.cells:
        tc_pr = cell._tc.get_or_add_tcPr()
        borders = OxmlElement("w:tcBorders")
        for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
            tag = OxmlElement(f"w:{edge}")
            tag.set(qn("w:val"), "nil")
            borders.append(tag)
        tc_pr.append(borders)

doc.add_page_break()

add_heading(doc, "摘要", 1)
add_body(doc, "本项目针对《面向对象技术》课程实验要求，设计并实现了一款基于 JavaFX 的桌面水果防守游戏。玩家切换具有不同攻击方式的水果英雄，从战场中央瞄准来袭怪物并手动投放弹药，阻止怪物到达果仓。系统实现八个关卡、八种水果武器、四种怪物、不同波数配置、积分金币、武器升级、八套专属大招、敌人出场说明、背景音乐、关卡解锁和最高分存档。")
add_body(doc, "项目采用 Java 25、JavaFX 25.0.2、Maven 和 JUnit 5。设计中使用抽象类、继承、多态和封装组织领域对象，并通过工厂、门面和仓库等模式降低模块耦合。自动化测试覆盖移动、伤害、奖励、经济、能量与对象创建等核心规则。实验结果表明，系统满足教学大纲对水果介绍、怪物介绍、两关以上塔防及击杀积分的全部要求。")
add_body(doc, "关键词：JavaFX；手动瞄准；面向对象；设计模式；多态")

add_heading(doc, "目录", 1)
for item in [
    "1 实验背景与目标", "2 需求分析", "3 系统总体设计", "4 面向对象设计",
    "5 关键功能实现", "6 测试与运行结果", "7 总结与展望"
]:
    p = doc.add_paragraph(item)
    p.paragraph_format.left_indent = Inches(0.25)
    set_paragraph_spacing(p, after=5)
    for run in p.runs:
        set_run_font(run)

doc.add_page_break()

add_heading(doc, "1 实验背景与目标", 1)
add_body(doc, "《面向对象技术》课程强调使用抽象性、封装性、继承性和多态性分析并实现相对完整的应用。本实验选择“保卫水果”题目，按照教学大纲中的“投放不同水果瞄准怪物”实现手动射击玩法，并把面向对象思想落实到类职责、对象协作和扩展机制中。")
add_heading(doc, "1.1 实验目标", 2)
for text in [
    "掌握从需求分析、类建模到编码测试的完整开发过程。",
    "使用面向对象语言实现模块化、可复用和易扩展的软件结构。",
    "实现水果和怪物介绍、至少两关塔防、击杀积分等教学大纲硬性要求。",
    "通过自动化测试和实际运行验证游戏核心规则。",
]:
    add_bullet(doc, text)

add_heading(doc, "1.2 开发环境", 2)
add_table(doc, ["项目", "配置", "用途"], [
    ["操作系统", "macOS ARM64", "开发与运行环境"],
    ["Java", "Oracle JDK 25.0.2 LTS", "面向对象程序实现"],
    ["界面框架", "JavaFX 25.0.2", "窗口、控件、Canvas 和动画循环"],
    ["构建工具", "Maven 3.9.16", "依赖管理、测试与打包"],
    ["测试框架", "JUnit 5.11.4", "核心规则自动化测试"],
], [1.25, 2.2, 3.1])

add_heading(doc, "2 需求分析", 1)
add_heading(doc, "2.1 基本需求", 2)
add_body(doc, "玩家从原创封面进入关卡选择，在战斗中切换水果英雄并移动准星。点击怪物会形成目标锁定并发射水果弹药，点击空地则向指定落点投掷，需要预判怪物移动。怪物按波次沿路线前往果仓；生命值归零时失败，消灭全部波次时胜利。")
add_table(doc, ["教学要求", "项目实现", "完成情况"], [
    ["水果介绍", "8 种水果，展示名称、伤害、冷却、弹速和特性", "已完成"],
    ["怪物介绍", "4 种怪物，展示名称、速度、血量和奖励", "已完成"],
    ["2 关以上", "8 个独立关卡，各关设置 3 至 8 个波次", "已完成"],
    ["击杀积分", "按怪物类型结算积分、金币和技能能量", "已完成"],
], [1.5, 4.0, 1.0])

add_heading(doc, "2.2 扩展需求", 2)
for text in [
    "水果武器支持三级升级，八种水果各有独立专属大招。",
    "英雄与弹药从战场中下方中央位置发射，快捷键 1 至 8 切换水果。",
    "怪物首次出场时展示名称、血量、速度、积分和特点。",
    "支持暂停、继续和二倍速，提供键盘快捷键。",
    "保存各关最高分、最高星级和解锁进度，重新启动后仍然有效。",
    "提供原创封面、关卡选择、角色图鉴、背景音乐和结算界面。",
]:
    add_bullet(doc, text)

doc.add_page_break()

add_heading(doc, "3 系统总体设计", 1)
add_heading(doc, "3.1 分层结构", 2)
add_body(doc, "系统分为界面层、游戏控制层和领域模型层。界面层接收输入并显示状态，不直接修改伤害、金币等规则；控制层统一组织关卡、波次、战斗和胜负流程；模型层保存游戏对象的状态与行为。")
add_table(doc, ["层次", "主要类", "职责"], [
    ["界面层", "GameView、GameCanvas", "组织 JavaFX 控件、接收输入、绘制地图与对象"],
    ["控制层", "GameController", "协调游戏状态、波次、经济、战斗与胜负"],
    ["服务与配置", "LevelRepository、ScoreManager、SaveManager、MusicManager", "提供关卡、积分、存档和音乐"],
    ["领域模型", "Enemy、FruitWeapon、Projectile", "封装移动、瞄准、冷却、伤害和命中"],
], [1.15, 2.5, 3.4])

add_heading(doc, "3.2 游戏流程", 2)
for step in [
    "用户选择已解锁关卡，GameController 初始化生命值、金币和波次状态。",
    "玩家切换水果并点击战场，控制器查找准星附近怪物并创建投射物。",
    "动画循环按时间增量更新波次生成、怪物移动、水果冷却与投射物运动。",
    "投射物命中后根据攻击类型执行单体伤害、范围伤害或减速效果。",
    "怪物死亡时结算一次奖励；怪物到达终点时扣除生命值。",
    "全部波次清空则胜利并解锁下一关，生命值归零则失败。",
]:
    add_bullet(doc, step)

add_heading(doc, "3.3 关卡数据", 2)
add_table(doc, ["关卡", "名称", "波数", "初始金币", "特点"], [
    ["1", "青苹果原野", "3", "290", "新手练习"],
    ["2", "葡萄峡谷", "4", "310", "混合敌群"],
    ["3", "西瓜火山", "5", "340", "重甲推进"],
    ["4", "草莓花径", "4", "390", "密集短波，适合连锁"],
    ["5", "冰蓝湖畔", "6", "440", "高速突袭，适合减速"],
    ["6", "菠萝古堡", "5", "500", "直线重甲，适合贯穿"],
    ["7", "蜜桃云境", "7", "560", "持久防守，适合恢复"],
    ["8", "丰收王庭", "8", "640", "混合终局挑战"],
], [0.65, 1.4, 0.65, 1.0, 2.8])

add_heading(doc, "4 面向对象设计", 1)
add_heading(doc, "4.1 抽象与继承", 2)
add_body(doc, "Enemy 是所有怪物的抽象基类，统一封装生命值、速度、路径移动、伤害、减速和奖励领取状态。SlimeEnemy、FoxEnemy、BoarEnemy 和 BossEnemy 通过继承复用通用逻辑。FruitWeapon 是水果武器抽象基类，封装攻击冷却、等级和升级费用，具体子类只需实现投射物创建方式。")

add_heading(doc, "4.2 多态", 2)
add_body(doc, "GameController 保存 FruitWeapon 与 Enemy 抽象类型集合。玩家点击后统一调用 FruitWeapon.fire，fire 内部通过抽象方法 createProjectile 动态分派到具体子类。新增普通攻击方式时无需修改发射流程，体现了面向抽象编程和开闭原则。")

add_heading(doc, "4.3 封装", 2)
add_body(doc, "模型字段均保持私有，外部只能通过受约束的方法改变状态。例如 takeDamage 保证生命值不会小于零，spendCoins 防止透支，claimReward 保证怪物奖励只能领取一次，consumeUltimate 保证能量未满时不能释放技能。")

add_heading(doc, "4.4 设计模式", 2)
add_table(doc, ["模式", "应用位置", "作用"], [
    ["工厂模式", "EnemyFactory、FruitWeaponFactory", "隔离具体子类构造过程"],
    ["门面模式", "GameController", "向界面提供统一游戏操作入口"],
    ["仓库模式", "LevelRepository", "集中管理关卡、路线和波次数据"],
    ["状态模式思想", "GameState", "明确菜单、运行、暂停、胜利和失败状态"],
], [1.25, 2.3, 3.4])

doc.add_page_break()

add_heading(doc, "5 关键功能实现", 1)
add_heading(doc, "5.1 游戏循环", 2)
add_body(doc, "JavaFX AnimationTimer 在每帧调用 GameController.update。控制器将不超过 0.25 秒的有效时间拆为最多 0.05 秒的小步，完整处理二倍速传入的时间。CombatFeedback 把伤害与奖励事件交给 Canvas 绘制飘字和冲击波，规则层不依赖绘图对象。")
add_code_lines(doc, [
    "controller.update(deltaSeconds * speed);",
    "canvas.render(deltaSeconds);",
    "refreshHud();",
])

add_heading(doc, "5.2 路径移动", 2)
add_body(doc, "地图路径由多个 Vec2 坐标点组成。怪物记录当前线段编号和线段内进度，根据速度和帧时间计算本帧距离。当剩余距离超过当前线段时，算法进入下一线段继续计算，从而适应任意折线路径。")

add_heading(doc, "5.3 手动瞄准与攻击", 2)
add_body(doc, "GameCanvas 持续根据鼠标位置绘制准星和发射引导线。所有水果从 900×560 战场的中央偏下位置 (450, 485) 发射，英雄面板与弹道起点使用同一坐标。玩家点击怪物时形成锁定，点击空地则按指定落点飞行。西瓜造成范围伤害，冰柠施加减速，草莓连锁三个目标，菠萝贯穿三个目标且不会重复命中，蓝莓造成范围减速，蜜桃发射高伤花瓣弹。")

add_body(doc, "点击模式支持按住左键持续发射；按 M 切换弹弓模式后，从中央拖动并松手，按拉动向量的反方向计算落点。伤害系数为 0.75 + 0.75 × 蓄力比例，最高 1.5 倍。图鉴和说明窗口自动暂停并恢复战斗；暂停时不能射击、升级或消费积分。")
add_heading(doc, "5.4 波次管理", 2)
add_body(doc, "WaveConfig 由多个 WaveEntry 组成，每个条目记录怪物类型、数量和生成间隔。控制器依次生成条目，当前波全部怪物被消灭后等待 2.2 秒再进入下一波。关卡难度系数同时影响怪物生命值和速度。")

add_heading(doc, "5.5 积分与技能", 2)
add_body(doc, "ScoreManager 独立保存积分、金币和能量。怪物死亡后增加奖励并补充技能能量。能量达到 100 时，当前水果可释放专属大招：橙子前排重击、葡萄全场弹幕、西瓜密集爆破、柠檬全场减速、草莓五目标连锁、菠萝重甲破阵、蓝莓范围霜环，以及蜜桃生命恢复与全场花瓣伤害。蜜桃每次恢复两点果仓生命，上限为八点。")

add_body(doc, "击杀另增加可消费积分，与总成绩分开记账。寒霜风暴、果实炸弹、果仓修复分别消费 120、180、200 分；控制器先检查状态、目标与生命上限，再扣费执行。消费不会降低总成绩，也不能透支。")
add_heading(doc, "5.6 本地存档", 2)
add_body(doc, "SaveManager 使用 JDK Preferences 保存总最高分、每关最高分、最高星级和解锁关卡。胜利时剩余 7 至 8 点生命为三星，4 至 6 点为二星，1 至 3 点为一星；重玩较低成绩不会降低纪录。BattleStatistics 统计发射数、命中数、击败数和用时；一次范围命中只计一次射击命中。存档兼容旧版进度。")

add_heading(doc, "6 测试与运行结果", 1)
add_heading(doc, "6.1 自动化测试", 2)
add_table(doc, ["测试类", "测试内容", "结果"], [
    ["EnemyTest", "路径移动、伤害下限、奖励防重复、减速效果", "通过"],
    ["ScoreManagerTest", "金币消费、击杀奖励、能量累积与消耗", "通过"],
    ["ArchitectureTest", "课程要求数据、工厂多态创建", "通过"],
    ["GameControllerTest", "武器升级、中央发射、八种大招与生命恢复上限", "通过"],
    ["NewFruitWeaponTest", "连锁距离、贯穿不重复命中、范围减速", "通过"],
    ["CampaignFlowTest", "八关流程、暂停、波次、二倍速、积分道具", "通过"],
    ["SaveManagerTest", "旧版兼容、星级与分数不会被较低成绩覆盖", "通过"],
], [1.45, 4.5, 0.8])
add_body(doc, "执行 mvn test 共运行 27 项测试，失败 0 项，错误 0 项，跳过 0 项。八关流程测试使用内存存档和模拟清场，不修改玩家记录，验证波次、胜负和解锁逻辑，不代替人工难度评测。执行 mvn clean package 完成编译、测试与打包。")

add_heading(doc, "6.2 功能结果", 2)
for text in [
    "八个关卡分别配置独立路线，波数依次为 3、4、5、4、6、5、7、8。",
    "中央发射、点击连发、弹弓蓄力和八种水果攻击效果均有对应实现。",
    "四种怪物按配置表现出不同血量、速度、体积和奖励。",
    "武器选择、升级、八种专属大招、积分、金币和能量均通过规则测试。",
    "原创封面、关卡选择、敌人出场说明和背景音乐能够正常工作。",
    "胜利后保存星级并可进入下一关，失败后可重新挑战，各关最高分能够持久保存。",
]:
    add_bullet(doc, text)

add_heading(doc, "6.3 运行方法", 2)
add_body(doc, "在项目根目录执行 mvn javafx:run 启动游戏。首次运行时 Maven 会自动下载 JavaFX 依赖。执行 mvn test 运行单元测试，执行 mvn clean package 生成构建产物。")

add_heading(doc, "7 总结与展望", 1)
add_body(doc, "本实验完成了一个可运行、可交互且具有完整胜负流程的手动瞄准水果防守游戏，并满足教学大纲的全部功能要求。通过抽象基类和多态，将不同怪物与水果武器纳入统一更新流程；通过工厂与分层设计，隔离对象创建、游戏规则和界面绘制；通过单元测试验证关键领域规则。")
add_body(doc, "当前系统仍可继续扩展。后续可以增加命中连击、随机掉落道具、更多 Boss 阶段、成就系统以及联网排行榜。由于核心规则已经与界面分离，这些功能可以在不大规模改写已有代码的情况下逐步加入。")

# Footer page number
for section in doc.sections:
    footer = section.footer
    p = footer.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run("第 ")
    set_run_font(run, size=9, color="666666")
    fld_char1 = OxmlElement("w:fldChar")
    fld_char1.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = "PAGE"
    fld_char2 = OxmlElement("w:fldChar")
    fld_char2.set(qn("w:fldCharType"), "end")
    run._r.append(fld_char1)
    run._r.append(instr)
    run._r.append(fld_char2)
    run2 = p.add_run(" 页")
    set_run_font(run2, size=9, color="666666")

OUTPUT.parent.mkdir(parents=True, exist_ok=True)
doc.save(OUTPUT)
print(OUTPUT)
