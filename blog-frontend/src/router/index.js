import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/home/HomePage.vue') },
      { path: 'article/:id', name: 'ArticleDetail', component: () => import('@/views/article/ArticleDetail.vue') },
      { path: 'category/:id', name: 'CategoryArticles', component: () => import('@/views/home/HomePage.vue') },
      { path: 'tag/:id', name: 'TagArticles', component: () => import('@/views/home/HomePage.vue') },
      { path: 'search', name: 'Search', component: () => import('@/views/home/HomePage.vue') },
      { path: 'message', name: 'Message', component: () => import('@/views/home/MessagePage.vue') },
      { path: 'notifications', name: 'Notifications', component: () => import('@/views/home/NotificationPage.vue') },
      { path: 'user/:id', name: 'UserProfile', component: () => import('@/views/user/UserProfile.vue') },
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/user/LoginPage.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/user/RegisterPage.vue') },
  {
    path: '/write',
    name: 'WriteArticle',
    component: () => import('@/views/article/WriteArticle.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/edit/:id',
    name: 'EditArticle',
    component: () => import('@/views/article/WriteArticle.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/admin/DashboardPage.vue') },
      { path: 'articles', name: 'AdminArticles', component: () => import('@/views/admin/ArticleManage.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'comments', name: 'AdminComments', component: () => import('@/views/admin/CommentManage.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('@/views/admin/CategoryManage.vue') },
      { path: 'messages', name: 'AdminMessages', component: () => import('@/views/admin/MessageManage.vue') },
      { path: 'logs', name: 'AdminLogs', component: () => import('@/views/admin/LogManage.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && (!user || user.role !== 1)) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
