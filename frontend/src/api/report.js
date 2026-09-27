import request from './request'

/**
 * 获取报告详情
 */
export function getReportById(id) {
  return request.get(`/report/${id}`)
}

/**
 * 分页获取报告列表
 */
export function getReportList(data) {
  return request.post('/report/list', data)
}

/**
 * 添加报告（管理员）
 */
export function addReport(data) {
  return request.post('/report', data)
}

/**
 * 修改报告（管理员）
 */
export function updateReport(data) {
  return request.put('/report', data)
}

/**
 * 删除报告（管理员）
 */
export function deleteReport(data) {
  return request.delete('/report', { data })
}
