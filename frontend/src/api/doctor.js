import request from './request'

/**
 * 医生登录
 */
export function loginApi(data) {
  return request.post('/doctor/login', data)
}

/**
 * 医生注册
 */
export function registerApi(data) {
  return request.post('/doctor/register', data)
}

/**
 * 医生退出
 */
export function logoutApi() {
  return request.post('/doctor/logout')
}

/**
 * 获取医生详情
 */
export function getDoctorById(id) {
  return request.get(`/doctor/${id}`)
}

/**
 * 分页获取医生列表
 */
export function getDoctorList(data) {
  return request.post('/doctor/list', data)
}

/**
 * 添加医生（管理员）
 */
export function addDoctor(data) {
  return request.post('/doctor', data)
}

/**
 * 修改医生信息（管理员）
 */
export function updateDoctor(data) {
  return request.put('/doctor', data)
}

/**
 * 修改医生状态（管理员）
 */
export function updateDoctorStatus(data) {
  return request.post('/doctor/status', data)
}

/**
 * 删除医生（管理员）
 */
export function deleteDoctor(data) {
  return request.delete('/doctor', { data })
}

/**
 * 修改个人资料（当前登录医生）
 */
export function updateProfile(data) {
  return request.put('/doctor/profile', data)
}
