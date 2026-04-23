package io.github.xororz.localdream.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.xororz.localdream.data.PromptCategory
import io.github.xororz.localdream.data.PromptData
import io.github.xororz.localdream.data.PromptGroup
import io.github.xororz.localdream.data.PromptTag
import io.github.xororz.localdream.data.PromptTagRepository
import kotlinx.coroutines.launch

private const val TAG = "PromptTagSelector"

/**
 * 提示词标签选择器对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptTagSelectorDialog(
    onDismiss: () -> Unit,
    onTagSelected: (String) -> Unit,  // 每次点击都会调用，传入新的完整prompt
    currentPrompt: String = ""
) {
    Log.d(TAG, "PromptTagSelectorDialog 打开，当前prompt: $currentPrompt")
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { PromptTagRepository(context) }
    
    var promptData by remember { mutableStateOf<PromptData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchMode by remember { mutableStateOf(false) }
    
    // 使用 mutableStateOf 来跟踪当前的 prompt，这样可以响应外部变化
    var internalPrompt by remember { mutableStateOf(currentPrompt) }
    
    // 当外部 currentPrompt 变化时，更新内部状态
    LaunchedEffect(currentPrompt) {
        Log.d(TAG, "外部 currentPrompt 变化: '$currentPrompt'")
        internalPrompt = currentPrompt
    }
    
    // 当前prompt中的标签集合（用于判断是否已存在）
    val currentTags = remember(internalPrompt) {
        internalPrompt.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
    
    Log.d(TAG, "当前标签: $currentTags")

    // 加载数据
    LaunchedEffect(Unit) {
        Log.d(TAG, "开始加载提示词数据")
        scope.launch {
            promptData = repository.loadPromptData()
            isLoading = false
            Log.d(TAG, "数据加载完成，分类数: ${promptData?.categories?.size ?: 0}")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "选择提示词",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = { isSearchMode = !isSearchMode }) {
                            Icon(Icons.Default.Search, "搜索")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "关闭")
                        }
                    }
                }

                // 搜索框
                AnimatedVisibility(visible = isSearchMode) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        placeholder = { Text("搜索提示词...") },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, "清除")
                                }
                            }
                        }
                    )
                }

                // 自定义标签输入（支持粘贴时按逗号自动分割）
                var customTagInput by remember { mutableStateOf("") }
                val addCustomTags: () -> Unit = {
                    val parts = customTagInput.split(",", "，")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    if (parts.isNotEmpty()) {
                        val existing = internalPrompt.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .toMutableList()
                        parts.forEach { p ->
                            if (!existing.contains(p)) existing.add(p)
                        }
                        val newPrompt = existing.joinToString(", ")
                        internalPrompt = newPrompt
                        onTagSelected(newPrompt)
                        customTagInput = ""
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customTagInput,
                        onValueChange = { newValue ->
                            // 粘贴/输入时若包含逗号，自动分割并加入
                            if (newValue.contains(",") || newValue.contains("，")) {
                                val parts = newValue.split(",", "，")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                if (parts.isNotEmpty()) {
                                    val existing = internalPrompt.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                        .toMutableList()
                                    parts.forEach { p ->
                                        if (!existing.contains(p)) existing.add(p)
                                    }
                                    val newPrompt = existing.joinToString(", ")
                                    internalPrompt = newPrompt
                                    onTagSelected(newPrompt)
                                }
                                customTagInput = ""
                            } else {
                                customTagInput = newValue
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("自定义标签（逗号分隔可批量添加）") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = { addCustomTags() }
                        )
                    )
                    FilledTonalIconButton(
                        onClick = addCustomTags,
                        enabled = customTagInput.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, "添加")
                    }
                }

                Divider()

                // 内容区域
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        promptData == null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("加载失败")
                            }
                        }
                        searchQuery.isNotEmpty() -> {
                            // 搜索结果
                            val searchResults = remember(searchQuery, promptData) {
                                repository.searchTags(searchQuery, promptData!!)
                            }
                            SearchResultsList(
                                results = searchResults,
                                currentTags = currentTags,
                                onTagClick = { tag ->
                                    Log.d(TAG, "=== 点击标签: ${tag.english} ===")
                                    Log.d(TAG, "内部prompt: '$internalPrompt'")
                                    
                                    val tagList = internalPrompt.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                        .toMutableList()
                                    
                                    Log.d(TAG, "解析后的标签列表: $tagList")
                                    Log.d(TAG, "标签是否存在: ${tagList.contains(tag.english)}")
                                    
                                    if (tagList.contains(tag.english)) {
                                        // 已存在，删除
                                        Log.d(TAG, "删除标签: ${tag.english}")
                                        tagList.remove(tag.english)
                                    } else {
                                        // 不存在，添加到末尾
                                        Log.d(TAG, "添加标签: ${tag.english}")
                                        tagList.add(tag.english)
                                    }
                                    
                                    // 立即更新文本框
                                    val newPrompt = tagList.joinToString(", ")
                                    Log.d(TAG, "新的prompt: '$newPrompt'")
                                    internalPrompt = newPrompt  // 更新内部状态
                                    Log.d(TAG, "=== 调用 onTagSelected ===")
                                    onTagSelected(newPrompt)
                                }
                            )
                        }
                        else -> {
                            // 分类列表
                            CategoryList(
                                data = promptData!!,
                                currentTags = currentTags,
                                onTagClick = { tag ->
                                    Log.d(TAG, "=== 点击标签: ${tag.english} ===")
                                    Log.d(TAG, "内部prompt: '$internalPrompt'")
                                    
                                    val tagList = internalPrompt.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                        .toMutableList()
                                    
                                    Log.d(TAG, "解析后的标签列表: $tagList")
                                    Log.d(TAG, "标签是否存在: ${tagList.contains(tag.english)}")
                                    
                                    if (tagList.contains(tag.english)) {
                                        // 已存在，删除
                                        Log.d(TAG, "删除标签: ${tag.english}")
                                        tagList.remove(tag.english)
                                    } else {
                                        // 不存在，添加到末尾
                                        Log.d(TAG, "添加标签: ${tag.english}")
                                        tagList.add(tag.english)
                                    }
                                    
                                    // 立即更新文本框
                                    val newPrompt = tagList.joinToString(", ")
                                    Log.d(TAG, "新的prompt: '$newPrompt'")
                                    internalPrompt = newPrompt  // 更新内部状态
                                    Log.d(TAG, "=== 调用 onTagSelected ===")
                                    onTagSelected(newPrompt)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 分类列表
 */
