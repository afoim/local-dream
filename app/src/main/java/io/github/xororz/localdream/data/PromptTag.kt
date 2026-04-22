package io.github.xororz.localdream.data

/**
 * 提示词标签数据模型
 */
data class PromptTag(
    val english: String,      // 英文提示词
    val chinese: String       // 中文翻译
)

/**
 * 二级分类
 */
data class PromptGroup(
    val name: String,         // 分类名称
    val color: String?,       // 颜色(可选)
    val tags: List<PromptTag> // 标签列表
)

/**
 * 一级分类
 */
data class PromptCategory(
    val name: String,                // 类别名称
    val groups: List<PromptGroup>    // 分组列表
)

/**
 * 根数据结构
 */
data class PromptData(
    val categories: List<PromptCategory>
)
