package io.github.xororz.localdream.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 提示词标签仓库
 * 负责从assets加载和解析提示词数据
 */
class PromptTagRepository(private val context: Context) {

    private var cachedData: PromptData? = null

    companion object {
        private const val TAG = "PromptTagRepository"
    }

    /**
     * 加载提示词数据
     */
    suspend fun loadPromptData(): PromptData = withContext(Dispatchers.IO) {
        Log.d(TAG, "开始加载提示词数据")
        
        // 如果已缓存,直接返回
        cachedData?.let { 
            Log.d(TAG, "使用缓存数据")
            return@withContext it 
        }

        try {
            Log.d(TAG, "从assets读取JSON文件: prompts/zh_CN.json")
            
            // 从assets读取JSON文件
            val jsonString = context.assets.open("prompts/zh_CN.json").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            }

            Log.d(TAG, "JSON文件读取成功，长度: ${jsonString.length}")
            Log.d(TAG, "JSON前100字符: ${jsonString.take(100)}")

            // 解析JSON
            val jsonArray = JSONArray(jsonString)
            Log.d(TAG, "JSON解析成功，分类数量: ${jsonArray.length()}")
            
            val categories = mutableListOf<PromptCategory>()

            for (i in 0 until jsonArray.length()) {
                val categoryObj = jsonArray.getJSONObject(i)
                val categoryName = categoryObj.getString("name")
                Log.d(TAG, "解析分类 $i: $categoryName")
                
                val groupsArray = categoryObj.getJSONArray("groups")
                val groups = mutableListOf<PromptGroup>()

                for (j in 0 until groupsArray.length()) {
                    val groupObj = groupsArray.getJSONObject(j)
                    
                    // 跳过没有name字段的对象（如type: wrap）
                    if (!groupObj.has("name")) {
                        Log.d(TAG, "  跳过无效分组 $j (没有name字段)")
                        continue
                    }
                    
                    val groupName = groupObj.getString("name")
                    val color = groupObj.optString("color", null)
                    
                    // 检查是否有tags字段
                    if (!groupObj.has("tags")) {
                        Log.d(TAG, "  跳过分组 $j: $groupName (没有tags字段)")
                        continue
                    }
                    
                    val tagsObj = groupObj.getJSONObject("tags")
                    val tags = mutableListOf<PromptTag>()

                    // 遍历tags对象的所有键
                    val keys = tagsObj.keys()
                    while (keys.hasNext()) {
                        val english = keys.next()
                        val chinese = tagsObj.getString(english)
                        tags.add(PromptTag(english, chinese))
                    }

                    Log.d(TAG, "  分组 $j: $groupName, 标签数: ${tags.size}")
                    groups.add(PromptGroup(groupName, color, tags))
                }

                categories.add(PromptCategory(categoryName, groups))
            }

            val data = PromptData(categories)
            cachedData = data
            
            Log.d(TAG, "提示词数据加载完成！总分类: ${categories.size}")
            data
        } catch (e: Exception) {
            Log.e(TAG, "加载提示词数据失败", e)
            e.printStackTrace()
            // 返回空数据
            PromptData(emptyList())
        }
    }

    /**
     * 搜索提示词
     */
    fun searchTags(query: String, data: PromptData): List<Pair<PromptTag, String>> {
        if (query.isBlank()) return emptyList()

        val results = mutableListOf<Pair<PromptTag, String>>()
        val lowerQuery = query.lowercase()

        data.categories.forEach { category ->
            category.groups.forEach { group ->
                group.tags.forEach { tag ->
                    if (tag.chinese.contains(lowerQuery) || 
                        tag.english.lowercase().contains(lowerQuery)) {
                        results.add(tag to "${category.name} > ${group.name}")
                    }
                }
            }
        }

        Log.d(TAG, "搜索 '$query' 找到 ${results.size} 个结果")
        return results
    }
}
