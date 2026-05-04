import request from '@/utils/request'

export const getArticles = (params) => request.get('/articles', { params })
export const getArticleDetail = (id) => request.get(`/articles/${id}`)
export const getMyArticleDetail = (id) => request.get(`/articles/mine/${id}`)
export const getArticleNotifications = () => request.get('/articles/notifications')
export const markArticleNotificationRead = (messageId) => request.put(`/articles/notifications/${messageId}/read`)
export const createArticle = (data) => request.post('/articles', data)
export const updateArticle = (data) => request.put('/articles', data)
export const deleteArticle = (id) => request.delete(`/articles/${id}`)
export const likeArticle = (id) => request.post(`/articles/${id}/like`)
