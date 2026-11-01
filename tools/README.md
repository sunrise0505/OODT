# 可选报告工具

本目录用于生成课程实验报告。运行游戏和 Java 测试不需要这些工具。

- `build_report.py`：用 `python-docx` 生成 `docs/保卫水果实验报告.docx`；如果已有报告，会保留符合预期格式的封面表格填写值。
- `requirements.txt`：报告生成脚本的 Python 依赖。
- `fontconfig-macos.xml`：报告渲染时可使用的 macOS 字体路径配置，不参与游戏运行或 Maven 构建。

在项目根目录执行：

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r tools/requirements.txt
python tools/build_report.py
```

Windows PowerShell 中，激活命令为 `.venv\Scripts\Activate.ps1`，其余命令相同。

生成脚本会覆盖 Word 报告正文。若直接在 Word 中修改过正文，请先备份；修改生成脚本后重新生成。报告字体配置主要针对 macOS，其他系统生成后应使用 Word 或 LibreOffice 检查字体与分页。
