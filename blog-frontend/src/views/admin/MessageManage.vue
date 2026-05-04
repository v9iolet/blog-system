<template>
  <div class="manage-page card">
    <div class="manage-head">
      <div>
        <p class="manage-kicker">MESSAGE CONTROL</p>
        <h2>留言管理</h2>
      </div>
    </div>

    <el-table :data="messages" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="content" label="留言内容" show-overflow-tooltip />
      <el-table-column prop="reply" label="回复" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">{{ row.status === 1 ? '已回复' : '待回复' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="170" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openReply(row)">回复</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination background layout="prev, pager, next" :total="total" :page-size="10"
      v-model:current-page="page" @current-change="loadData" style="margin-top:20px;justify-content:center" />

    <el-dialog v-model="dialogVisible" title="回复留言" width="500px">
      <p class="dialog-content">{{ currentMsg?.content }}</p>
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="输入回复内容..." />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReply">确认回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMessages, replyMessage } from '@/api/admin'
import { ElMessage } from 'element-plus'

const messages = ref([])
const total = ref(0)
const page = ref(1)
const dialogVisible = ref(false)
const currentMsg = ref(null)
const replyContent = ref('')

async function loadData() {
  const res = await getMessages({ page: page.value, size: 10 })
  messages.value = res.data.records
  total.value = res.data.total
}

function openReply(row) {
  currentMsg.value = row
  replyContent.value = row.reply || ''
  dialogVisible.value = true
}

async function handleReply() {
  if (!replyContent.value.trim()) { ElMessage.warning('回复内容不能为空'); return }
  await replyMessage(currentMsg.value.id, replyContent.value)
  ElMessage.success('回复成功')
  dialogVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.manage-page {
  padding: 24px 26px;
}

.manage-head {
  margin-bottom: 18px;
}

.manage-kicker {
  color: var(--brand-1);
  font-family: 'Orbitron', sans-serif;
  letter-spacing: 0.16em;
  font-size: 11px;
  margin-bottom: 10px;
}

h2 {
  color: #143965;
}

.dialog-content {
  margin-bottom: 12px;
  color: var(--text-secondary);
}
</style>
