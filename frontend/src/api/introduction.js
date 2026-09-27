import request from './request'

export function getProjectIntroduction() {
  return request.get('/project-introduction')
}

export function saveProjectIntroduction(data) {
  return request.put('/project-introduction', data)
}
