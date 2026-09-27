<template>
  <div class="register-container">
    <div class="register-card">
      <h1 class="register-title">医生注册</h1>
      <p class="register-subtitle">创建您的医生账号</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="doctorName">
              <el-input v-model="form.doctorName" placeholder="请输入您的姓名" :prefix-icon="User" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称">
              <el-input v-model="form.title" placeholder="如：主任医师（选填）" :prefix-icon="Stamp" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="医院" prop="hospital">
          <el-input v-model="form.hospital" placeholder="请输入所在医院" :prefix-icon="OfficeBuilding" clearable />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号（选填）" :prefix-icon="Phone" clearable />
        </el-form-item>

        <el-form-item label="登录账号" prop="account">
          <el-input v-model="form.account" placeholder="请设置登录账号" :prefix-icon="Key" clearable />
        </el-form-item>

        <el-form-item label="登录密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请设置登录密码（至少6位）"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Stamp, OfficeBuilding, Phone, Key, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  doctorName: '',
  title: '',
  hospital: '',
  phone: '',
  account: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  doctorName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 20, message: '姓名不能超过20位', trigger: 'blur' }
  ],
  // title 后端不校验，改为选填
  hospital: [
    { required: true, message: '请输入医院', trigger: 'blur' },
    { max: 64, message: '医院名称不能超过64位', trigger: 'blur' }
  ],
  // phone 后端不校验，改为选填
  account: [
    { required: true, message: '请设置登录账号', trigger: 'blur' },
    { max: 50, message: '账号不能超过50位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置登录密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' },
    { max: 15, message: '密码不能超过15位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { doctorName, title, hospital, phone, account, password } = form
    await userStore.register({ doctorName, title, hospital, phone, account, password })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (err) {
    // 错误信息已在 request 拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.register-card {
  width: 520px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.register-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 14px;
  color: #909399;
  text-align: center;
  margin-bottom: 30px;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.register-footer a {
  color: #409EFF;
  text-decoration: none;
  margin-left: 4px;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
