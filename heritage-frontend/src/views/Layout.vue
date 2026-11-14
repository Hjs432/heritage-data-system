<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside class="aside">
      <div class="brand">
        <div class="brand-seal">遗</div>
        <div class="brand-text">
          <div class="brand-name">青藏高原非遗</div>
          <div class="brand-sub">HERITAGE ARCHIVE</div>
        </div>
      </div>

      <div class="nav-caption">档案管理</div>

      <el-menu :default-active="$route.path" router class="nav-menu">
        <el-menu-item index="/search">
          <el-icon><MagicStick /></el-icon>
          <span>智能检索</span>
          <span class="nav-badge">AI</span>
        </el-menu-item>
        <el-menu-item index="/project">
          <el-icon><Files /></el-icon>
          <span>非遗项目管理</span>
        </el-menu-item>
        <el-menu-item index="/inheritor">
          <el-icon><UserFilled /></el-icon>
          <span>传承人档案管理</span>
        </el-menu-item>
        <el-menu-item index="/activity">
          <el-icon><Flag /></el-icon>
          <span>民俗活动管理</span>
        </el-menu-item>
        <el-menu-item index="/source">
          <el-icon><Link /></el-icon>
          <span>资料来源溯源管理</span>
        </el-menu-item>
        <el-menu-item index="/vision">
          <el-icon><MapLocation /></el-icon>
          <span>资源视界</span>
          <span class="nav-badge is-map">MAP</span>
        </el-menu-item>
        <el-menu-item index="/statistic">
          <el-icon><PieChart /></el-icon>
          <span>数据统计分析</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-foot">
        <span class="foot-rule"></span>
        <p>唐卡 · 藏戏 · 锅庄</p>
        <p>格萨尔 · 酥油花</p>
      </div>
    </el-aside>

    <el-container class="shell">
      <el-header class="header">
        <div class="head-left">
          <span class="head-mark"></span>
          <div class="head-text">
            <div class="head-title">{{ $route.meta.title }}</div>
            <div class="head-desc">{{ $route.meta.desc }}</div>
          </div>
        </div>

        <div class="right">
          <div class="user">
            <div class="avatar">{{ avatarChar }}</div>
            <div class="user-meta">
              <span class="nickname">{{ nickname }}</span>
              <span class="role">{{ userStore.isAdmin ? '管理员 · 可编辑' : '普通用户 · 只读' }}</span>
            </div>
          </div>
          <span class="sep"></span>
          <el-button link class="logout-btn" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const nickname = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.userAccount || '未登录')
const avatarChar = computed(() => (nickname.value || '遗').slice(0, 1).toUpperCase())

/** 退出登录，清空 token */
async function handleLogout() {
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
  min-width: 1040px;
}

/* ============================ 侧边栏 ============================ */

.aside {
  width: 238px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: #cbbcA8;
  background-color: var(--lapis-deep);
  background-image:
    radial-gradient(circle at 12% -6%, rgba(217, 164, 65, 0.16), transparent 42%),
    radial-gradient(circle at 88% 108%, rgba(166, 55, 44, 0.14), transparent 46%),
    repeating-linear-gradient(0deg, rgba(255, 255, 255, 0.022) 0 1px, transparent 1px 24px);
  box-shadow: inset -1px 0 0 rgba(217, 164, 65, 0.22);
}

.brand {
  height: 76px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(217, 164, 65, 0.16);
}

.brand-seal {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  font-family: var(--font-display);
  font-size: 22px;
  color: #f0d08e;
  border: 1px solid rgba(217, 164, 65, 0.6);
  box-shadow: inset 0 0 0 3px rgba(217, 164, 65, 0.1);
  background: rgba(217, 164, 65, 0.06);
}

.brand-text {
  min-width: 0;
}

.brand-name {
  font-family: var(--font-display);
  font-size: 16px;
  letter-spacing: 0.12em;
  color: #f3e6cd;
  white-space: nowrap;
}

.brand-sub {
  margin-top: 4px;
  font-size: 9px;
  letter-spacing: 0.24em;
  color: rgba(217, 164, 65, 0.7);
  white-space: nowrap;
}

