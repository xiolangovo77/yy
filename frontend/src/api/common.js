import request from './request'

/**
 * 上传MP4视频文件
 * @param {File} file - 视频文件
 * @returns {Promise} 返回上传后的文件URL
 */
export function uploadMP4(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/common/uploadMP4', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 600000 // 10分钟超时，视频文件较大
  })
}

/**
 * 上传MD报告文件
 * @param {File} file - MD文件
 * @returns {Promise} 返回上传后的文件URL
 */
export function uploadMD(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/common/uploadMD', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
