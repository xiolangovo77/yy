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

/** 转接儿童负责医生（管理员） */
export function transferChildDoctor(data) {
  return request.put('/child/transfer', data)
}

/**
 * 删除儿童（管理员）
 */
export function deleteChild(data) {
  return request.delete('/child', { data })
}

/** 加载全部选择项，遵守后端每页最多 20 条的限制。 */
export async function getAllChildOptions(filters = {}) {
  const records = []
  for (let current = 1; ; current++) {
    const res = await getChildList({ ...filters, current, pageSize: 20, sortField: 'id', sortOrder: 'ascend' })
    const page = res.data.records || []
    records.push(...page)
    if (!page.length || records.length >= Number(res.data.total || 0)) return records
  }
}
