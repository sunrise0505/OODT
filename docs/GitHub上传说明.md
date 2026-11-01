# 上传到 GitHub

建议仓库名：`fruit-guard`。

建议仓库描述：`Java 25 + JavaFX 水果防守游戏：8 关、8 种水果、手动瞄准、专属技能与本地存档。`

## 首次上传

1. 在 GitHub 创建空仓库。先不勾选自动创建 README、`.gitignore` 或许可证，项目已有前两项。
2. 在本项目根目录打开终端，先运行 `mvn clean verify`，确认构建通过。
3. 如果当前文件夹还没有 Git 仓库，执行下面的初始化命令；已有仓库则沿用原有分支和历史。

```bash
git init -b main
git add .
git status
git diff --cached --stat
```

暂存列表应包含 `src/`、`docs/`、`tools/`、`.github/`、README 和构建配置，不应包含 `target/`、IDE 配置或临时文件。保留 Markdown 与 Word 报告供课程验收；如果报告之后填写了姓名、学号等信息，发布公开仓库前按自己的需要处理这些字段。

确认暂存内容后创建首次提交：

```bash
git commit -m "Initial commit: Fruit Guard JavaFX game"
```

将下一条命令中的 `YOUR_USERNAME` 和 `fruit-guard` 换成实际账号和仓库名，再关联空仓库并上传：

```bash
git remote add origin https://github.com/YOUR_USERNAME/fruit-guard.git
git push -u origin main
```

## 上传后的检查

- 仓库首页应正确显示 README 封面图。
- 源码、五张 PNG、CSS、测试和文档应齐全，`target/` 不应出现在仓库中。
- 在 Actions 页面查看 `Java build and tests`，等待 Linux、Windows 两项任务结束。尚未上传时，无法预先确认云端构建结果。
- 从新目录克隆后执行 `mvn javafx:run`，检查能否打开封面并开始第一关。

## 后续更新

```bash
mvn clean verify
git add .
git diff --cached --stat
git commit -m "Describe the changes"
git push
```

项目没有预设开源许可证。公开源码和授权他人使用是不同的决定；需要开源授权时，再根据自己的用途选择许可证。

自动构建配置依据官方 [checkout](https://github.com/actions/checkout) 和 [setup-java](https://github.com/actions/setup-java) 的使用说明，使用 Java 25、Maven 缓存和只读仓库权限。
