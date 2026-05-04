<template>
  <div class="admin-layout">
    <el-container class="admin-shell">
      <el-aside width="260px" class="admin-aside">
        <div class="admin-logo">
          <span class="logo-badge">IT</span>
          <div>
            <strong>IT博客平台</strong>
            <span>管理后台</span>
          </div>
        </div>

        <div class="aside-section">系统模块</div>

        <el-menu :default-active="activeMenu" router class="admin-menu">
          <el-menu-item index="/admin">
            <el-icon><DataAnalysis /></el-icon><span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/articles">
            <el-icon><Document /></el-icon><span>文章管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon><span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/comments">
            <el-icon><ChatDotRound /></el-icon><span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Menu /></el-icon><span>分类标签</span>
          </el-menu-item>
          <el-menu-item index="/admin/messages">
            <el-icon><Promotion /></el-icon><span>留言管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Tickets /></el-icon><span>系统日志</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="admin-header">
          <div>
            <h2>后台管理系统</h2>
          </div>
          <div class="header-right">
            <div class="admin-user-chip">
              <span class="online-dot"></span>
              <el-avatar :size="36" :src="userStore.user?.avatar || '/default-avatar.png'" class="admin-avatar" />
              <div>
                <strong>{{ userStore.user?.nickname }}</strong>
                <small>管理员</small>
              </div>
            </div>
            <el-button plain @click="$router.push('/')">返回前台</el-button>
            <el-button type="primary" @click="handleLogout">退出</el-button>
          </div>
        </el-header>
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at left top, rgba(77, 196, 255, 0.14), transparent 24%),
    linear-gradient(180deg, #eff6ff, #edf3fb);
}

.admin-shell {
  min-height: 100vh;
}

.admin-aside {
  position: relative;
  min-height: 100vh;
  padding: 18px 16px;
  background: linear-gradient(180deg, #102845, #14345c 52%, #122a46 100%);
  border-right: 1px solid rgba(126, 189, 255, 0.1);
  box-shadow: 14px 0 42px rgba(7, 23, 43, 0.18);
}

.admin-aside::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(rgba(133, 201, 255, 0.09) 1px, transparent 1px),
    linear-gradient(90deg, rgba(133, 201, 255, 0.09) 1px, transparent 1px);
  background-size: 36px 36px;
  opacity: 0.35;
  pointer-events: none;
}

.admin-logo {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 8px 22px;
  margin-bottom: 14px;

  strong {
    display: block;
    color: #f4fbff;
    font-family: 'Orbitron', sans-serif;
    letter-spacing: 0.06em;
    font-size: 17px;
  }

  span {
    color: rgba(197, 223, 255, 0.72);
    font-size: 11px;
    letter-spacing: 0.16em;
    text-transform: uppercase;
  }
}

.logo-badge {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2b7fff, #59d1ff);
  color: #fff !important;
  box-shadow: 0 12px 26px rgba(66, 152, 255, 0.35);
}

.aside-section {
  position: relative;
  z-index: 1;
  padding: 0 10px 12px;
  color: rgba(184, 216, 255, 0.58);
  font-size: 11px;
  letter-spacing: 0.22em;
}

.admin-menu {
  position: relative;
  z-index: 1;
  border-right: none;
  background: transparent;

  :deep(.el-menu-item) {
    height: 50px;
    margin-bottom: 10px;
    border-radius: 14px;
    color: #c4dcfb;
    background: transparent;
    transition: all 0.22s ease;
  }

  :deep(.el-menu-item:hover) {
    background: rgba(255, 255, 255, 0.08);
    color: #fff;
  }

  :deep(.el-menu-item.is-active) {
    color: #fff;
    background: linear-gradient(135deg, rgba(43, 127, 255, 0.34), rgba(77, 196, 255, 0.22));
    box-shadow: inset 0 0 0 1px rgba(131, 198, 255, 0.16), 0 12px 24px rgba(16, 58, 114, 0.24);
  }

  :deep(.el-menu-item .el-icon) {
    font-size: 18px;
    margin-right: 12px;
  }
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 92px;
  padding: 18px 28px;
  background: rgba(248, 252, 255, 0.68);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(120, 179, 255, 0.18);
}

.admin-header h2 {
  font-size: 28px;
  color: #12396d;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.admin-user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 18px;
  background: rgba(255,255,255,0.82);
  border: 1px solid rgba(109, 176, 255, 0.18);

  .admin-avatar {
    border: 2px solid rgba(109, 176, 255, 0.3);
  }

  strong {
    display: block;
    font-size: 14px;
    color: var(--text-primary);
  }

  small {
    color: var(--text-muted);
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }
}

.online-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(180deg, #31ecbe, #15af7c);
  box-shadow: 0 0 0 4px rgba(49, 236, 190, 0.14);
}

.admin-main {
  padding: 26px 28px 30px;
}

@media (max-width: 992px) {
  .admin-header {
    padding: 18px;
    align-items: flex-start;
    flex-direction: column;
  }

  .header-right {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .admin-main {
    padding: 18px 14px 22px;
  }

  .admin-aside {
    width: 84px !important;
    padding-inline: 10px;
  }

  .admin-logo div,
  .aside-section,
  .admin-menu :deep(.el-menu-item span) {
    display: none;
  }

  .admin-menu :deep(.el-menu-item) {
    justify-content: center;
    padding: 0 !important;
  }

  .admin-menu :deep(.el-menu-item .el-icon) {
    margin-right: 0;
  }
}
</style>
