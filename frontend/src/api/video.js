import request from './request'

/**
 * 获取视频详情
 */
export function getVideoById(id) {
  return request.get(`/video/${id}`)
}

/**
 * 分页获取视频列表
 */
export function getVideoList(data) {
  return request.post('/video/list', data)
}

/**
 * 添加视频资源（管理员）
 */
export function addVideo(data) {
  return request.post('/video', data)
}

/**
 * 修改视频资源（管理员）
 */
export function updateVideo(data) {
  return request.put('/video', data)
}

/**
 * 删除视频资源（管理员）
 */
export function deleteVideo(data) {
  return request.delete('/video', { data })
}
