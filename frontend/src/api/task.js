import request from './request'

/**
 * 发布评估任务（管理员）
 */
export function publishTask(data) {
  return request.post('/task/publish', data)
}

/**
 * 获取任务详情
 */
export function getTaskById(id) {
  return request.get(`/task/${id}`)
}

/**
 * 分页获取任务列表
 */
export function getTaskList(data) {
  return request.post('/task/list', data)
}

/**
 * 获取一阶段表单
 */
export function getForm1ByTaskId(taskId) {
  return request.get(`/task/${taskId}/form1`)
}

/**
 * 获取二阶段表单
 */
export function getForm2ByTaskId(taskId) {
  return request.get(`/task/${taskId}/form2`)
}

/**
 * 开始任务（医生认领）
 */
export function startTask(id) {
  return request.put(`/task/${id}/start`)
}

/**
 * 提交一阶段表单
 */
export function submitForm1(data) {
  return request.post('/task/sub_form1', data)
}

/**
 * 提交二阶段表单
 */
export function submitForm2(data) {
  return request.post('/task/sub_form2', data)
}

export function getTaskFeedback(taskId) {
  return request.get(`/task/${taskId}/feedback`)
}

export function submitTaskFeedback(data) {
  return request.post('/task/feedback', data)
}

/**
 * 删除任务（管理员）
 */
export function deleteTask(data) {
  return request.delete('/task', { data })
}
