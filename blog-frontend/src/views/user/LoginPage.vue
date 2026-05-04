<template>
  <div class="auth-page">
    <div class="auth-backdrop"></div>
    <div class="auth-card card">
      <div class="auth-head">
        <p class="auth-kicker">ACCOUNT ACCESS</p>
        <h2>登录系统</h2>
        <p>进入蓝白科技风内容平台，切换用户端或管理员端工作流。</p>
      </div>

      <el-form :model="form" @submit.prevent="handleLogin" class="auth-form">
        <el-form-item>
          <el-select v-model="form.role" placeholder="请选择登录角色" size="large" style="width: 100%">
            <el-option label="普通用户" :value="0" />
            <el-option label="管理员" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-button type="primary" @click="handleLogin" :loading="loading" size="large" style="width:100%">登录</el-button>
      </el-form>

      <div class="auth-footer">
        <span>还没有账号？</span>
        <router-link to="/register">去注册</router-link>
        <span style="margin: 0 8px;">|</span>
        <router-link to="/register">忘记密码</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const form = ref({ role: 0, username: '', password: '' })

async function handleLogin() {
  if (form.value.role === null || form.value.role === undefined || !form.value.username || !form.value.password) {
    ElMessage.warning('请填写完整')
    return
  }
  loading.value = true
  try {
    const result = await userStore.login(form.value)
    ElMessage.success('登录成功')
    if (result.user?.role === 1) {
      router.push(route.query.redirect || '/admin')
    } else {
      router.push(route.query.redirect || '/')
    }
  } catch (e) {} finally { loading.value = false }
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 24px;
}

.auth-backdrop {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 20%, rgba(75, 171, 255, 0.26), transparent 18%),
    radial-gradient(circle at 80% 30%, rgba(77, 196, 255, 0.3), transparent 20%),
    linear-gradient(180deg, rgba(249,252,255,0.88), rgba(238,245,255,0.72));
}

.auth-card {
  position: relative;
  z-index: 1;
  width: min(440px, 100%);
  padding: 34px 32px;
}

.auth-head {
  margin-bottom: 26px;

  h2 {
    font-size: 32px;
    margin: 10px 0 8px;
    color: #133766;
  }

  p:last-child {
    color: var(--text-secondary);
  }
}

.auth-kicker {
  color: var(--brand-1);
  font-family: 'Orbitron', sans-serif;
  letter-spacing: 0.18em;
  font-size: 11px;
}

.auth-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

.auth-footer {
  margin-top: 18px;
  text-align: center;
  color: var(--text-muted);

  a {
    margin-left: 6px;
    font-weight: 700;
  }
}
</style>
