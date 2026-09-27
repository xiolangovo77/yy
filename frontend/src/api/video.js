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

/** 按后端每页 20 条的限制完整加载，避免主视频或报告证据视频被截断。 */
export async function getAllChildVideos(childId, filters = {}) {
  const records = []
  for (let current = 1; ; current++) {
    const res = await getVideoList({ ...filters, childId, current, pageSize: 20, sortField: 'id', sortOrder: 'ascend' })
    const page = res.data?.records || []
    records.push(...page)
    if (!page.length || records.length >= Number(res.data?.total || 0)) return records
  }
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
