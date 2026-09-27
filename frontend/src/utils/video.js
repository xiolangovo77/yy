export const DEFAULT_VIDEO_NAME = 'main.mp4'

export function isMainVideoName(name) {
  return String(name || '').trim().toLowerCase().startsWith('main')
}
