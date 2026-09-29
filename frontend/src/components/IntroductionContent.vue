<template>
  <article class="introduction-content">
    <h2>{{ title }}</h2>
    <div class="introduction-body">{{ content }}</div>
  </article>
</template>

<script setup>
defineProps({ title: { type: String, default: '' }, content: { type: String, default: '' } })
</script>

<style scoped>
.introduction-content { overflow-wrap: anywhere; }
h2 { margin: 0 0 24px; font-size: 24px; color: #303133; }
.introduction-body { white-space: pre-wrap; font-size: 17px; line-height: 1.9; color: #606266; }
</style>