@Composable
private fun CategoryList(
    data: PromptData,
    currentTags: Set<String>,
    onTagClick: (PromptTag) -> Unit
) {
    Log.d(TAG, "CategoryList 渲染，分类数: ${data.categories.size}")
    
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var expandedGroup by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        data.categories.forEach { category ->
            item {
                CategoryItem(
                    category = category,
                    isExpanded = expandedCategory == category.name,
                    expandedGroup = expandedGroup,
                    currentTags = currentTags,
                    onCategoryClick = {
                        Log.d(TAG, "点击分类: ${category.name}")
                        expandedCategory = if (expandedCategory == category.name) {
                            null
                        } else {
                            category.name
                        }
                        expandedGroup = null
                    },
                    onGroupClick = { groupName ->
                        Log.d(TAG, "点击分组: $groupName")
                        expandedGroup = if (expandedGroup == groupName) {
                            null
                        } else {
                            groupName
                        }
                    },
                    onTagClick = onTagClick
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * 分类项
 */
@Composable
private fun CategoryItem(
    category: PromptCategory,
    isExpanded: Boolean,
    expandedGroup: String?,
    currentTags: Set<String>,
    onCategoryClick: () -> Unit,
    onGroupClick: (String) -> Unit,
    onTagClick: (PromptTag) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // 分类标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCategoryClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                                  else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 分组列表
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    category.groups.forEach { group ->
                        GroupItem(
                            group = group,
                            isExpanded = expandedGroup == group.name,
                            currentTags = currentTags,
                            onGroupClick = { onGroupClick(group.name) },
                            onTagClick = onTagClick
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

/**
 * 分组项
 */
@Composable
private fun GroupItem(
    group: PromptGroup,
    isExpanded: Boolean,
    currentTags: Set<String>,
    onGroupClick: () -> Unit,
    onTagClick: (PromptTag) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // 分组标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onGroupClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                                  else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            // 标签列表
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    // 使用简单的网格布局（每行3个）
                    var currentRow = mutableListOf<PromptTag>()
                    group.tags.forEachIndexed { index, tag ->
                        currentRow.add(tag)
                        if (currentRow.size == 3 || index == group.tags.lastIndex) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                currentRow.forEach { t ->
                                    TagChip(
                                        tag = t,
                                        isSelected = currentTags.contains(t.english),
                                        onClick = { onTagClick(t) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // 填充空白
                                repeat(3 - currentRow.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            currentRow = mutableListOf()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 标签芯片
 */
@Composable
private fun TagChip(
    tag: PromptTag,
    isSelected: Boolean,  // 是否在当前prompt中
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Text(
            text = tag.chinese,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 3.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * 搜索结果列表
 */
@Composable
private fun SearchResultsList(
    results: List<Pair<PromptTag, String>>,
    currentTags: Set<String>,
    onTagClick: (PromptTag) -> Unit
) {
    if (results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("未找到匹配的提示词")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(results) { (tag, path) ->
                val isSelected = currentTags.contains(tag.english)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable { onTagClick(tag) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tag.chinese,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = path,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "已选中",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
