import request from './request'

/** 用户模块 */
export const userApi = {
  login: (data) => request.post('/user/login', data),
  register: (data) => request.post('/user/register', data),
  list: () => request.get('/user/list')
}

/** 非遗项目模块 */
export const projectApi = {
  page: (params) => request.get('/project', { params }),
  detail: (id) => request.get(`/project/${id}`),
  save: (data) => request.post('/project', data),
  update: (id, data) => request.put(`/project/${id}`, data),
  remove: (id) => request.delete(`/project/${id}`),
  stat: () => request.get('/project/stat'),
  // 口语化模糊检索：返回 Top-K、匹配分、权重、命中标签与理由
  search: (data) => request.post('/project/search', data),
  // 仅查看意图归一化结果，便于前端展示"我理解成了什么"
  searchIntent: (data) => request.post('/project/search/intent', data)
}

/** 传承人模块 */
export const inheritorApi = {
  page: (params) => request.get('/inheritor', { params }),
  detail: (id) => request.get(`/inheritor/${id}`),
  // 该传承人关联的全部非遗项目
  projects: (id) => request.get(`/inheritor/${id}/projects`),
  save: (data) => request.post('/inheritor', data),
  update: (id, data) => request.put(`/inheritor/${id}`, data),
  remove: (id) => request.delete(`/inheritor/${id}`)
}

/** 民俗活动模块 */
export const activityApi = {
  page: (params) => request.get('/activity', { params }),
  detail: (id) => request.get(`/activity/${id}`),
  save: (data) => request.post('/activity', data),
  update: (id, data) => request.put(`/activity/${id}`, data),
  remove: (id) => request.delete(`/activity/${id}`)
}

/** 数据来源模块 */
export const sourceApi = {
  page: (params) => request.get('/source', { params }),
  detail: (id) => request.get(`/source/${id}`),
  save: (data) => request.post('/source', data),
  update: (id, data) => request.put(`/source/${id}`, data),
  remove: (id) => request.delete(`/source/${id}`)
}