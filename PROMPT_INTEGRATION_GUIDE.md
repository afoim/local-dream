# 提示词插件集成指南

## 📝 概述

本指南将帮助你将 sd-webui-prompt-all-in-one 提示词插件移植到 LocalDream Android App 中。

## ✅ 已完成的工作

### 1. 数据模型 (已创建)
- ✅ `PromptTag.kt` - 提示词标签数据模型
- ✅ `PromptTagRepository.kt` - 提示词数据仓库

### 2. 数据转换 (已完成)
- ✅ `convert_yaml_to_json.py` - YAML到JSON转换脚本
- ✅ `app/src/main/assets/prompts/zh_CN.json` - 转换后的JSON数据文件 (192KB)

### 3. UI界面 (已创建)
- ✅ `PromptTagSelectorScreen.kt` - 提示词选择器对话框

## 🔧 需要手动完成的步骤

### 步骤1: 集成到ModelRunScreen

在 `ModelRunScreen.kt` 中找到prompt输入框部分(大约在1470行左右)，进行以下修改:

#### 1.1 添加状态变量

在函数开头添加:

```kotlin
var showPromptSelector by remember { mutableStateOf(false) }
var showNegativePromptSelector by remember { mutableStateOf(false) }
```

#### 1.2 修改Prompt输入框

将现有的 `OutlinedTextField` 包装在 `Row` 中，添加一个按钮:

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
            .weight(1f)  // 改为占据剩余空间
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
    
    // 新增: 提示词选择按钮
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

#### 1.3 对Negative Prompt做同样的修改

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
            .weight(1f)
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
    
    // 新增: 负面提示词选择按钮
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

#### 1.4 添加对话框

在 `ModelRunScreen` 函数的末尾(return之前)添加:

```kotlin
// 提示词选择器对话框
if (showPromptSelector) {
    PromptTagSelectorDialog(
        onDismiss = { showPromptSelector = false },
        onTagSelected = { tag ->
            // 将选中的标签添加到prompt中
            val newPrompt = if (prompt.isBlank()) {
                tag
            } else {
                "$prompt, $tag"
            }
            onPromptChange(newPrompt)
        }
    )
}

// 负面提示词选择器对话框
if (showNegativePromptSelector) {
    PromptTagSelectorDialog(
        onDismiss = { showNegativePromptSelector = false },
        onTagSelected = { tag ->
            // 将选中的标签添加到negativePrompt中
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

### 步骤2: 添加导入语句

在 `ModelRunScreen.kt` 文件顶部添加:

```kotlin
import io.github.xororz.localdream.ui.screens.PromptTagSelectorDialog
```

### 步骤3: 添加字符串资源(可选)

如果你想要本地化按钮文字，可以在 `app/src/main/res/values/strings.xml` 中添加:

```xml
<string name="select_prompt">选择提示词</string>
<string name="select_negative_prompt">选择负面提示词</string>
```

## 🎨 功能特性

### 已实现的功能:
1. ✅ 三级分类结构(类别 > 分组 > 标签)
2. ✅ 中英文双语显示
3. ✅ 搜索功能(支持中英文搜索)
4. ✅ 可折叠的分类和分组
5. ✅ 点击标签自动添加到输入框
6. ✅ 自动处理逗号分隔

### UI特点:
- Material Design 3 风格
- 响应式布局
- 流畅的动画效果
- 搜索高亮
- 分类路径显示

## 📱 使用方法

1. 在生图界面,点击Prompt输入框旁边的 `+` 按钮
2. 浏览分类或使用搜索功能查找提示词
3. 点击想要的标签,会自动添加到输入框中
4. 多个标签会自动用逗号分隔

## 🔄 更新提示词数据

如果需要更新提示词数据:

1. 更新 `demo/sd-webui-prompt-all-in-one-main/group_tags/zh_CN.yaml`
2. 运行转换脚本:
   ```bash
   uv run convert_yaml_to_json.py
   ```
3. 重新编译App

## 🐛 故障排除

### 问题1: 找不到JSON文件
- 确保 `app/src/main/assets/prompts/zh_CN.json` 文件存在
- 检查文件路径是否正确

### 问题2: 编译错误
- 确保所有导入语句都已添加
- 检查Kotlin语法是否正确

### 问题3: 对话框不显示
- 检查状态变量是否正确设置
- 确保对话框代码在正确的位置

## 📚 代码结构

```
app/src/main/
├── assets/
│   └── prompts/
│       └── zh_CN.json          # 提示词数据
├── java/io/github/xororz/localdream/
│   ├── data/
│   │   ├── PromptTag.kt        # 数据模型
│   │   └── PromptTagRepository.kt  # 数据仓库
│   └── ui/screens/
│       ├── ModelRunScreen.kt   # 需要修改
│       └── PromptTagSelectorScreen.kt  # 新增UI
```

## 🎯 下一步

完成上述步骤后:
1. 编译并运行App
2. 测试提示词选择功能
3. 根据需要调整UI样式
4. 可以考虑添加收藏功能
5. 可以添加历史记录功能

## 💡 扩展建议

### 未来可以添加的功能:
1. 提示词收藏功能
2. 最近使用的提示词
3. 自定义提示词
4. 提示词权重调整
5. 提示词模板
6. 多语言支持(英文、日文等)

## 📞 需要帮助?

如果在集成过程中遇到问题:
1. 检查本文档的故障排除部分
2. 查看代码注释
3. 确保所有文件都已正确创建

祝你集成顺利! 🎉
