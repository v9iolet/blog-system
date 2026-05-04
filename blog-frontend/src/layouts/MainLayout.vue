<template>
  <div class="main-layout page-shell">
    <header class="page-header main-header">
      <div class="container header-inner">
        <router-link to="/" class="brand-mark">
          <span class="brand-badge">01</span>
          <div class="brand-copy">
            <strong>IT博客平台</strong>
          </div>
        </router-link>

        <!-- 搜索栏 -->
        <div class="header-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索文章..."
            :prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
            class="search-input"
          />
        </div>

        <nav class="nav-links">
          <router-link to="/">首页</router-link>
          <router-link to="/message">留言板</router-link>
          <template v-if="userStore.isLoggedIn">
            <router-link to="/write">创作台</router-link>
            <el-dropdown trigger="click" popper-class="tech-dropdown-menu" @visible-change="handleNoticeVisible">
              <span class="notice-menu">
                <el-badge :value="unreadCount" :hidden="!unreadCount" class="notice-badge">
                  <span class="notice-trigger">
                    <el-icon><Bell /></el-icon>
                    <span>通知</span>
                  </span>
                </el-badge>
              </span>
              <template #dropdown>
                <div class="notice-dropdown-shell">
                  <div class="notice-dropdown-head">
                    <strong>审核通知</strong>
                    <span>{{ unreadCount }} 条未读</span>
                  </div>
                  <div v-if="noticeLoading" class="notice-dropdown-empty">加载中...</div>
                  <div v-else-if="!notifications.length" class="notice-dropdown-empty">暂无通知</div>
                  <div v-else class="notice-dropdown-list">
                    <div v-for="item in notifications.slice(0, 6)" :key="item.id" class="notice-dropdown-item" @click="openNotification(item)">
                      <div class="notice-dropdown-title-row">
                        <strong>{{ item.title }}</strong>
                        <el-tag v-if="item.status === 0" type="danger" size="small" round>未读</el-tag>
                      </div>
                      <p>{{ item.content }}</p>
                    </div>
                  </div>
                  <div class="notice-dropdown-footer">
                    <el-button type="primary" link @click="router.push('/notifications')">查看全部通知</el-button>
                  </div>
                </div>
              </template>
            </el-dropdown>
            <el-dropdown trigger="click" popper-class="tech-dropdown-menu">
              <span class="user-menu">
                <span class="status-dot"></span>
                <el-avatar :size="34" :src="userStore.user?.avatar || '/default-avatar.png'" />
                <span class="user-copy">
                  <strong>{{ userStore.user?.nickname }}</strong>
                  <small>{{ userStore.isAdmin() ? '管理员' : '用户中心' }}</small>
                </span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push(`/user/${userStore.user?.id}`)">个人主页</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin()" @click="$router.push('/admin')">后台管理</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
            <router-link to="/register" class="nav-cta">注册</router-link>
          </template>
        </nav>
      </div>
    </header>


    <main class="main-content container">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="container footer-inner">
        <p>Powered by Vue 3 + Spring Boot</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ArrowDown, Bell, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { useRouter } from 'vue-router'
import { getArticleNotifications, markArticleNotificationRead } from '@/api/article'

const userStore = useUserStore()
const router = useRouter()
const notifications = ref([])
const noticeLoading = ref(false)
const searchKeyword = ref('')

const unreadCount = computed(() => notifications.value.filter(item => item.status === 0).length)

function handleLogout() {
  userStore.logout()
  notifications.value = []
  router.push('/')
}

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/', query: { keyword: searchKeyword.value.trim() } })
  }
}

async function loadNotifications() {
  if (!userStore.isLoggedIn) return
  noticeLoading.value = true
  try {
    const res = await getArticleNotifications()
    notifications.value = res.data || []
  } finally {
    noticeLoading.value = false
  }
}

async function handleNoticeVisible(visible) {
  if (visible) {
    await loadNotifications()
  }
}

async function openNotification(item) {
  if (item.status === 0) {
    await markArticleNotificationRead(item.id)
    item.status = 1
  }
  router.push('/notifications')
}

watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    loadNotifications()
  } else {
    notifications.value = []
  }
}, { immediate: true })

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadNotifications()
  }
})
</script>

<style scoped lang="scss">
.main-header {
  .header-inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 76px;
    gap: 24px;
  }
}

.brand-mark {
  display: flex;
  align-items: center;
  gap: 14px;
  color: var(--text-primary);
  flex-shrink: 0;
}

