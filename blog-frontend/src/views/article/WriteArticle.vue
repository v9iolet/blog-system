<template>
  <div class="write-page">
    <div class="card editor-shell">
      <div class="editor-head">
        <div>
          <h2>{{ isEdit ? '编辑文章' : '写文章' }}</h2>
          <p>在统一的蓝白科技创作界面中整理标题、分类与正文。可使用 AI 辅助生成文章草稿，发布后的文章需经平台审核通过后才会展示在首页。</p>
        </div>
        <div class="editor-status">
          <span>{{ isEdit ? '编辑模式' : '创作模式' }}</span>
        </div>
      </div>

      <el-form :model="form" label-position="top" class="editor-form">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入文章标题" size="large" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width:100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width:100%">
                <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要（可选，也可发布后由审核生成）" />
        </el-form-item>
        <div class="ai-draft-bar">
          <el-button
            type="primary"
            :icon="MagicStick"
            @click="generatingDraft ? handleCancelDraft() : handleGenerateDraft()"
            :loading="generatingDraft && false"
            class="ai-button"
          >
            {{ generatingDraft ? '⏹ 停止生成' : '✨ AI 辅助生成草稿' }}
          </el-button>
          <span v-if="generatingDraft" class="ai-hint">AI 正在撰写中，内容将实时填充到下方编辑器...</span>
          <span v-else class="ai-hint">填写标题并选择分类/标签后，点击按钮让 AI 为你撰写文章草稿</span>
        </div>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="封面图URL（可选）" />
        </el-form-item>
        <el-form-item label="内容（Markdown）">
          <el-input v-model="form.content" type="textarea" :rows="20" placeholder="使用 Markdown 编写文章内容..." />
        </el-form-item>
        <div class="editor-actions">
          <el-button type="primary" @click="handleSubmit(1)" :loading="submitting">提交审核</el-button>
          <el-button @click="handleSubmit(0)" :loading="submitting">保存草稿</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { createArticle, updateArticle, getArticleDetail, getMyArticleDetail } from '@/api/article'
import { getCategories, getTags } from '@/api/common'
import { generateDraftStream } from '@/api/ai'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const categories = ref([])
const tags = ref([])
const submitting = ref(false)
const generatingDraft = ref(false)
let abortController = null
const form = ref({ title: '', content: '', summary: '', coverImage: '', categoryId: null, tagIds: [], status: 0, isTop: 0 })

async function loadData() {
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  tags.value = tagRes.data
  if (isEdit.value) {
    const res = await getMyArticleDetail(route.params.id)
    const a = res.data
    form.value = { id: a.id, title: a.title, content: a.content, summary: a.summary, coverImage: a.coverImage, categoryId: a.categoryId, tagIds: a.tags?.map(t => t.id) || [], status: a.status, isTop: a.isTop }
  }
}

/**
 * AI 辅助生成草稿
 * 通过 SSE 流式接收后端返回的 Markdown 内容，实时写入编辑器。
 */
async function handleGenerateDraft() {
  if (!form.value.title) {
    ElMessage.warning('请先填写文章标题')
    return
  }

  // 查找已选分类的名称
  const selectedCategory = categories.value.find(c => c.id === form.value.categoryId)
  const categoryName = selectedCategory ? selectedCategory.name : ''
  // 查找已选标签的名称列表
  const tagNames = form.value.tagIds
    .map(id => tags.value.find(t => t.id === id)?.name)
    .filter(Boolean)

  // 清空内容区域，准备接收流式数据
  form.value.content = ''
  generatingDraft.value = true
  abortController = new AbortController()

  try {
    await generateDraftStream(
      { title: form.value.title, categoryName, tagNames },
      (chunk) => {
        // 每收到一段文本，追加到编辑器内容末尾
        form.value.content += chunk
      },
      abortController.signal
    )
    // 生成完成后清理格式：去除超过2个连续空行
    form.value.content = form.value.content.replace(/\n{3,}/g, '\n\n').trim()
    ElMessage.success('AI 草稿生成完成！')
  } catch (error) {
    if (error.name === 'AbortError') {
      ElMessage.info('已停止生成')
    } else {
      ElMessage.error('生成草稿失败: ' + (error.message || '请稍后重试'))
    }
  } finally {
    generatingDraft.value = false
    abortController = null
  }
}

/**
 * 中止正在进行的 AI 生成
 */
function handleCancelDraft() {
  if (abortController) {
    abortController.abort()
  }
}

async function handleSubmit(status) {
  if (!form.value.title || !form.value.content) { ElMessage.warning('标题和内容不能为空'); return }
  submitting.value = true
  form.value.status = status
  try {
    if (isEdit.value) { await updateArticle(form.value) }
    else { const res = await createArticle(form.value); form.value.id = res.data }
    ElMessage.success(status === 1 ? '文章已提交审核' : '已保存草稿')
    if (isEdit.value) router.push(`/edit/${form.value.id}`)
    else router.push(status === 1 ? '/notifications' : `/edit/${form.value.id}`)
  } finally { submitting.value = false }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.write-page {
  max-width: 980px;
  margin: 0 auto;
}

.editor-shell {
  padding: 28px 30px;
}

.editor-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 24px;

  h2 {
    margin: 0 0 8px;
    font-size: 32px;
    color: #143965;
  }

  p:last-child {
    color: var(--text-secondary);
  }
}

.editor-status {
  padding: 14px 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255,255,255,0.94), rgba(240,247,255,0.82));
  border: 1px solid rgba(109, 176, 255, 0.22);
  color: var(--brand-deep);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.editor-actions {
  display: flex;
  gap: 12px;
  margin-top: 6px;
}

.ai-draft-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
  padding: 14px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.06), rgba(118, 75, 162, 0.06));
  border: 1px solid rgba(102, 126, 234, 0.15);

  .ai-button {
    flex-shrink: 0;
    height: 40px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .ai-hint {
    font-size: 13px;
    color: var(--text-secondary);
  }
}

@media (max-width: 768px) {
  .editor-shell {
    padding: 22px 18px;
  }

  .editor-head,
  .editor-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .editor-head h2 {
    font-size: 28px;
  }

  .ai-draft-bar {
    flex-direction: column;
    align-items: flex-start;

    .ai-button {
      width: 100%;
    }
  }
}
</style>
