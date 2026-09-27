<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">个人主页</h1>
    </div>

    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight: 600">个人信息</span>
          </template>
          <el-descriptions :column="1" border v-loading="loadingInfo">
            <el-descriptions-item label="姓名">{{ info.doctorName }}</el-descriptions-item>
            <el-descriptions-item label="账号">{{ info.account }}</el-descriptions-item>
            <el-descriptions-item label="职称">{{ info.title }}</el-descriptions-item>
            <el-descriptions-item label="医院">{{ info.hospital }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ info.phone }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="info.role == 1 ? 'danger' : 'primary'" size="small">
                {{ info.role == 1 ? '管理员' : '普通医生' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ info.createTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 编辑资料 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight: 600">编辑资料</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" size="default">
            <el-form-item label="姓名" prop="doctorName">
              <el-input v-model="form.doctorName" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="职称">
              <el-input v-model="form.title" placeholder="如：主任医师（选填）" />
            </el-form-item>
            <el-form-item label="医院" prop="hospital">
              <el-input v-model="form.hospital" placeholder="请输入医院" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号（选填）" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="form.password" type="password" placeholder="留空则不修改密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleUpdate">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDoctorById, updateProfile } from '@/api/doctor'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const formRef = ref(null)
const loadingInfo = ref(false)
const submitting = ref(false)

const info = reactive({
  doctorName: '', account: '', title: '', hospital: '', phone: '', role: 0, createTime: ''
})

const form = reactive({
  doctorName: '', title: '', hospital: '', phone: '', password: ''
})

const rules = {
  doctorName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 20, message: '姓名不能超过20位', trigger: 'blur' }
  ],
  // title 后端不校验，改为选填
  hospital: [
    { required: true, message: '请输入医院', trigger: 'blur' },
    { max: 64, message: '医院名称不能超过64位', trigger: 'blur' }
  ]
  // phone、password 后端均不校验，前端也不强制
}

async function fetchInfo() {
  loadingInfo.value = true
  try {
    const res = await getDoctorById(userStore.userInfo.id)
    const d = res.data
    info.doctorName = d.doctorName; info.account = d.account
    info.title = d.title; info.hospital = d.hospital
    info.phone = d.phone; info.role = d.role; info.createTime = d.createTime

    // 填充编辑表单
    form.doctorName = d.doctorName; form.title = d.title
    form.hospital = d.hospital; form.phone = d.phone
    form.password = ''
  } catch (e) {} finally { loadingInfo.value = false }
}

async function handleUpdate() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      doctorName: form.doctorName,
      title: form.title,
      hospital: form.hospital,
      phone: form.phone
    }
    if (form.password) {
      data.password = form.password
    }
    await updateProfile(data)
    ElMessage.success('资料修改成功')
    // 更新 store 中的用户名
    userStore.userInfo.doctorName = form.doctorName
    // 刷新信息
    fetchInfo()
  } catch (e) {} finally { submitting.value = false }
}

onMounted(() => fetchInfo())
</script>
