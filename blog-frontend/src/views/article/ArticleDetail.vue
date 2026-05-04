<template>
  <div class="article-detail" v-if="article">
    <div class="card article-shell">
      <div class="article-head">
        <div class="headline-copy">
          <div class="headline-tags">
            <el-tag size="small">{{ article.categoryName }}</el-tag>
            <el-tag v-if="article.tags?.length" size="small" effect="plain">{{ article.tags.length }} 个标签</el-tag>
          </div>
          <h1>{{ article.title }}</h1>
          <div class="meta">
            <div class="author-chip" @click="$router.push(`/user/${article.authorId}`)">
              <el-avatar :size="36" :src="article.authorAvatar" />
              <span>{{ article.authorName }}</span>
            </div>
            <span>{{ formatDate(article.createdAt) }}</span>
            <span><el-icon><View /></el-icon> {{ article.views }}</span>
          </div>
        </div>
        <el-button type="primary" :icon="Star" @click="handleLike">点赞 ({{ article.likes }})</el-button>
      </div>

      <div class="tags" v-if="article.tags?.length">
        <el-tag v-for="tag in article.tags" :key="tag.id" size="small" effect="plain">{{ tag.name }}</el-tag>
      </div>

      <el-divider />
      <div class="article-content" v-html="renderedContent"></div>
    </div>

    <div class="card comment-section">
      <div class="comment-head">
        <h3 class="panel-title">评论区</h3>
        <span>{{ comments.length }} 条互动</span>
      </div>
      <div v-if="userStore.isLoggedIn" class="comment-form">
        <el-input v-model="commentText" type="textarea" :rows="4" placeholder="写下你的评论..." />
        <el-button type="primary" @click="submitComment" :disabled="!commentText.trim()">发表评论</el-button>
      </div>
      <el-alert v-else type="info" :closable="false">请先登录后再评论</el-alert>

      <div class="comment-list">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <el-avatar :size="40" :src="c.userAvatar" />
          <div class="comment-body">
            <div class="comment-header">
              <span class="name">{{ c.userName }}</span>
              <span class="time">{{ formatDate(c.createdAt) }}</span>
            </div>
            <p>{{ c.content }}</p>
            <div class="comment-actions">
              <el-button link size="small" @click="replyTo = c.id; replyText = ''">回复</el-button>
            </div>
            <div v-if="replyTo === c.id" class="reply-form">
              <el-input v-model="replyText" size="small" placeholder="回复..." />
              <el-button size="small" type="primary" @click="submitReply(c.id)">发送</el-button>
              <el-button size="small" @click="replyTo = null">取消</el-button>
            </div>
            <div v-if="c.children?.length" class="children">
              <div v-for="child in c.children" :key="child.id" class="comment-item child">
                <el-avatar :size="30" :src="child.userAvatar" />
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="name">{{ child.userName }}</span>
                    <span class="time">{{ formatDate(child.createdAt) }}</span>
                  </div>
                  <p>{{ child.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { View, Star } from '@element-plus/icons-vue'
import { getArticleDetail, likeArticle } from '@/api/article'
import { getComments, addComment } from '@/api/common'
import { useUserStore } from '@/store/modules/user'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import dayjs from 'dayjs'

const md = new MarkdownIt({
  highlight: (str, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang }).value}</code></pre>`
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  }
})

const route = useRoute()
const userStore = useUserStore()
const article = ref(null)
const comments = ref([])
const commentText = ref('')
const replyTo = ref(null)
const replyText = ref('')

const renderedContent = computed(() => article.value ? md.render(article.value.content) : '')
const formatDate = (d) => dayjs(d).format('YYYY-MM-DD HH:mm')

async function loadArticle() {
  const res = await getArticleDetail(route.params.id)
  article.value = res.data
}

async function loadComments() {
  const res = await getComments(route.params.id)
  comments.value = res.data
}

async function handleLike() {
  await likeArticle(article.value.id)
  article.value.likes++
}

async function submitComment() {
  await addComment({ articleId: article.value.id, content: commentText.value })
  commentText.value = ''
  loadComments()
}

async function submitReply(parentId) {
  await addComment({ articleId: article.value.id, content: replyText.value, parentId })
  replyTo.value = null
  replyText.value = ''
  loadComments()
}

onMounted(() => { loadArticle(); loadComments() })
</script>

<style scoped lang="scss">
.article-detail {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.article-shell,
.comment-section {
  padding: 28px 30px;
}

.article-head {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
}

.headline-tags,
.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.headline-copy h1 {
  margin: 16px 0 14px;
  font-size: 34px;
  line-height: 1.2;
  color: #123766;
}

.meta {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  color: var(--text-muted);
  font-size: 13px;
}

.author-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px 6px 6px;
  border-radius: 999px;
  background: rgba(43, 127, 255, 0.08);
  cursor: pointer;
  color: var(--text-primary);
}

.tags {
  margin-bottom: 6px;
}

.comment-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;

  span {
    color: var(--text-muted);
    font-size: 13px;
  }
}

.comment-form {
  margin-bottom: 22px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-end;

  .el-input {
    width: 100%;
  }
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid rgba(112, 172, 255, 0.16);
}

.comment-body {
  flex: 1;

  p {
    margin: 8px 0;
    color: var(--text-secondary);
  }
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.name {
  font-weight: 700;
  color: var(--text-primary);
}

.time {
  color: var(--text-muted);
  font-size: 12px;
}

.reply-form {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.children {
  margin-top: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(47, 128, 237, 0.05);
}

.child {
  border-bottom: none;
  padding: 8px 0;
}

@media (max-width: 768px) {
  .article-shell,
  .comment-section {
    padding: 22px 18px;
  }

  .article-head,
  .comment-head,
  .reply-form {
    flex-direction: column;
    align-items: flex-start;
  }

  .headline-copy h1 {
    font-size: 28px;
  }

  .reply-form {
    width: 100%;

    .el-input {
      width: 100%;
    }
  }
}
</style>
