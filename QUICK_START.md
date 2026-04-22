# 🚀 快速开始指南

## 当前状态

✅ **已自动完成:**
1. 数据模型已创建 (`PromptTag.kt`, `PromptTagRepository.kt`)
2. UI界面已创建 (`PromptTagSelectorScreen.kt`)
3. YAML数据已转换为JSON (192KB)
4. 所有文件已放置在正确位置

## ⚠️ 需要你手动完成的3个步骤

由于你的电脑可能没有Android开发环境,我无法直接编译APK。但是集成非常简单,只需要3步:

### 步骤1: 打开Android Studio

1. 用Android Studio打开这个项目
2. 等待Gradle同步完成

### 步骤2: 修改ModelRunScreen.kt

打开文件: `app/src/main/java/io/github/xororz/localdream/ui/screens/ModelRunScreen.kt`

#### 2.1 在文件顶部添加导入(大约在第1行附近):

```kotlin
import io.github.xororz.localdream.ui.screens.PromptTagSelectorDialog
```

#### 2.2 在函数开头添加状态变量(大约在第350行,其他var声明附近):

```kotlin
var showPromptSelector by remember { mutableStateOf(false) }
var showNegativePromptSelector by remember { mutableStateOf(false) }
```

#### 2.3 找到Prompt输入框(搜索 "image_prompt",大约在第1470行)

**原代码:**
```kotlin
OutlinedTextField(
    value = prompt,
    onValueChange = onPromptChange,
    modifier = Modifier
        .fillMaxWidth()
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) { },
    label = { Text(stringResource(R.string.image_prompt)) },
    // ... 其他代码
)
```

**替换为:**
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.Top
) {
    OutlinedTextField(
        value = prompt,
        onValueChange = onPromptChange,
        modifier = Modifier
            .weight(1f)  // 改这里!
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { },
        label = { Text(stringResource(R.string.image_prompt)) },
        maxLines = if (expandedPrompt) Int.MAX_VALUE else 2,
        minLines = if (expandedPrompt) 3 else 2,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        trailingIcon = {
            IconButton(onClick = {
                expandedPrompt = !expandedPrompt
            }) {
                Icon(
                    if (expandedPrompt) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expandedPrompt) "collapse" else "expand"
                )
            }
        }
    )
    
    FilledTonalIconButton(
        onClick = { showPromptSelector = true },
        modifier = Modifier
            .padding(top = 8.dp)
            .size(48.dp)
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "选择提示词"
        )
    }
}
```

#### 2.4 对Negative Prompt做同样的修改(搜索 "negative_prompt")

**原代码:**
```kotlin
OutlinedTextField(
    value = negativePrompt,
    onValueChange = onNegativePromptChange,
    modifier = Modifier
        .fillMaxWidth()
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) { },
    label = { Text(stringResource(R.string.negative_prompt)) },
    // ... 其他代码
)
```

**替换为:**
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.Top
) {
    OutlinedTextField(
        value = negativePrompt,
        onValueChange = onNegativePromptChange,
        modifier = Modifier
            .weight(1f)  // 改这里!
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { },
        label = { Text(stringResource(R.string.negative_prompt)) },
        maxLines = if (expandedNegativePrompt) Int.MAX_VALUE else 2,
        minLines = if (expandedNegativePrompt) 3 else 2,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        trailingIcon = {
            IconButton(onClick = {
                expandedNegativePrompt = !expandedNegativePrompt
            }) {
                Icon(
                    if (expandedNegativePrompt) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expandedNegativePrompt) "collapse" else "expand"
                )
            }
        }
    )
    
    FilledTonalIconButton(
        onClick = { showNegativePromptSelector = true },
        modifier = Modifier
            .padding(top = 8.dp)
            .size(48.dp)
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "选择负面提示词"
        )
    }
}
```

#### 2.5 在文件末尾添加对话框(在最后一个 `}` 之前,大约在第3890行):

找到 `ModelRunScreen` 函数的最后,在所有其他对话框之后添加:

```kotlin
    // 提示词选择器
    if (showPromptSelector) {
        PromptTagSelectorDialog(
            onDismiss = { showPromptSelector = false },
            onTagSelected = { tag ->
                val newPrompt = if (prompt.isBlank()) {
                    tag
                } else {
                    "$prompt, $tag"
                }
                onPromptChange(newPrompt)
            }
        )
    }

    if (showNegativePromptSelector) {
        PromptTagSelectorDialog(
            onDismiss = { showNegativePromptSelector = false },
            onTagSelected = { tag ->
                val newNegativePrompt = if (negativePrompt.isBlank()) {
                    tag
                } else {
                    "$negativePrompt, $tag"
                }
                onNegativePromptChange(newNegativePrompt)
            }
        )
    }
```

### 步骤3: 编译运行

1. 在Android Studio中点击 "Build" -> "Make Project"
2. 如果没有错误,点击 "Run" 按钮
3. 选择你的设备或模拟器
4. 等待App安装并运行

## 🎉 完成!

现在你应该能看到:
- Prompt输入框旁边有一个 `+` 按钮
- 点击按钮会打开提示词选择器
- 可以浏览分类或搜索提示词
- 点击标签会自动添加到输入框

## 📸 预期效果

1. **主界面**: Prompt输入框右侧有一个圆形的 `+` 按钮
2. **点击按钮**: 弹出全屏对话框,显示提示词分类
3. **展开分类**: 点击分类名称展开,显示子分组
4. **展开分组**: 点击分组名称展开,显示标签列表
5. **选择标签**: 点击标签,自动添加到输入框并关闭对话框
6. **搜索功能**: 点击搜索图标,输入关键词快速查找

## ❓ 遇到问题?

### 编译错误?
- 确保所有导入语句都已添加
- 检查括号是否匹配
- 清理项目: Build -> Clean Project

### 找不到文件?
- 确保 `app/src/main/assets/prompts/zh_CN.json` 存在
- 重新运行转换脚本: `uv run convert_yaml_to_json.py`

### 按钮不显示?
- 检查是否正确用 `Row` 包装了 `OutlinedTextField`
- 确保 `FilledTonalIconButton` 在 `Row` 内部

### 对话框不弹出?
- 检查状态变量是否正确声明
- 确保对话框代码在正确位置
- 查看Logcat是否有错误信息

## 💡 提示

- 修改代码时建议使用Android Studio的搜索功能(Ctrl+F)快速定位
- 可以先只修改Prompt输入框,测试成功后再修改Negative Prompt
- 保存文件后Android Studio会自动提示重新编译

祝你成功! 🎊