.brand-badge {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--brand-1), var(--brand-2));
  color: #fff;
  font-family: 'Orbitron', sans-serif;
  font-size: 14px;
  box-shadow: 0 14px 32px rgba(43, 127, 255, 0.32);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  line-height: 1.15;

  strong {
    font-family: 'Orbitron', sans-serif;
    font-size: 18px;
    letter-spacing: 0.05em;
  }
}

.header-search {
  flex: 1;
  max-width: 400px;
  
  .search-input {
    :deep(.el-input__wrapper) {
      border-radius: 999px;
      background: rgba(255, 255, 255, 0.85);
      border: 1px solid rgba(95, 166, 255, 0.2);
      box-shadow: 0 8px 20px rgba(43, 127, 255, 0.08);
      transition: all 0.3s ease;
      
      &:hover {
        border-color: rgba(95, 166, 255, 0.4);
        box-shadow: 0 10px 24px rgba(43, 127, 255, 0.12);
      }
      
      &.is-focus {
        border-color: var(--brand-1);
        box-shadow: 0 10px 28px rgba(43, 127, 255, 0.18);
      }
    }
    
    :deep(.el-input__inner) {
      font-size: 14px;
    }
  }
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  flex-shrink: 0;

  a {
    min-height: 42px;
    padding: 0 16px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    color: var(--text-secondary);
    font-size: 14px;
    border: 1px solid transparent;
    transition: all 0.25s ease;

    &:hover,
    &.router-link-active {
      color: var(--brand-deep);
      background: rgba(255, 255, 255, 0.75);
      border-color: rgba(95, 166, 255, 0.2);
      box-shadow: 0 10px 24px rgba(43, 127, 255, 0.08);
    }
  }
}

.nav-cta {
  background: linear-gradient(135deg, rgba(43, 127, 255, 0.12), rgba(77, 196, 255, 0.2));
  border-color: rgba(95, 166, 255, 0.26) !important;
}

.notice-menu {
  display: inline-flex;
  align-items: center;
}

.notice-badge :deep(.el-badge__content) {
  box-shadow: none;
}

.notice-trigger,
.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 7px 10px 7px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(104, 170, 255, 0.2);
  cursor: pointer;
  color: var(--text-primary);
  box-shadow: 0 12px 28px rgba(40, 91, 171, 0.08);
}

.notice-trigger {
  padding-right: 14px;

  span {
    font-size: 14px;
    font-weight: 600;
  }
}

.status-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: linear-gradient(180deg, #3cf6b8, #16b378);
  box-shadow: 0 0 0 4px rgba(60, 246, 184, 0.12);
}

.user-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.1;

  strong {
    font-size: 14px;
    font-weight: 700;
  }

  small {
    color: var(--text-muted);
    font-size: 11px;
    letter-spacing: 0.08em;
    text-transform: uppercase;
  }
}

.footer-inner {
  padding-bottom: 8px;
}

:deep(.tech-dropdown-menu) {
  border-radius: 18px;
  padding: 8px;
  border: 1px solid rgba(102, 171, 255, 0.2);
  box-shadow: 0 18px 50px rgba(34, 83, 155, 0.18);
}

.notice-dropdown-shell {
  width: 360px;
  padding: 8px;
}

.notice-dropdown-head,
.notice-dropdown-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notice-dropdown-head {
  padding: 10px 10px 14px;

  strong {
    color: #12386a;
  }

  span {
    color: var(--text-muted);
    font-size: 12px;
  }
}

.notice-dropdown-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 360px;
  overflow: auto;
}

.notice-dropdown-item {
  padding: 14px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255,255,255,0.94), rgba(239,247,255,0.82));
  border: 1px solid rgba(97, 170, 255, 0.14);
  cursor: pointer;

  p {
    margin: 8px 0 0;
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.6;
  }
}

.notice-dropdown-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;

  strong {
    color: #143965;
    font-size: 14px;
  }
}

.notice-dropdown-empty {
  padding: 28px 16px;
  color: var(--text-muted);
  text-align: center;
}

.notice-dropdown-footer {
  padding: 12px 8px 4px;
}

@media (max-width: 992px) {
  .main-header .header-inner {
    min-height: auto;
    padding: 14px 0;
    flex-wrap: wrap;
  }

  .header-search {
    order: 3;
    width: 100%;
    max-width: 100%;
    margin-top: 10px;
  }

  .nav-links {
    justify-content: flex-end;
  }
}

@media (max-width: 768px) {
  .nav-links {
    gap: 10px;

    a {
      min-height: 38px;
      padding: 0 12px;
      font-size: 13px;
    }
  }

  .brand-copy strong {
    font-size: 16px;
  }

  .user-copy {
    display: none;
  }

  .notice-dropdown-shell {
    width: min(320px, 80vw);
  }
}
</style>
