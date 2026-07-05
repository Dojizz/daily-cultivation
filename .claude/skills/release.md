---
name: release
description: 自动构建 release APK，创建 GitHub Release 并上传 APK。触发词："发布新版本"、"发布版本"、"发版"、"release"。
---

# 自动发布 Skill

自动执行以下流程：
1. 递增版本号（versionCode 自动 +1，versionName 按 semver 递增 patch）
2. 构建签名 release APK
3. 创建 git tag 并推送到 GitHub
4. 通过 GitHub API 创建 Release 并上传 APK

## 前置条件

- 用户需要提供 GitHub Personal Access Token（需要 `repo` 权限）
- Token 优先从环境变量 `GITHUB_TOKEN` 读取，如果未设置则询问用户

## 执行流程

### Step 1: 确认版本号

- 读取 `app/build.gradle.kts` 的 `defaultConfig` 中的 `versionCode` 和 `versionName`
- 默认建议：versionCode 自动 +1，versionName patch 自动 +1
- 询问用户确认，用户可指定不同的版本号

### Step 2: 执行发布脚本

运行 `scripts/release.sh <versionCode> <versionName> <token>`，该脚本会自动完成：
- 更新 `app/build.gradle.kts` 中的版本号
- 运行 `./gradlew assembleRelease` 构建签名 APK
- `git commit` + `git push`
- `git tag` + `git push origin <tag>`
- 通过 curl 调用 GitHub API 创建 Release 并上传 APK

### Step 3: 确认结果

- 告知用户发布成功
- 输出 Release 页面 URL：`https://github.com/Dojizz/daily-cultivation/releases/tag/v<VERSION_NAME>`
- 提醒用户打开手机 App 检查更新

## 版本号规则

| 层级 | 何时递增 | 示例 |
|------|---------|------|
| MAJOR | 重大架构变更或不兼容改动 | 0.1.0 → 1.0.0 |
| MINOR | 新增功能 | 0.1.0 → 0.2.0 |
| PATCH | Bug 修复/小改进（默认） | 0.1.0 → 0.1.1 |

## Token 设置

在 `.claude/settings.json` 中配置：

```json
{
  "env": {
    "GITHUB_TOKEN": "ghp_xxxxxxxxxxxx"
  }
}
```

或对话中临时提供：`export GITHUB_TOKEN=ghp_xxx && ./scripts/release.sh …`
