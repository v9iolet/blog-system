import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { login as loginApi, getProfile } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const user = ref(null)
  const isLoggedIn = computed(() => !!token.value)

  function loadFromStorage() {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    if (savedToken) {
      token.value = savedToken
      if (savedUser) user.value = JSON.parse(savedUser)
    }
  }

  async function login(form) {
    const res = await loginApi(form)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('refreshToken', res.data.refreshToken)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res.data
  }

  async function fetchProfile() {
    const res = await getProfile()
    user.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  const isAdmin = () => user.value?.role === 1

  return { token, user, isLoggedIn, loadFromStorage, login, fetchProfile, logout, isAdmin }
})
