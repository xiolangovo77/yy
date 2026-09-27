const TOKEN_KEY = 'token'
const USER_KEY = 'user_info'

/**
 * 获取 token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

/**
 * 设置 token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除 token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  try {
    const info = localStorage.getItem(USER_KEY)
    return info ? JSON.parse(info) : null
  } catch {
    return null
  }
}

/**
 * 设置用户信息
 */
export function setUserInfo(info) {
  localStorage.setItem(USER_KEY, JSON.stringify(info))
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  localStorage.removeItem(USER_KEY)
}

/**
 * 清除所有登录状态
 */
export function clearAuth() {
  removeToken()
  removeUserInfo()
}

/**
 * 判断是否已登录
 */
export function isLoggedIn() {
  return !!getToken()
}
