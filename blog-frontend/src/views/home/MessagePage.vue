<template>
  <div class="message-page">
    <section class="message-hero tech-panel">
      <div>
        <p class="message-kicker">OPEN COMMUNICATION PORTAL</p>
        <h1>留言互动中心</h1>
        <p>把建议、反馈与想法沉淀到一个更清晰、更有仪式感的交流界面里。</p>
      </div>
      <div class="message-badge-group">
        <div class="message-badge">BlueNova Guestbook</div>
        <div class="message-badge">Realtime Feedback</div>
      </div>
    </section>

    <div class="card message-card">
      <div class="panel-head">
        <h2 class="panel-title">留下你的足迹</h2>
        <span>CONNECT · SHARE · RESPOND</span>
      </div>
      <p class="desc">有什么想说的？欢迎留下你的意见、鼓励或建议。</p>
      <el-form :model="form" label-position="top" class="message-form">
        <div class="form-grid">
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" placeholder="你的昵称" />
          </el-form-item>
          <el-form-item label="邮箱（可选）">
            <el-input v-model="form.email" placeholder="你的邮箱" />
          </el-form-item>
        </div>
        <el-form-item label="留言内容">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="写下你想说的..." />
        </el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading" size="large">提交留言</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { leaveMessage } from '@/api/common'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const form = ref({ nickname: '', email: '', content: '' })

async function handleSubmit() {
  if (!form.value.nickname || !form.value.content) { ElMessage.warning('昵称和内容不能为空'); return }
  loading.value = true
  try {
    await leaveMessage(form.value)
    ElMessage.success('留言成功！')
    form.value = { nickname: '', email: '', content: '' }
  } catch (e) {} finally { loading.value = false }
}
</script>

<style scoped lang="scss">
.message-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.message-hero {
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

.message-kicker {
  color: var(--brand-1);
  font-family: 'Orbitron', sans-serif;
  letter-spacing: 0.18em;
  font-size: 11px;
}

.message-badge-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-badge {
  padding: 14px 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255,255,255,0.92), rgba(239,247,255,0.82));
  border: 1px solid rgba(107, 176, 255, 0.22);
  font-family: 'Orbitron', sans-serif;
  color: var(--brand-deep);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.message-card {
  padding: 26px 28px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 10px;

  span {
    color: var(--text-muted);
    font-size: 12px;
    letter-spacing: 0.16em;
  }
}

.desc {
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.message-form {
  position: relative;
  z-index: 1;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

@media (max-width: 768px) {
  .message-hero,
  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .message-hero {
    padding: 24px 18px;

    h1 {
      font-size: 28px;
    }
  }

  .message-card {
    padding: 22px 18px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
