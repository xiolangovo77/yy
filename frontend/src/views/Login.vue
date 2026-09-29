<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">儿童早期疾病筛查评估系统</h1>
      <p class="login-subtitle">医生登录</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="账号" prop="account">
          <el-input
            v-model="form.account"
            placeholder="请输入登录账号"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await userStore.login(form.account, form.password)

    // 普通医生登录后统一先进入项目介绍页
    const redirect = route.query.redirect
    if (Number(data.role) === 0) {
      router.push('/doctor/introduction')
      ElMessage.success('登录成功！')
    } else if (redirect) {
      router.push(redirect)
    } else if (Number(data.role) === 1) {
      router.push('/admin/dashboard')
      ElMessage.success('登录成功，欢迎管理员！')
    } else {
      router.push('/doctor/introduction')
      ElMessage.success('登录成功！')
    }
  } catch (err) {
    // 错误信息已在 request 拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 16px;
  color: #909399;
  text-align: center;
  margin-bottom: 30px;
}

.login-footer {
  text-align: center;
  font-size: 16px;
  color: #909399;
}

.login-footer a {
  color: #409EFF;
  text-decoration: none;
  margin-left: 4px;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>
