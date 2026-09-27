import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo, clearAuth } from '@/utils/auth'
import { loginApi, registerApi, logoutApi } from '@/api/doctor'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(getToken())
  const userInfo = ref(getUserInfo() || {})
  const role = computed(() => userInfo.value.role)
  const isAdmin = computed(() => userInfo.value.role === 1)
  const isDoctor = computed(() => userInfo.value.role === 0)
  const isLoggedIn = computed(() => !!token.value)

  // 登录
  async function login(account, password) {
    const res = await loginApi({ account, password })
    const data = res.data
    token.value = data.token
    // 后端 Jackson 将 Long 序列化为 String，需要转 Number
    const roleNum = Number(data.role)
    userInfo.value = {
      id: data.id,
      doctorName: data.doctorName,
      account: data.account,
      role: roleNum
    }
    setToken(data.token)
    setUserInfo(userInfo.value)
    return { ...data, role: roleNum }
  }

  // 注册
  async function register(form) {
    const res = await registerApi(form)
    return res.data
  }

  // 退出登录
  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // 即使接口失败也清除本地状态
    }
    token.value = ''
    userInfo.value = {}
    clearAuth()
  }

  return {
    token,
    userInfo,
    role,
    isAdmin,
    isDoctor,
    isLoggedIn,
    login,
    register,
    logout
  }
})
