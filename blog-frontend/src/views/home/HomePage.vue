<template>
  <div class="home-page">
    <div class="home-main">
      <div class="article-list">
        <section class="hero-banner tech-panel">
          <div class="hero-copy">
            <h1>洞察、创作与交流汇聚于同一片蓝白信息场</h1>
            <p class="hero-subtitle">以科技感大气界面承载优质内容，让搜索、阅读、分类与热点追踪都更清晰、更高效。</p>
            <div class="hero-actions">
              <el-input
                v-model="keyword"
                placeholder="搜索文章、标签或主题..."
                clearable
                @keyup.enter="loadArticles"
                :prefix-icon="Search"
                size="large"
              />
              <el-button type="primary" size="large" @click="loadArticles">开始检索</el-button>
            </div>
            <div class="hero-stats">
              <div class="stat-box">
                <span>文章数</span>
                <strong>{{ total }}</strong>
              </div>
              <div class="stat-box">
                <span>分类数</span>
                <strong>{{ categories.length }}</strong>
              </div>
              <div class="stat-box">
                <span>标签数</span>
                <strong>{{ tags.length }}</strong>
              </div>
            </div>
          </div>
        </section>

        <div class="section-head">
          <div>
            <h2 class="section-title">内容流</h2>
          </div>
          <div class="section-count">共 {{ total }} 篇内容</div>
        </div>

        <div
          v-for="article in articles"
          :key="article.id"
          class="card article-card"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div class="article-card-body">
            <div class="cover-wrap">
              <img v-if="article.coverImage" :src="article.coverImage" class="cover" alt="" />
              <div v-else class="cover cover-fallback">
                <span>博客</span>
                <strong>文章</strong>
              </div>
            </div>
            <div class="info">
              <div class="title-row">
                <el-tag v-if="article.isTop" type="danger" size="small">置顶</el-tag>
                <el-tag size="small" effect="plain">{{ article.categoryName || '未分类' }}</el-tag>
              </div>
              <h2 class="title">{{ article.title }}</h2>
              <p class="summary">{{ article.summary }}</p>
              <div class="meta">
                <span>{{ article.authorName }}</span>
                <span>{{ formatDate(article.createdAt) }}</span>
                <span><el-icon><View /></el-icon> {{ article.views }}</span>
                <span><el-icon><ChatDotRound /></el-icon> {{ article.likes }}</span>
              </div>
              <div class="tags" v-if="article.tags?.length">
                <el-tag
                  v-for="tag in article.tags"
                  :key="tag.id"
                  size="small"
                  effect="plain"
                  @click.stop="$router.push(`/tag/${tag.id}`)"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && articles.length === 0" description="暂无文章" class="card empty-wrap" />

        <div class="pagination">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            v-model:current-page="currentPage"
            @current-change="loadArticles"
          />
        </div>
      </div>

      <aside class="sidebar hide-mobile">
        <div class="card side-panel">
          <div class="sidebar-title-row">
            <h3 class="panel-title">分类导航</h3>
            <span>{{ categories.length }}</span>
          </div>
          <div class="category-list">
            <div v-for="cat in categories" :key="cat.id" class="category-item" @click="$router.push(`/category/${cat.id}`)">
              <span>{{ cat.name }}</span>
              <strong>{{ cat.articleCount }}</strong>
            </div>
          </div>
        </div>

        <div class="card side-panel">
          <div class="sidebar-title-row">
            <h3 class="panel-title">标签矩阵</h3>
            <span>{{ tags.length }}</span>
          </div>
          <div class="tag-cloud">
            <el-tag
              v-for="tag in tags"
              :key="tag.id"
              class="tag-item"
              effect="plain"
              @click="$router.push(`/tag/${tag.id}`)"
            >
              {{ tag.name }} · {{ tag.articleCount }}
            </el-tag>
          </div>
        </div>

        <div class="card side-panel" v-if="hotArticles.length">
          <div class="sidebar-title-row">
            <h3 class="panel-title">热度排行</h3>
            <span>TOP {{ hotArticles.length }}</span>
          </div>
          <div v-for="(a, i) in hotArticles" :key="a.id" class="hot-item" @click="$router.push(`/article/${a.id}`)">
            <span class="rank" :class="{ top: i < 3 }">{{ i + 1 }}</span>
            <span class="hot-title">{{ a.title }}</span>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search, View, ChatDotRound } from '@element-plus/icons-vue'
import { getArticles } from '@/api/article'
import { getCategories, getTags } from '@/api/common'
import dayjs from 'dayjs'

const route = useRoute()
const articles = ref([])
const hotArticles = ref([])
const categories = ref([])
const tags = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)

const formatDate = (d) => dayjs(d).format('YYYY-MM-DD')

