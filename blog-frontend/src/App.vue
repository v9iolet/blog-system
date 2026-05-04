<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from '@/store/modules/user'
import { onMounted } from 'vue'

const userStore = useUserStore()
onMounted(() => {
  userStore.loadFromStorage()
})
</script>
