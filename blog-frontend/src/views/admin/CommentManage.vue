<template>
  <div class="manage-page card">
    <div class="manage-head">
      <div>
        <p class="manage-kicker">INTERACTION CONTROL</p>
        <h2>评论管理</h2>
      </div>
    </div>

    <el-table :data="comments" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="articleId" label="文章ID" width="90" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="createdAt" label="时间" width="170" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference><el-button type="danger" link size="small">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination background layout="prev, pager, next" :total="total" :page-size="10"
      v-model:current-page="page" @current-change="loadData" style="margin-top:20px;justify-content:center" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminComments, deleteAdminComment } from '@/api/admin'
import { ElMessage } from 'element-plus'

const comments = ref([])
const total = ref(0)
const page = ref(1)

async function loadData() {
  const res = await getAdminComments({ page: page.value, size: 10 })
  comments.value = res.data.records
  total.value = res.data.total
}

async function handleDelete(id) {
  await deleteAdminComment(id)
  ElMessage.success('已删除')
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