async function loadArticles() {
  loading.value = true
  const params = { page: currentPage.value, size: pageSize }
  if (route.params.id && route.name === 'CategoryArticles') params.categoryId = route.params.id
  if (route.params.id && route.name === 'TagArticles') params.tagId = route.params.id
  if (keyword.value || route.query.q) params.keyword = keyword.value || route.query.q
  const res = await getArticles(params)
  articles.value = res.data.records
  total.value = res.data.total
  loading.value = false
}

async function loadSidebar() {
  const [catRes, tagRes, hotRes] = await Promise.all([
    getCategories(), getTags(),
    getArticles({ page: 1, size: 5 })
  ])
  categories.value = catRes.data
  tags.value = tagRes.data
  hotArticles.value = hotRes.data.records
}

watch(() => route.fullPath, () => { currentPage.value = 1; loadArticles() })
onMounted(() => { loadArticles(); loadSidebar() })
</script>

<style scoped lang="scss">
.home-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-banner {
  padding: 34px 32px;
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) 360px;
  gap: 24px;
}

.hero-copy h1 {
  margin: 0 0 14px;
  font-size: 38px;
  line-height: 1.16;
  color: #12386a;
}

.hero-subtitle {
  max-width: 760px;
  color: var(--text-secondary);
}

.hero-actions {
  margin-top: 24px;
  display: flex;
  gap: 14px;
}

.hero-stats {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.stat-box {
  padding: 18px 20px;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(255,255,255,0.92), rgba(240,247,255,0.88));
  border: 1px solid rgba(110, 178, 255, 0.2);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.8);

  span {
    display: block;
    color: var(--text-muted);
    font-size: 11px;
    text-transform: uppercase;
    letter-spacing: 0.18em;
    margin-bottom: 8px;
  }

  strong {
    font-size: 34px;
    font-family: 'Orbitron', sans-serif;
    color: var(--brand-deep);
  }
}

.home-main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
}

.section-head,
.sidebar-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-head {
  margin-bottom: 16px;
}

.section-count,
.sidebar-title-row span {
  color: var(--text-muted);
  font-size: 13px;
}

.article-card {
  margin-bottom: 18px;
  cursor: pointer;
  transition: transform 0.26s ease, box-shadow 0.26s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 28px 80px rgba(33, 78, 156, 0.2);
  }
}

.article-card-body {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 18px;
  padding: 22px;
}

.cover-wrap {
  width: 228px;
  flex-shrink: 0;
}

.cover {
  width: 100%;
  height: 152px;
  object-fit: cover;
  border-radius: 18px;
  box-shadow: 0 18px 36px rgba(20, 67, 138, 0.16);
}

.cover-fallback {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 18px;
  background:
    radial-gradient(circle at top right, rgba(122, 218, 255, 0.7), transparent 34%),
    linear-gradient(135deg, #0f56d0, #49c1ff);
  color: #fff;

  span {
    font-size: 12px;
    letter-spacing: 0.24em;
    opacity: 0.84;
  }

  strong {
    font-family: 'Orbitron', sans-serif;
    font-size: 24px;
    margin-top: 8px;
  }
}

.info {
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.title {
  font-size: 24px;
  line-height: 1.3;
  margin-bottom: 10px;
  color: #143965;
}

.summary {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  display: flex;
  gap: 16px;
  color: var(--text-muted);
  font-size: 13px;
  align-items: center;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;

  .el-tag {
    cursor: pointer;
  }
}

.side-panel {
  padding: 22px;
  margin-bottom: 18px;
}

.category-list,
.tag-cloud {
  margin-top: 18px;
}

.category-item,
.hot-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.22s ease;

  &:hover {
    background: rgba(56, 141, 255, 0.08);
    transform: translateX(4px);
  }
}

.category-item {
  color: var(--text-secondary);

  strong {
    color: var(--brand-deep);
    font-family: 'Orbitron', sans-serif;
    font-size: 13px;
  }
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;

  .tag-item {
    cursor: pointer;
  }
}

.hot-item {
  margin-top: 8px;
  justify-content: flex-start;
}

.rank {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  background: rgba(71, 135, 230, 0.12);
  color: var(--brand-deep);
  text-align: center;
  font-size: 12px;
  line-height: 28px;
  font-family: 'Orbitron', sans-serif;
  flex-shrink: 0;

  &.top {
    background: linear-gradient(135deg, var(--brand-1), var(--brand-2));
    color: #fff;
  }
}

.hot-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-wrap {
  padding: 30px;
}

.pagination {
  text-align: center;
  margin-top: 22px;
}

@media (max-width: 1100px) {
  .hero-banner,
  .home-main {
    grid-template-columns: 1fr;
  }

  .hero-stats {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-banner {
    padding: 24px 18px;
  }

  .hero-copy h1 {
    font-size: 28px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }

  .article-card-body {
    flex-direction: column;
    padding: 18px;
  }

  .cover-wrap {
    width: 100%;
  }

  .cover {
    height: 180px;
  }

  .title {
    font-size: 20px;
  }
}
</style>
