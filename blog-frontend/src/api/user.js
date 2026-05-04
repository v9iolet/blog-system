import request from '@/utils/request'

export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const sendVerificationCode = (email, purpose) => request.post('/auth/send-code', null, { params: { email, purpose } })
export const resetPassword = (data) => request.post('/auth/reset-password', data)
export const getProfile = () => request.get('/user/profile')
export const updateProfile = (data) => request.put('/user/profile', data)
export const changePassword = (data) => request.put('/user/password', data)
export const getUserById = (id) => request.get(`/user/${id}`)
