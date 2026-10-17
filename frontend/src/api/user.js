import request from './request'

// 用户接口:与后端 mall-user 一一对应。

/** 注册。POST /api/v1/user/register,返回 userId */
export function register(data) {
  return request.post('/v1/user/register', data)
}

/** 登录。POST /api/v1/user/login,返回 { token, userId, nickname, avatar, level } */
export function login(data) {
  return request.post('/v1/user/login', data)
}
