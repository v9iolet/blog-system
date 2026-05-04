<template>
  <div class="manage-page card">
    <div class="manage-head">
      <div>
        <p class="manage-kicker">AUDIT STREAM</p>
        <h2>系统日志</h2>
      </div>
    </div>

    <el-table :data="logs" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="action" label="操作" width="180" />
      <el-table-column prop="detail" label="详情" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="createdAt" label="时间" width="170" />
    </el-table>
    <el-pagination background layout="prev, pager, next" :total="total" :page-size="10"
      v-model:current-page="page" @current-change="loadData" style="margin-top:20px;justify-content:center" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLogs } from '@/api/admin'

const logs = ref([])
const total = ref(0)
const page = ref(1)

async function loadData() {
  const res = await getLogs({ page: page.value, size: 10 })
  logs.value = res.data.records
  total.value = res.data.total
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
