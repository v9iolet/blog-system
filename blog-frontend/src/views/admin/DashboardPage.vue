<template>
  <div class="dashboard-page">
    <el-row :gutter="20" class="dashboard-grid">
      <el-col :span="6" v-for="item in statCards" :key="item.label">
        <div class="stat-card card">
          <div class="stat-icon" :style="{ background: item.color }">
            <el-icon :size="28"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Document, User, ChatDotRound, View } from '@element-plus/icons-vue'
import { getDashboard } from '@/api/admin'

const stats = ref({})

const statCards = computed(() => [
  { label: '文章总数', value: stats.value.totalArticles || 0, icon: Document, color: 'linear-gradient(135deg, #2b7fff, #57ceff)' },
  { label: '用户总数', value: stats.value.totalUsers || 0, icon: User, color: 'linear-gradient(135deg, #00a9ff, #6fe3ff)' },
  { label: '评论总数', value: stats.value.totalComments || 0, icon: ChatDotRound, color: 'linear-gradient(135deg, #1c8fff, #4fd7ff)' },
  { label: '访问总量', value: stats.value.totalVisits || 0, icon: View, color: 'linear-gradient(135deg, #256dff, #72c6ff)' },
])

onMounted(async () => {
  const res = await getDashboard()
  stats.value = res.data
})
</script>

<style scoped lang="scss">
.dashboard-page {
  display: flex;
  flex-direction: column;
}

.dashboard-grid {
  margin-top: 0;
}

.stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 148px;
}

.stat-icon {
  width: 62px;
  height: 62px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 18px 36px rgba(43, 127, 255, 0.22);
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #12396d;
  font-family: 'Orbitron', sans-serif;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 14px;
  margin-top: 6px;
}

@media (max-width: 992px) {
  :deep(.el-col) {
    margin-bottom: 16px;
  }
}
</style>
