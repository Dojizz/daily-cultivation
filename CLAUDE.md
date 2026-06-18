# Daily Cultivation（日课）

个人成长记录与督促工具。核心理念：通过每日刻意练习和时限约束，推动个人行为改善。

## 项目目标

- **首期**：72 小时任务清单 —— 任何待办事项纳入清单即开始倒计时，必须在 72 小时内完成
- **后续**（见 `docs/backlog.md`）：日课练习、日记、备忘录/检查清单、财务预算等

## 技术栈

- 语言：Kotlin
- UI 框架：Jetpack Compose + Material 3
- 架构：MVVM + Repository 模式
- 本地存储：Room
- 构建工具：Gradle (Kotlin DSL)
- 最低 SDK：26 (Android 8.0)
- 目标 SDK：35

## 目录结构约定

```
daily-cultivation/
├── app/
│   ├── src/main/
│   │   ├── java/com/dailycultivation/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/          # Room DAO, entities, repository
│   │   │   ├── ui/            # Compose screens & components
│   │   │   │   ├── theme/
│   │   │   │   ├── home/      # 主页面（今日视图）
│   │   │   │   └── task/      # 72h 任务相关页面
│   │   │   └── viewmodel/     # ViewModel
│   │   └── res/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── docs/                      # 设计文档、需求讨论记录
└── CLAUDE.md
```

- 代码目录按功能模块分包（`data` / `ui` / `viewmodel`），不作按层的顶层分包
- Compose 组件文件命名：`XxxScreen.kt`（全屏页面）、`XxxCard.kt`（卡片组件），以此类推
- 新功能先在 `docs/` 写设计文档，再动手写代码

## 环境配置规则

- Gradle 仓库优先使用阿里云镜像（`maven.aliyun.com`），配置在 `settings.gradle.kts` 的 `dependencyResolutionManagement.repositories` 中
- Android SDK 路径使用本地已安装的版本

## 验证命令

```bash
# 构建 debug 版本
./gradlew assembleDebug

# 运行所有测试
./gradlew test

# 运行单个模块测试
./gradlew app:test

# 检查代码风格（后续配置 ktlint 后启用）
# ./gradlew ktlintCheck
```

## 开发纪律

- 每次改动后跑 `./gradlew assembleDebug` 确保能编译通过
- 新增数据模型时同步写 Room migration（如果已有发布版本）
- Compose 预览函数用 `@Preview` 标注，方便 UI 调试
- 不做 premature optimization —— 功能先跑通，再考虑性能
