<template>
  <div class="auth-page">
    <div class="auth-backdrop"></div>
    <div class="auth-card card">
      <div class="auth-head">
        <p class="auth-kicker">CREATE ACCOUNT</p>
        <h2>注册新账号</h2>
        <p>快速加入蓝白科技内容社区，开启阅读、创作与互动体验。</p>
      </div>

      <el-form :model="form" @submit.prevent="handleRegister" class="auth-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名（3-20位）" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.email" placeholder="邮箱" :prefix-icon="Message" size="large" />
        </el-form-item>
        <el-form-item>
          <div class="code-input-wrapper">
            <el-input v-model="form.code" placeholder="邮箱验证码" size="large" maxlength="6" />
            <el-button 
              type="primary" 
              :disabled="countdown > 0 || !form.email" 
              @click="handleSendCode"
              :loading="sendingCode"
              size="large"
            >
              {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码（6位以上）" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.nickname" placeholder="昵称（可选）" size="large" />
        </el-form-item>
        <el-button type="primary" @click="handleRegister" :loading="loading" size="large" style="width:100%">注册</el-button>
      </el-form>

      <div class="auth-footer">
        <span>已有账号？</span>
        <router-link to="/login">去登录</router-link>
        <span style="margin: 0 8px;">|</span>
        <a href="javascript:;" @click="showResetDialog = true">忘记密码</a>
      </div>
    </div>

    <!-- 找回密码对话框 -->
    <el-dialog v-model="showResetDialog" title="找回密码" width="450px" :close-on-click-modal="false">
      <el-form :model="resetForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="resetForm.email" placeholder="请输入注册邮箱" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-input-wrapper">
            <el-input v-model="resetForm.code" placeholder="邮箱验证码" maxlength="6" />
            <el-button 
              type="primary" 
              :disabled="resetCountdown > 0 || !resetForm.email" 
              @click="handleSendResetCode"
              :loading="sendingResetCode"
            >
              {{ resetCountdown > 0 ? `${resetCountdown}秒` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码（6位以上）" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResetDialog = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword" :loading="resetting">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { register, sendVerificationCode, resetPassword } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const form = ref({ username: '', email: '', password: '', nickname: '', code: '' })

// 找回密码相关
const showResetDialog = ref(false)
const resetting = ref(false)
const sendingResetCode = ref(false)
const resetCountdown = ref(0)
const resetForm = ref({ email: '', code: '', newPassword: '' })

// 发送注册验证码
async function handleSendCode() {
  if (!form.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  // 简单的邮箱格式验证
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailReg.test(form.value.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }
  
  sendingCode.value = true
  try {
    await sendVerificationCode(form.value.email, 'register')
    ElMessage.success('验证码已发送，请查收邮件')
    startCountdown()
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    sendingCode.value = false
  }
}

// 发送找回密码验证码
async function handleSendResetCode() {
  if (!resetForm.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailReg.test(resetForm.value.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }
  
  sendingResetCode.value = true
  try {
    await sendVerificationCode(resetForm.value.email, 'reset')
    ElMessage.success('验证码已发送，请查收邮件')
    startResetCountdown()
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    sendingResetCode.value = false
  }
}

// 注册倒计时
function startCountdown() {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 找回密码倒计时
function startResetCountdown() {
  resetCountdown.value = 60
  const timer = setInterval(() => {
    resetCountdown.value--
    if (resetCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 注册
async function handleRegister() {
  if (!form.value.username || !form.value.email || !form.value.password || !form.value.code) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (form.value.username.length < 3 || form.value.username.length > 20) {
    ElMessage.warning('用户名长度应为3-20位')
    return
  }
  
  if (form.value.password.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  
  if (form.value.code.length !== 6) {
    ElMessage.warning('请输入6位验证码')
    return
  }
  
  loading.value = true
  try {
    await register(form.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

// 重置密码
async function handleResetPassword() {
  if (!resetForm.value.email || !resetForm.value.code || !resetForm.value.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (resetForm.value.newPassword.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  
  if (resetForm.value.code.length !== 6) {
    ElMessage.warning('请输入6位验证码')
    return
  }
  
  resetting.value = true
  try {
    await resetPassword(resetForm.value)
    ElMessage.success('密码重置成功，请使用新密码登录')
    showResetDialog.value = false
    resetForm.value = { email: '', code: '', newPassword: '' }
    router.push('/login')
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    resetting.value = false
  }
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
    radial-gradient(circle at 14% 24%, rgba(73, 189, 255, 0.28), transparent 18%),
    radial-gradient(circle at 84% 24%, rgba(43, 127, 255, 0.24), transparent 18%),
    linear-gradient(180deg, rgba(249,252,255,0.9), rgba(237,244,252,0.76));
}

.auth-card {
  position: relative;
  z-index: 1;
  width: min(460px, 100%);
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

.code-input-wrapper {
  display: flex;
  gap: 10px;
  width: 100%;

  .el-input {
    flex: 1;
  }

  .el-button {
    white-space: nowrap;
    min-width: 110px;
  }
}

.auth-footer {
  margin-top: 18px;
  text-align: center;
  color: var(--text-muted);

  a {
    margin-left: 6px;
    font-weight: 700;
    color: var(--brand-1);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

:deep(.el-dialog) {
  .code-input-wrapper {
    display: flex;
    gap: 10px;

    .el-input {
      flex: 1;
    }

    .el-button {
      white-space: nowrap;
      min-width: 100px;
    }
  }
}
</style>
