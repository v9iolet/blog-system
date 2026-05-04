<template>
  <div class="user-profile" v-if="user">
    <div class="card profile-header">
      <div class="avatar-section">
        <el-avatar :size="100" :src="user.avatar" class="profile-avatar" />
        <el-upload
          v-if="isCurrentUser"
          :show-file-list="false"
          :before-upload="handleAvatarUpload"
          accept="image/*"
          class="avatar-upload"
        >
          <el-button size="small" type="primary" :icon="Upload" :loading="uploading">
            {{ uploading ? '上传中...' : '更换头像' }}
          </el-button>
        </el-upload>
      </div>
      <div class="profile-info">
        <div class="profile-header-row">
          <h2>{{ user.nickname }}</h2>
          <el-button 
            v-if="isCurrentUser" 
            type="primary" 
            plain 
            size="small"
            @click="showEditDialog = true"
          >
            编辑资料
          </el-button>
        </div>
        <p class="username">@{{ user.username }}</p>
        <p class="bio">{{ user.bio || '这个人很懒，什么都没写~' }}</p>
        <div class="profile-meta">
          <span><el-icon><Calendar /></el-icon> 加入于 {{ formatDate(user.createdAt) }}</span>
          <span><el-icon><Document /></el-icon> {{ articles.length }} 篇文章</span>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h3>TA的文章</h3>
      </div>
      <div class="article-list">
        <div v-for="a in articles" :key="a.id" class="article-item" @click="$router.push(`/article/${a.id}`)">
          <div class="article-content">
            <h4 class="article-title">{{ a.title }}</h4>
            <p class="article-summary">{{ a.summary || '暂无摘要' }}</p>
            <div class="article-meta">
              <span class="article-date">{{ formatDate(a.createdAt) }}</span>
              <span class="article-views"><el-icon><View /></el-icon> {{ a.views || 0 }}</span>
            </div>
          </div>
        </div>
        <el-empty v-if="articles.length === 0" description="暂无文章" />
      </div>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑个人资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input 
            v-model="editForm.bio" 
            type="textarea" 
            :rows="4" 
            placeholder="介绍一下自己吧~"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateProfile" :loading="updating">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getUserById, updateProfile } from '@/api/user'
import { getArticles } from '@/api/article'
import { uploadImage } from '@/api/common'
import { useUserStore } from '@/store/modules/user'
import { Calendar, Document, View, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const userStore = useUserStore()
const user = ref(null)
const articles = ref([])
const uploading = ref(false)
const updating = ref(false)
const showEditDialog = ref(false)
const editForm = ref({
  nickname: '',
  email: '',
  bio: ''
})

const formatDate = (d) => dayjs(d).format('YYYY-MM-DD')

const isCurrentUser = computed(() => {
  return userStore.isLoggedIn && userStore.user?.id == route.params.id
})

async function loadUserData() {
  const res = await getUserById(route.params.id)
  user.value = res.data
  editForm.value = {
    nickname: user.value.nickname || '',
    email: user.value.email || '',
    bio: user.value.bio || ''
  }
}

async function loadArticles() {
  const artRes = await getArticles({ page: 1, size: 50 })
  articles.value = artRes.data.records.filter(a => a.authorId == route.params.id)
}

async function handleAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  uploading.value = true
  try {
    const res = await uploadImage(file)
    const avatarUrl = res.data
    
    await updateProfile({ avatar: avatarUrl })
    
    user.value.avatar = avatarUrl
    userStore.user.avatar = avatarUrl
    
    ElMessage.success('头像更新成功')
  } catch (error) {
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
  }
  
  return false
}

async function handleUpdateProfile() {
  if (!editForm.value.nickname) {
    ElMessage.warning('昵称不能为空')
    return
  }
  
  updating.value = true
  try {
    await updateProfile(editForm.value)
    
    user.value.nickname = editForm.value.nickname
    user.value.email = editForm.value.email
    user.value.bio = editForm.value.bio
    
    if (userStore.user) {
      userStore.user.nickname = editForm.value.nickname
      userStore.user.email = editForm.value.email
      userStore.user.bio = editForm.value.bio
    }
    
    ElMessage.success('资料更新成功')
    showEditDialog.value = false
  } catch (error) {
    ElMessage.error('资料更新失败')
  } finally {
    updating.value = false
  }
}

onMounted(async () => {
  await loadUserData()
  await loadArticles()
})
</script>

<style scoped lang="scss">
.user-profile {
  max-width: 900px;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.profile-header {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.profile-avatar {
  border: 3px solid #f0f0f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-upload {
  :deep(.el-upload) {
    width: 100%;
  }
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.profile-info h2 {
  margin: 0;
  font-size: 28px;
  color: #333;
}

.username {
  color: #999;
  font-size: 14px;
  margin: 4px 0 12px;
}

.bio {
  color: #666;
  line-height: 1.6;
  margin: 12px 0;
}

.profile-meta {
  display: flex;
  gap: 20px;
  margin-top: 16px;
  
  span {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #999;
    font-size: 14px;
    
    .el-icon {
      font-size: 16px;
    }
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  
  h3 {
    margin: 0;
    font-size: 20px;
    color: #333;
  }
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-item {
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
    transform: translateY(-2px);
    
    .article-title {
      color: #409eff;
    }
  }
}

.article-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.article-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  transition: color 0.3s ease;
}

.article-summary {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 4px;
}

.article-date,
.article-views {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 13px;
  
  .el-icon {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .profile-header-row {
    flex-direction: column;
  }
  
  .profile-meta {
    justify-content: center;
  }
  
  .article-title {
    font-size: 16px;
  }
}
</style>
