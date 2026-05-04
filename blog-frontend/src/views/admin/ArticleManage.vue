<template>
  <div class="manage-page card">
    <div class="manage-head">
      <div>
        <p class="manage-kicker">CONTENT CONTROL</p>
        <h2>文章管理</h2>
      </div>
      <div class="head-actions">
        <el-select v-model="filters.reviewStatus" placeholder="审核状态" clearable style="width: 180px" @change="loadData">
          <el-option label="待审核" :value="0" />
          <el-option label="审核通过" :value="1" />
          <el-option label="审核不通过" :value="2" />
        </el-select>
      </div>
    </div>

    <el-table :data="articles" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip min-width="220" />
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column prop="views" label="浏览" width="80" />
      <el-table-column label="发布状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="120">
        <template #default="{ row }">
          <el-tag :type="reviewTagType(row.reviewStatus)" size="small">{{ reviewStatusText(row.reviewStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewReason" label="驳回原因" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ row.reviewReason || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" type="primary" link size="small" @click="openReviewDialog(row)">审核</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference><el-button type="danger" link size="small">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="10"
      v-model:current-page="page"
      @current-change="loadData"
      style="margin-top:20px;justify-content:center"
    />

    <el-dialog v-model="dialogVisible" title="文章审核" width="520px" destroy-on-close>
      <div v-if="currentArticle" class="review-dialog">
        <div class="review-summary">
          <p><strong>文章标题：</strong>{{ currentArticle.title }}</p>
          <p><strong>作者：</strong>{{ currentArticle.authorName || '-' }}</p>
        </div>
        <el-form label-position="top">
          <el-form-item label="审核结果">
            <el-radio-group v-model="reviewForm.reviewStatus">
              <el-radio :label="1">通过</el-radio>
              <el-radio :label="2">不通过</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="不通过原因" v-if="reviewForm.reviewStatus === 2" required>
            <el-input
              v-model="reviewForm.reviewReason"
              type="textarea"
              :rows="4"
              maxlength="300"
              show-word-limit
              placeholder="请输入审核不通过原因"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitReview">确认审核</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminArticles, deleteAdminArticle, reviewAdminArticle } from '@/api/admin'
import { ElMessage } from 'element-plus'

const articles = ref([])
const total = ref(0)
const page = ref(1)
const dialogVisible = ref(false)
const submitting = ref(false)
const currentArticle = ref(null)
const filters = ref({ reviewStatus: undefined })
const reviewForm = ref({ reviewStatus: 1, reviewReason: '' })

function reviewStatusText(status) {
  if (status === 1) return '审核通过'
  if (status === 2) return '审核不通过'
  if (status === 0) return '待审核'
  return '未提交'
}

function reviewTagType(status) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  if (status === 0) return 'warning'
  return 'info'
}

async function loadData() {
  const params = { page: page.value, size: 10 }
  if (filters.value.reviewStatus !== undefined && filters.value.reviewStatus !== null && filters.value.reviewStatus !== '') {
    params.reviewStatus = filters.value.reviewStatus
  }
  const res = await getAdminArticles(params)
  articles.value = res.data.records
  total.value = res.data.total
}

function openReviewDialog(row) {
  currentArticle.value = row
  reviewForm.value = {
    reviewStatus: row.reviewStatus === 2 ? 2 : 1,
    reviewReason: row.reviewStatus === 2 ? (row.reviewReason || '') : ''
  }
  dialogVisible.value = true
}

async function submitReview() {
  if (!currentArticle.value) return
  if (reviewForm.value.reviewStatus === 2 && !reviewForm.value.reviewReason.trim()) {
    ElMessage.warning('请输入审核不通过原因')
    return
  }
  submitting.value = true
  try {
    await reviewAdminArticle(currentArticle.value.id, {
      reviewStatus: reviewForm.value.reviewStatus,
      reviewReason: reviewForm.value.reviewStatus === 2 ? reviewForm.value.reviewReason.trim() : ''
    })
    ElMessage.success('审核完成')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await deleteAdminArticle(id)
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.head-actions {
  display: flex;
  gap: 12px;
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

.review-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-summary {
  padding: 14px 16px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255,255,255,0.96), rgba(239,247,255,0.82));
  border: 1px solid rgba(98, 170, 255, 0.2);

  p {
    margin: 0;
    color: var(--text-secondary);
  }

  p + p {
    margin-top: 10px;
  }
}
</style>
