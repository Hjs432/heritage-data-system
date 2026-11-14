import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { userApi } from '../api'

/**
 * 用户状态：token + 用户信息 + 角色，持久化到 localStorage
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  /** 是否管理员：控制写操作按钮可见性（后端仍会二次校验） */
  const isAdmin = computed(() => userInfo.value?.userRole === 'admin')

  /** 登录 */
  async function login(form) {
    const res = await userApi.login(form)
    const data = res.data
    token.value = data.token
    userInfo.value = {
      userAccount: data.userAccount,
      nickname: data.nickname,
      userRole: data.userRole
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return data
  }

  /** 退出登录 */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isAdmin, login, logout }
})