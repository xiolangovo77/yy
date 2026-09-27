import request from './request'

/**
 * 分页获取儿童列表
 */
export function getChildList(data) {
  return request.post('/child/list', data)
}

/**
 * 获取儿童详情
 */
export function getChildById(id) {
  return request.get(`/child/${id}`)
}

/**
 * 添加儿童（管理员）
 */
export function addChild(data) {
  return request.post('/child', data)
}

/**
 * 修改儿童信息（管理员）
 */
export function updateChild(data) {
  return request.put('/child', data)
}

/**
 * 删除儿童（管理员）
 */
export function deleteChild(data) {
  return request.delete('/child', { data })
}
