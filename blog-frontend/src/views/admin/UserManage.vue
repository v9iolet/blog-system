<template>
  <div class="manage-page card">
    <div class="manage-head">
      <div>
        <p class="manage-kicker">ACCOUNT CONTROL</p>
        <h2>用户管理</h2>
      </div>
    </div>

    <el-table :data="users" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : ''" size="small">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
    </el-table>
    <el-pagination background layout="prev, pager, next" :total="total" :page-size="10"
      v-model:current-page="page" @current-change="loadData" style="margin-top:20px;justify-content:center" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsers, toggleUserStatus } from '@/api/admin'
import { ElMessage } from 'element-plus'

const users = ref([])
const total = ref(0)
const page = ref(1)

async function loadData() {
  const res = await getUsers({ page: page.value, size: 10 })
  users.value = res.data.records
  total.value = res.data.total
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await toggleUserStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
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
</style>
