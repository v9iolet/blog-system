import { createApp, watch } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus, { ElNotification } from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { useUserStore } from './store/modules/user'
import './assets/styles/global.scss'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

const userStore = useUserStore(pinia)
userStore.loadFromStorage()

let notificationSocket = null

function closeNotificationSocket() {
  if (notificationSocket) {
    notificationSocket.close()
    notificationSocket = null
  }
}

function connectNotificationSocket() {
  if (!userStore.token || notificationSocket) return
  const token = typeof userStore.token === 'string' ? userStore.token : userStore.token?.value
  if (!token) return
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  notificationSocket = new WebSocket(`${protocol}://${window.location.host}/ws/notifications?token=${encodeURIComponent(token)}`)

  notificationSocket.onmessage = (event) => {
    try {
      const payload = JSON.parse(event.data)
      if (payload.type === 'article_review_notification') {
        ElNotification({
          title: payload.title || '新通知',
          message: payload.content || '你有一条新的审核通知',
          type: 'info',
          duration: 4500
        })
      }
    } catch (error) {
      console.error('notification parse error', error)
    }
  }

  notificationSocket.onclose = () => {
    notificationSocket = null
    if (userStore.token) {
      setTimeout(connectNotificationSocket, 1500)
    }
  }
}

watch(() => userStore.token, (token) => {
  if (token) {
    closeNotificationSocket()
    connectNotificationSocket()
  } else {
    closeNotificationSocket()
  }
}, { immediate: true })

app.mount('#app')
