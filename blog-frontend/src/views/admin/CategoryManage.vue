<template>
  <div class="taxonomy-page">
    <el-row :gutter="20">
      <el-col :span="12">
        <div class="card taxonomy-card">
          <div class="manage-head">
            <div>
              <p class="manage-kicker">CATEGORY CONTROL</p>
              <h3>分类管理</h3>
            </div>
          </div>
          <div class="toolbar-row">
            <el-input v-model="catName" placeholder="分类名称" />
            <el-button type="primary" @click="addCategory">添加</el-button>
          </div>
          <el-table :data="categories" stripe size="small">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="articleCount" label="文章数" width="90" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-popconfirm title="确定删除？" @confirm="delCategory(row.id)">
                  <template #reference><el-button type="danger" link size="small">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card taxonomy-card">
          <div class="manage-head">
            <div>
              <p class="manage-kicker">TAG CONTROL</p>
              <h3>标签管理</h3>
            </div>
          </div>
          <div class="toolbar-row">
            <el-input v-model="tagName" placeholder="标签名称" />
            <el-button type="primary" @click="addTag">添加</el-button>
          </div>
          <el-table :data="tags" stripe size="small">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="articleCount" label="文章数" width="90" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-popconfirm title="确定删除？" @confirm="delTag(row.id)">
                  <template #reference><el-button type="danger" link size="small">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCategories, getTags } from '@/api/common'
import { createCategory, deleteCategory, createTag, deleteTag } from '@/api/admin'
import { ElMessage } from 'element-plus'

const categories = ref([])
const tags = ref([])
const catName = ref('')
const tagName = ref('')

async function loadData() {
  const [c, t] = await Promise.all([getCategories(), getTags()])
  categories.value = c.data
  tags.value = t.data
}

async function addCategory() {
  if (!catName.value) return
  await createCategory({ name: catName.value })
  catName.value = ''
  ElMessage.success('添加成功')
  loadData()
}

async function delCategory(id) { await deleteCategory(id); ElMessage.success('已删除'); loadData() }

async function addTag() {
  if (!tagName.value) return
  await createTag({ name: tagName.value })
  tagName.value = ''
  ElMessage.success('添加成功')
  loadData()
}

async function delTag(id) { await deleteTag(id); ElMessage.success('已删除'); loadData() }

onMounted(loadData)
</script>

<style scoped lang="scss">
.taxonomy-card {
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

h3 {
  color: #143965;
}

.toolbar-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .toolbar-row {
    flex-direction: column;
  }
}
</style>