.nav-caption {
  flex-shrink: 0;
  padding: 20px 22px 8px;
  font-size: 10px;
  letter-spacing: 0.3em;
  color: rgba(203, 188, 168, 0.45);
}

.nav-menu {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 4px 12px 12px;
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #c9bba6;
  --el-menu-active-color: #f0d08e;
  --el-menu-hover-bg-color: rgba(217, 164, 65, 0.08);
}

.nav-menu :deep(.el-menu-item) {
  height: 46px;
  margin-bottom: 4px;
  padding-left: 14px !important;
  border-radius: 2px;
  font-size: 13.5px;
  letter-spacing: 0.08em;
  color: #c9bba6;
}

.nav-menu :deep(.el-menu-item .el-icon) {
  color: rgba(217, 164, 65, 0.72);
  font-size: 17px;
  margin-right: 10px;
}

.nav-menu :deep(.el-menu-item:hover) {
  color: #f3e6cd;
  background: rgba(217, 164, 65, 0.08);
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: #f5e2b6;
  background: linear-gradient(90deg, rgba(217, 164, 65, 0.18), rgba(217, 164, 65, 0.02));
  box-shadow: inset 0 0 0 1px rgba(217, 164, 65, 0.14);
}

.nav-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 11px;
  bottom: 11px;
  width: 3px;
  background: var(--butter);
}

.nav-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #f0d08e;
}

.nav-badge {
  margin-left: auto;
  padding: 1px 5px;
  font-size: 9px;
  letter-spacing: 0.12em;
  color: #f0d08e;
  border: 1px solid rgba(217, 164, 65, 0.45);
  border-radius: 1px;
}
.nav-badge.is-map {
  color: #a9c6b3;
  border-color: rgba(79, 122, 94, 0.6);
}

.aside-foot {
  flex-shrink: 0;
  padding: 16px 22px 20px;
  font-family: var(--font-display);
  color: rgba(203, 188, 168, 0.4);
}

.foot-rule {
  display: block;
  width: 28px;
  height: 1px;
  margin-bottom: 10px;
  background: rgba(217, 164, 65, 0.5);
}

.aside-foot p {
  margin: 0 0 4px;
  font-size: 11px;
  letter-spacing: 0.18em;
}

/* ============================ 顶栏 ============================ */

.shell {
  min-width: 0;
  background-color: var(--paper);
}

.header {
  height: 76px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 26px;
  background: rgba(251, 247, 238, 0.92);
  backdrop-filter: blur(6px);
  border-bottom: 1px solid var(--line);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.6);
}

.head-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.head-mark {
  width: 3px;
  height: 34px;
  flex-shrink: 0;
  background: linear-gradient(180deg, var(--tibet-red), var(--ochre));
  border-radius: 1px;
}

.head-title {
  font-family: var(--font-display);
  font-size: 21px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: var(--ink);
  line-height: 1.2;
}

.head-desc {
  margin-top: 4px;
  font-size: 11.5px;
  letter-spacing: 0.06em;
  color: var(--ink-4);
}

.right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  font-family: var(--font-display);
  font-size: 16px;
  color: var(--tibet-red);
  background: linear-gradient(160deg, #fdf8ec, #f1e5ce);
  border: 1px solid var(--line-strong);
  border-radius: 50%;
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.5);
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.nickname {
  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--ink-2);
}

.role {
  margin-top: 2px;
  font-size: 10.5px;
  letter-spacing: 0.08em;
  color: var(--ink-4);
}

.sep {
  width: 1px;
  height: 22px;
  background: var(--line);
}

.logout-btn {
  font-size: 12.5px;
  color: var(--ink-3);
}

.logout-btn:hover {
  color: var(--tibet-red);
}

/* ============================ 内容区 ============================ */

.main {
  padding: 22px 26px 34px;
  overflow-y: auto;
  background-color: var(--paper);
  background-image: radial-gradient(rgba(120, 90, 55, 0.055) 1px, transparent 1px);
  background-size: 18px 18px;
}

@media (max-width: 1200px) {
  .aside {
    width: 212px;
  }

  .main {
    padding: 18px;
  }

  .header {
    padding: 0 18px;
  }
}
</style>
