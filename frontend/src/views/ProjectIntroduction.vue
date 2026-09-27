<template>
  <div class="page-container introduction">
    <div class="page-header"><h1 class="page-title">项目介绍与评估说明</h1></div>
    <el-card shadow="never" v-loading="loading">
      <el-alert v-if="loadFailed" title="项目介绍加载失败，请重试" type="error" :closable="false">
        <el-button size="small" @click="loadIntroduction">重新加载</el-button>
      </el-alert>
      <IntroductionContent v-else-if="introduction" :title="introduction.title" :content="introduction.content" />
      <el-empty v-else-if="!loading" description="项目介绍尚未发布" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getProjectIntroduction } from '@/api/introduction'
import IntroductionContent from '@/components/IntroductionContent.vue'

const loading = ref(false)
const loadFailed = ref(false)
const introduction = ref(null)

async function loadIntroduction() {
  if (loading.value) return
  loading.value = true
  loadFailed.value = false
  try {
    const res = await getProjectIntroduction()
    introduction.value = res.data
  } catch (e) {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadIntroduction)
</script>

<style scoped>
.introduction { max-width: 1080px; margin: 0 auto; }
</style>
