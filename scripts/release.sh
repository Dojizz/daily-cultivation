#!/bin/bash
set -euo pipefail

# ============================================
# Daily Cultivation 自动发布脚本
# 用法: ./scripts/release.sh <new_version_code> <new_version_name> <github_token>
# 示例: ./scripts/release.sh 2 "0.2.0" ghp_xxx
# ============================================

VERSION_CODE="${1:?请提供新的 versionCode}"
VERSION_NAME="${2:?请提供新的 versionName}"
GITHUB_TOKEN="${3:?请提供 GitHub Personal Access Token}"

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_FILE="$PROJECT_ROOT/app/build.gradle.kts"
OWNER="Dojizz"
REPO="daily-cultivation"

# ── Step 1: 更新版本号 ──────────────────────────────────────

CURRENT_CODE=$(grep 'versionCode' "$BUILD_FILE" | head -1 | grep -oE '[0-9]+')
CURRENT_NAME=$(grep 'versionName' "$BUILD_FILE" | head -1 | grep -oE '"[^"]*"' | tr -d '"')

if [ "$VERSION_CODE" -le "$CURRENT_CODE" ]; then
    echo "错误: versionCode 必须大于 $CURRENT_CODE"
    exit 1
fi

sed -i.bak \
    -e "s/versionCode = $CURRENT_CODE/versionCode = $VERSION_CODE/" \
    -e "s/versionName = \"$CURRENT_NAME\"/versionName = \"$VERSION_NAME\"/" \
    "$BUILD_FILE"
rm -f "$BUILD_FILE.bak"

echo "✓ 版本号已更新: $CURRENT_NAME ($CURRENT_CODE) → $VERSION_NAME ($VERSION_CODE)"

# ── Step 2: 构建 Release APK ─────────────────────────────────

cd "$PROJECT_ROOT"
echo "▶ 正在构建 release APK…"
./gradlew assembleRelease

APK_PATH=$(find "$PROJECT_ROOT/app/build/outputs/apk/release" -name "*.apk" ! -name "*unaligned*" | head -1)
if [ ! -f "$APK_PATH" ]; then
    echo "错误: 找不到 APK 文件"
    exit 1
fi
echo "✓ APK 已构建: $APK_PATH"

# ── Step 3: Git 提交并打 tag ──────────────────────────────────

TAG="v$VERSION_NAME"

git add "$BUILD_FILE"
git commit -m "release: bump version to $TAG (versionCode=$VERSION_CODE)"
git push origin HEAD

git tag -a "$TAG" -m "Release $TAG"
git push origin "$TAG"

echo "✓ Git tag $TAG 已推送"

# ── Step 4: 创建 GitHub Release ───────────────────────────────

RELEASE_BODY="Release $TAG"

RELEASE_RESPONSE=$(curl -s -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "Content-Type: application/json" \
    "https://api.github.com/repos/$OWNER/$REPO/releases" \
    -d "{
        \"tag_name\": \"$TAG\",
        \"name\": \"$TAG\",
        \"body\": \"$RELEASE_BODY\",
        \"draft\": false,
        \"prerelease\": false
    }")

RELEASE_ID=$(echo "$RELEASE_RESPONSE" | grep -o '"id": [0-9]*' | head -1 | grep -o '[0-9]*')
UPLOAD_URL=$(echo "$RELEASE_RESPONSE" | grep -o '"upload_url": "[^"]*"' | head -1 | sed 's/"upload_url": "//;s/"{?name,label}//;s/"//g')

if [ -z "$RELEASE_ID" ] || [ -z "$UPLOAD_URL" ]; then
    echo "错误: 创建 Release 失败"
    echo "$RELEASE_RESPONSE"
    exit 1
fi

echo "✓ GitHub Release 已创建 (ID=$RELEASE_ID)"

# ── Step 5: 上传 APK ─────────────────────────────────────────

APK_NAME="daily-cultivation-v${VERSION_NAME}.apk"

UPLOAD_RESPONSE=$(curl -s -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "Content-Type: application/octet-stream" \
    "${UPLOAD_URL}?name=${APK_NAME}" \
    --data-binary "@${APK_PATH}")

if echo "$UPLOAD_RESPONSE" | grep -q '"browser_download_url"'; then
    echo "✓ APK 已上传: $APK_NAME"
    echo "  下载链接: $(echo "$UPLOAD_RESPONSE" | grep -o '"browser_download_url": "[^"]*"' | head -1 | sed 's/"browser_download_url": "//;s/"//g')"
else
    echo "错误: APK 上传失败"
    echo "$UPLOAD_RESPONSE"
    exit 1
fi
echo ""
echo "═══════════════════════════════════════"
echo "  Release $TAG 发布完成！"
echo "  $TAG/releases/tag/$TAG"
echo "═══════════════════════════════════════"
