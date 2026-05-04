import request from '@/utils/request'

export const generateAISummary = (data) => request.post('/ai/summary', data)
export const checkAIStatus = () => request.get('/ai/status')
