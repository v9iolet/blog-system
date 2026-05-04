<template>
  <div class="notification-page">
    <section class="notification-hero tech-panel">
      <div>
        <p class="notification-kicker">REVIEW FEEDBACK STREAM</p>
        <h1>审核通知中心</h1>
        <p>集中查看文章审核结果、反馈原因与最新状态，帮助创作者快速完成下一步调整。</p>
      </div>
      <div class="notification-badges">
        <div class="notification-badge">Station Inbox</div>
        <div class="notification-badge">Review Loop</div>
      </div>
    </section>

    <div class="card notice-card">
      <div class="panel-head">
        <h2 class="panel-title">最新通知</h2>
        <span>UNREAD · {{ unreadCount }}</span>
      </div>

      <el-empty v-if="!loading && notifications.length === 0" description="暂无审核通知" />

      <div v-else class="notice-list">
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notice-item"
          :class="{ unread: item.status === 0 }"
        >
          <div class="notice-top">
            <div>
              <h3>{{ item.title }}</h3>
              <p>{{ formatDate(item.createdAt) }}</p>
            </div>
            <el-tag v-if="item.status === 0" type="danger" effect="dark" round>未读</el-tag>
            <el-tag v-else type="info" round>已读</el-tag>
          </div>
          <p class="notice-content">{{ item.content }}</p>
          <div class="notice-actions">
            <el-button v-if="item.articleId" type="primary" link @click="goArticle(item.articleId)">查看文章</el-button>
            <el-button v-if="item.status === 0" link @click="handleRead(item.id)">标记已读</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getArticleNotifications, markArticleNotificationRead } from '@/api/article'

const router = useRouter()
const loading = ref(false)
const notifications = ref([])

const unreadCount = computed(() => notifications.value.filter(item => item.status === 0).length)
const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

async function loadNotifications() {
  loading.value = true
  try {
    const res = await getArticleNotifications()
    notifications.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleRead(id) {
  await markArticleNotificationRead(id)
  const target = notifications.value.find(item => item.id === id)
  if (target) target.status = 1
  ElMessage.success('已标记为已读')
}

function goArticle(articleId) {
  router.push(`/article/${articleId}`)
}

onMounted(loadNotifications)
</script>

<style scoped lang="scss">
.notification-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.notification-hero {
  padding: 30px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;

  h1 {
    margin: 12px 0 10px;
    font-size: 34px;
    color: #133766;
  }

  p:last-child {
    color: var(--text-secondary);
  }
}

.notification-kicker {
  color: var(--brand-1);
  font-family: 'Orbitron', sans-serif;
  letter-spacing: 0.18em;
  font-size: 11px;
}

.notification-badges {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-badge {
  padding: 14px 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255,255,255,0.92), rgba(239,247,255,0.82));
  border: 1px solid rgba(107, 176, 255, 0.22);
  font-family: 'Orbitron', sans-serif;
  color: var(--brand-deep);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.notice-card {
  padding: 26px 28px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;

  span {
    color: var(--text-muted);
    font-size: 12px;
    letter-spacing: 0.16em;
  }
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  padding: 20px 22px;
  border-radius: 22px;
  border: 1px solid rgba(100, 170, 255, 0.18);
  background: linear-gradient(180deg, rgba(255,255,255,0.94), rgba(239,247,255,0.82));
  box-shadow: 0 16px 40px rgba(34, 82, 149, 0.08);
}

.notice-item.unread {
  border-color: rgba(66, 138, 255, 0.32);
  box-shadow: 0 22px 50px rgba(34, 82, 149, 0.14);
}

.notice-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;

  h3 {
    margin: 0;
    color: #12386a;
    font-size: 20px;
  }

  p {
    margin: 8px 0 0;
    color: var(--text-muted);
    font-size: 13px;
  }
}

.notice-content {
  margin: 14px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.notice-actions {
  margin-top: 16px;
  display: flex;
  gap: 14px;
}

@media (max-width: 768px) {
  .notification-hero,
  .panel-head,
  .notice-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .notification-hero {
    padding: 24px 18px;

    h1 {
      font-size: 28px;
    }
  }

  .notice-card {
    padding: 22px 18px;
  }
}
</style>
