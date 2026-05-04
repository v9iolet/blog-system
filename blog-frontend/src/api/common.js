import request from '@/utils/request'

export const getCategories = () => request.get('/categories')
export const getTags = () => request.get('/tags')
export const getComments = (articleId) => request.get(`/comments/article/${articleId}`)
export const addComment = (data) => request.post('/comments', data)
export const deleteComment = (id) => request.delete(`/comments/${id}`)
export const leaveMessage = (data) => request.post('/messages', data)
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
