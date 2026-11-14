<template>
  <div class="login-page" :class="{ 'is-opening': introOpen }">
    <div class="stage">
      <!-- 品牌区 -->
      <section class="brand-side">
      <div class="frame"></div>
      <span class="watermark">遗</span>

      <div class="brand-top">
        <div class="brand-seal">遗</div>
        <span class="brand-top-name">青藏高原非物质文化遗产</span>
      </div>

      <div class="brand-main">
        <div class="brand-eyebrow">TIBETAN PLATEAU · HERITAGE ARCHIVE</div>
        <h1>雪域文脉<br />数字档案</h1>
        <span class="rule"></span>
        <p class="brand-desc">
          系统整理青藏高原非物质文化遗产项目、传承人与民俗活动，
          让每一项技艺都有据可循。
        </p>
        <div class="keywords">
          <span>唐卡</span><i>·</i><span>藏戏</span><i>·</i><span>锅庄</span><i>·</i><span>格萨尔</span><i>·</i><span>酥油花</span>
        </div>
      </div>

      <div class="brand-foot">
        <span class="vertical-label">资源数据管理系统</span>
        <span class="brand-en">THANGKA / FOLK ARTS / ARCHIVE</span>
      </div>
    </section>

    <!-- 表单区 -->
    <section class="form-side">
      <div class="form-box">
        <div class="form-head">
          <div class="eyebrow">档案管理系统</div>
          <h2>欢迎回来</h2>
          <p>请使用账号登录，继续整理雪域文脉</p>
        </div>

        <el-tabs v-model="activeTab" class="login-tabs">
          <!-- 登录 -->
          <el-tab-pane label="登录" name="login">
            <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0">
              <el-form-item prop="userAccount">
                <el-input v-model="loginForm.userAccount" placeholder="请输入账号" size="large" :prefix-icon="User" />
              </el-form-item>
              <el-form-item prop="userPwd">
                <el-input v-model="loginForm.userPwd" type="password" placeholder="请输入密码" size="large"
                          :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
              </el-form-item>
              <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="handleLogin">
                登 录
              </el-button>
            </el-form>
          </el-tab-pane>

          <!-- 注册 -->
          <el-tab-pane label="注册" name="register">
            <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="0">
              <el-form-item prop="userAccount">
                <el-input v-model="registerForm.userAccount" placeholder="请输入账号" size="large" :prefix-icon="User" />
              </el-form-item>
              <el-form-item prop="nickname">
                <el-input v-model="registerForm.nickname" placeholder="请输入昵称（可选）" size="large" />
              </el-form-item>
              <el-form-item prop="userPwd">
                <el-input v-model="registerForm.userPwd" type="password" placeholder="请输入密码" size="large" show-password />
              </el-form-item>
              <el-button class="submit-btn" type="success" size="large" :loading="loading" @click="handleRegister">
                注 册
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="login-tip">
          <span class="tip-label">演示账号</span>
          admin / 123456（管理员）　user / 123456（普通用户）
        </div>
      </div>

      <div class="form-foot">青藏高原非物质文化遗产资源数据管理系统</div>
    </section>
    </div>

    <!-- 入场动画：纯色 → 刀锋划开 → 登录浮现 -->
    <div
      v-if="introVisible"
      class="intro"
      :class="{ 'is-open': introOpen }"
      role="button"
      tabindex="0"
      aria-label="点击进入系统"
      @click="startIntro"
      @keydown.enter="startIntro"
      @keydown.space.prevent="startIntro"
    >
      <div class="intro-shards">
        <span
          v-for="(shard, i) in SHARDS"
          :key="i"
          class="shard"
          :style="{
            clipPath: shard.clip,
            transformOrigin: shard.origin,
            '--tx': shard.tx,
            '--ty': shard.ty,
            '--rot': shard.rot,
            '--tint': shard.tint,
            animationDelay: `${shard.delay}ms`
          }"
        ></span>
      </div>

      <svg class="cracks" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
        <path
          v-for="(crack, i) in CRACKS"
          :key="i"
          :d="crack.d"
          pathLength="1"
          :style="{ animationDelay: `${crack.delay}ms` }"
        />
      </svg>

      <span class="impact"></span>

      <span
        v-for="(sliver, i) in SLIVERS"
        :key="'sliver-' + i"
        class="sliver"
        :style="{
          left: sliver.x,
          top: sliver.y,
          width: sliver.w,
          height: sliver.h,
          '--dx': sliver.dx,
          '--dy': sliver.dy,
          '--spin': sliver.spin,
          animationDelay: `${sliver.delay}ms`
        }"
      ></span>

      <div class="intro-frame"></div>

      <div class="intro-content">
        <div class="intro-seal">遗</div>
        <div class="intro-name">青藏高原非遗档案</div>
        <div class="intro-en">TIBETAN PLATEAU · HERITAGE ARCHIVE</div>
        <div class="intro-hint">
          <span class="hint-dot"></span>
          点击页面 · 划开卷轴
        </div>
      </div>

      <svg class="slash" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
        <path class="blade-glow" d="M -4 104 L 104 -4" pathLength="1" />
        <path class="blade-mid" d="M -4 104 L 104 -4" pathLength="1" />
        <path class="blade-core" d="M -4 104 L 104 -4" pathLength="1" />
      </svg>

      <span
        v-for="(spark, i) in SPARKS"
        :key="i"
        class="spark"
        :style="{ left: spark.x + '%', top: spark.y + '%', animationDelay: `${130 + i * 42}ms` }"
      ></span>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { userApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)

/* —— 入场动画 —— */
const introVisible = ref(true)
const introOpen = ref(false)
let introTimer = null

/** 刀痕划过时迸出的碎光，坐标沿左下 → 右上对角线分布 */
const SPARKS = [
  { x: 14, y: 82 },
  { x: 27, y: 69 },
  { x: 40, y: 56 },
  { x: 53, y: 44 },
  { x: 66, y: 31 },
  { x: 78, y: 19 },
  { x: 90, y: 7 }
]

/** 固定种子随机数，保证每次进入页面碎裂纹路形态一致 */
function seededRandom(seed) {
  return function () {
    seed = (seed * 1664525 + 1013904223) % 4294967296
    return seed / 4294967296
  }
}

const rand = seededRandom(20261021)

/** 冲击点附近的密集碎点 + 全屏随机点，用 Voronoi 划分出不规则玻璃碎片 */
const SITES = []
for (let i = 0; i < 6; i++) {
  const angle = rand() * Math.PI * 2
  const radius = 3 + rand() * 13
  SITES.push([50 + Math.cos(angle) * radius, 50 + Math.sin(angle) * radius])
}
for (let i = 0; i < 20; i++) {
  SITES.push([4 + rand() * 92, 4 + rand() * 92])
}

/** 用半平面裁剪多边形：保留离当前碎点更近的区域 */
function clipByBisector(poly, site, other) {
  const nx = other[0] - site[0]
  const ny = other[1] - site[1]
  const mx = (other[0] + site[0]) / 2
  const my = (other[1] + site[1]) / 2
  const out = []
  for (let i = 0; i < poly.length; i++) {
    const a = poly[i]
    const b = poly[(i + 1) % poly.length]
    const da = (a[0] - mx) * nx + (a[1] - my) * ny
    const db = (b[0] - mx) * nx + (b[1] - my) * ny
    if (da <= 0) out.push(a)
    if ((da <= 0) !== (db <= 0)) {
      const t = da / (da - db)
      out.push([a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t])
    }
  }
  return out
}

const CELLS = SITES.map((site, index) => {
  let poly = [[0, 0], [100, 0], [100, 100], [0, 100]]
  for (let j = 0; j < SITES.length; j++) {
    if (j === index) continue
    poly = clipByBisector(poly, site, SITES[j])
    if (poly.length < 3) break
  }
  return poly
})

const TINTS = ['#516a7d', '#5b7385', '#647c8e', '#4c6578']

/** 每块碎片沿“冲击点 → 自身重心”的方向飞出，附带不同旋转与延迟 */
const SHARDS = CELLS.map((poly, i) => {
  if (poly.length < 3) return null
  const cx = poly.reduce((sum, p) => sum + p[0], 0) / poly.length
  const cy = poly.reduce((sum, p) => sum + p[1], 0) / poly.length
  const dx = cx - 50
  const dy = cy - 50
  const dist = Math.hypot(dx, dy) || 1
  const travel = 24 + dist * 0.35 + rand() * 18
  return {
    clip: `polygon(${poly.map((p) => `${p[0].toFixed(2)}% ${p[1].toFixed(2)}%`).join(', ')})`,
    origin: `${cx.toFixed(2)}% ${cy.toFixed(2)}%`,
    tx: `${((dx / dist) * travel).toFixed(2)}%`,
    ty: `${(((dy / dist) * travel) * 0.9).toFixed(2)}%`,
    rot: `${(rand() > 0.5 ? 1 : -1) * (5 + rand() * 16)}deg`,
    delay: Math.round(340 + Math.min(dist, 66) * 2.1 + (i % 3) * 22),
    tint: TINTS[i % TINTS.length]
  }
}).filter(Boolean)

/** 提取所有碎片的公共边，作为从冲击点向外蔓延的裂纹 */
const edgeMap = new Map()
CELLS.forEach((poly) => {
  for (let i = 0; i < poly.length; i++) {
    const a = poly[i]
    const b = poly[(i + 1) % poly.length]
    const onBorder =
      (Math.abs(a[0]) < 0.05 && Math.abs(b[0]) < 0.05) ||
      (Math.abs(a[0] - 100) < 0.05 && Math.abs(b[0] - 100) < 0.05) ||
      (Math.abs(a[1]) < 0.05 && Math.abs(b[1]) < 0.05) ||
      (Math.abs(a[1] - 100) < 0.05 && Math.abs(b[1] - 100) < 0.05)
    if (onBorder) continue
    const key = [a, b]
      .map((p) => `${p[0].toFixed(1)},${p[1].toFixed(1)}`)
      .sort()
      .join('|')
    if (!edgeMap.has(key)) edgeMap.set(key, [a, b])
  }
})

const CRACKS = [...edgeMap.values()].map(([a, b], i) => {
  const mx = (a[0] + b[0]) / 2
  const my = (a[1] + b[1]) / 2
  const dist = Math.hypot(mx - 50, my - 50)
  return {
    d: `M ${a[0].toFixed(2)} ${a[1].toFixed(2)} L ${b[0].toFixed(2)} ${b[1].toFixed(2)}`,
    delay: Math.round(90 + dist * 5.5 + (i % 4) * 8)
  }
})

/** 从冲击点向外飞溅的细小玻璃碴 */
const SLIVERS = Array.from({ length: 18 }, () => {
  const angle = rand() * Math.PI * 2
  const radius = 2 + rand() * 14
  const travel = 18 + rand() * 34
  return {
    x: `${(50 + Math.cos(angle) * radius).toFixed(2)}%`,
    y: `${(50 + Math.sin(angle) * radius).toFixed(2)}%`,
    w: `${(5 + rand() * 14).toFixed(1)}px`,
    h: `${(2 + rand() * 4).toFixed(1)}px`,
    dx: `${(Math.cos(angle) * travel).toFixed(2)}vmax`,
    dy: `${(Math.sin(angle) * travel - 6).toFixed(2)}vmax`,
    spin: `${((rand() > 0.5 ? 1 : -1) * (180 + rand() * 540)).toFixed(0)}deg`,
    delay: Math.round(320 + rand() * 240)
  }
})

/** 点击幕布：刀痕划过 → 玻璃碎裂 → 登录界面浮现 */
function startIntro() {
  if (introOpen.value) return
  introOpen.value = true
  introTimer = setTimeout(() => {
    introVisible.value = false
  }, 1600)
}

onMounted(() => {
  // 尊重系统「减少动态效果」偏好：直接进入登录界面
  if (window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) {
    introOpen.value = true
    introVisible.value = false
  }
})

onBeforeUnmount(() => clearTimeout(introTimer))

const loginFormRef = ref()
const registerFormRef = ref()

const loginForm = reactive({ userAccount: '', userPwd: '' })
const registerForm = reactive({ userAccount: '', userPwd: '', nickname: '' })

const loginRules = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  userPwd: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  userPwd: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

/** 登录：token 与用户信息存入 Pinia */
async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push('/project')
  } finally {
    loading.value = false
  }
}

/** 注册：默认角色为普通用户 */
async function handleRegister() {
  await registerFormRef.value.validate()
  loading.value = true
  try {
    await userApi.register(registerForm)
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.userAccount = registerForm.userAccount
    loginForm.userPwd = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  height: 100%;
  overflow: hidden;
  background: var(--paper);
}

/* 登录界面本体：藏在纯色幕布下，待刀痕划开后浮现 */
.stage {
  display: flex;
  width: 100%;
  height: 100%;
  opacity: 0;
  transform: scale(1.045);
  filter: blur(10px);
  will-change: opacity, transform, filter;
}

.is-opening .stage {
  opacity: 1;
  transform: scale(1);
  filter: blur(0);
  transition:
    opacity 0.85s ease 0.34s,
    transform 1.05s cubic-bezier(0.16, 0.84, 0.3, 1) 0.28s,
    filter 0.85s ease 0.34s;
}

/* ============================ 品牌区 ============================ */

.brand-side {
  position: relative;
  flex: 1 1 56%;
  min-width: 460px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 44px 56px 40px 64px;
  overflow: hidden;
  color: #e9dcc4;
  background-color: var(--lapis-deep);
  background-image:
    radial-gradient(circle at 16% 8%, rgba(217, 164, 65, 0.18), transparent 42%),
    radial-gradient(circle at 88% 96%, rgba(166, 55, 44, 0.22), transparent 46%),
    repeating-linear-gradient(0deg, rgba(255, 255, 255, 0.024) 0 1px, transparent 1px 26px);
}

.frame {
  position: absolute;
  inset: 22px;
  border: 1px solid rgba(217, 164, 65, 0.22);
  pointer-events: none;
}

.frame::before,
.frame::after {
  content: '';
  position: absolute;
  width: 22px;
  height: 22px;
  border: 1px solid rgba(217, 164, 65, 0.65);
}

.frame::before {
  top: -1px;
  left: -1px;
  border-right: none;
  border-bottom: none;
}

.frame::after {
  right: -1px;
  bottom: -1px;
  border-left: none;
  border-top: none;
}

.watermark {
  position: absolute;
  right: 4%;
  top: 46%;
  transform: translateY(-50%);
  font-family: var(--font-display);
  font-size: 340px;
  line-height: 1;
  color: rgba(217, 164, 65, 0.045);
  pointer-events: none;
  user-select: none;
}

.brand-top {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-seal {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  font-family: var(--font-display);
  font-size: 21px;
  color: #f0d08e;
  border: 1px solid rgba(217, 164, 65, 0.6);
  box-shadow: inset 0 0 0 3px rgba(217, 164, 65, 0.1);
}

.brand-top-name {
  font-family: var(--font-display);
  font-size: 14px;
  letter-spacing: 0.2em;
  color: rgba(243, 230, 205, 0.78);
}

.brand-main {
  position: relative;
  max-width: 560px;
  padding-left: 4px;
}

.brand-eyebrow {
  font-size: 10px;
  letter-spacing: 0.34em;
  color: rgba(217, 164, 65, 0.72);
  margin-bottom: 22px;
}

.brand-main h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 52px;
  font-weight: 600;
  line-height: 1.28;
  letter-spacing: 0.12em;
  color: #f5e8cf;
  text-shadow: 0 2px 22px rgba(0, 0, 0, 0.25);
}

.rule {
  display: block;
  width: 56px;
  height: 2px;
  margin: 26px 0 22px;
  background: linear-gradient(90deg, var(--butter), rgba(217, 164, 65, 0.1));
}

.brand-desc {
  margin: 0;
  max-width: 420px;
  font-size: 13.5px;
  line-height: 2;
  letter-spacing: 0.06em;
  color: rgba(233, 220, 196, 0.72);
}

.keywords {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 30px;
  font-family: var(--font-display);
  font-size: 12px;
  letter-spacing: 0.16em;
  color: rgba(240, 208, 142, 0.66);
}

.keywords i {
  font-style: normal;
  color: rgba(217, 164, 65, 0.35);
}

.brand-foot {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.vertical-label {
  writing-mode: vertical-rl;
  font-family: var(--font-display);
  font-size: 11px;
  letter-spacing: 0.4em;
  color: rgba(233, 220, 196, 0.34);
}

.brand-en {
  font-size: 9px;
  letter-spacing: 0.28em;
  color: rgba(233, 220, 196, 0.34);
}

/* ============================ 表单区 ============================ */

.form-side {
  position: relative;
  flex: 0 0 44%;
  min-width: 440px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 56px;
  background-color: var(--paper-2);
  background-image: radial-gradient(rgba(120, 90, 55, 0.05) 1px, transparent 1px);
  background-size: 18px 18px;
  border-left: 1px solid var(--line);
}

.form-box {
  width: 100%;
  max-width: 372px;
  animation: rise-in 0.6s cubic-bezier(0.2, 0.7, 0.3, 1) both;
}

.form-head .eyebrow {
  margin-bottom: 12px;
}

.form-head h2 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: var(--ink);
}

.form-head p {
  margin: 10px 0 26px;
  font-size: 12.5px;
  letter-spacing: 0.04em;
  color: var(--ink-4);
}

.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 22px;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: var(--line);
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  letter-spacing: 0.14em;
  padding-left: 0;
  margin-right: 28px;
}

.login-tabs :deep(.el-tabs__item:first-child) {
  padding-left: 0;
}

.login-tabs :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-tabs :deep(.el-input__wrapper) {
  height: 46px;
  background: var(--paper-3);
}

.submit-btn {
  width: 100%;
  height: 46px;
  margin-top: 6px;
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5em;
  text-indent: 0.5em;
}

.login-tip {
  margin-top: 24px;
  padding: 12px 14px;
  font-size: 11.5px;
  line-height: 1.8;
  letter-spacing: 0.03em;
  color: var(--ink-3);
  background: rgba(241, 231, 213, 0.55);
  border: 1px dashed var(--line-strong);
}

.tip-label {
  display: inline-block;
  margin-right: 8px;
  padding: 1px 6px;
  font-size: 10px;
  letter-spacing: 0.14em;
  color: var(--tibet-red);
  border: 1px solid currentColor;
}

.form-foot {
  position: absolute;
  bottom: 22px;
  right: 36px;
  font-size: 10px;
  letter-spacing: 0.12em;
  color: var(--ink-4);
}

/* ============================ 入场动画 ============================ */

/* 纯色幕布：点击后沿左下 → 右上被刀锋划开 */
.intro {
  position: absolute;
  inset: 0;
  z-index: 60;
  overflow: hidden;
  cursor: pointer;
  outline: none;
  background-color: var(--lapis-deep);
  transition: background-color 0.35s ease 0.08s;
}

/* 碎裂开始后，底层让出，露出正在浮现的登录界面 */
.intro.is-open {
  background-color: transparent;
  pointer-events: none;
}

/* 玻璃碎片：以屏幕中心为受力点，向四周放射 */
.intro-shards {
  position: absolute;
  inset: 0;
  z-index: 1;
  overflow: hidden;
  pointer-events: none;
}

.shard {
  position: absolute;
  inset: 0;
  background: var(--lapis-deep);
  backface-visibility: hidden;
  will-change: transform, opacity, background-color;
}

/* 碎片飞散时掠过的玻璃反光 */
.shard::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    118deg,
    transparent 28%,
    rgba(226, 244, 255, 0.26) 44%,
    rgba(255, 255, 255, 0.06) 52%,
    transparent 68%
  );
  opacity: 0;
  transition: opacity 0.22s ease 0.32s;
}

.intro.is-open .shard::after {
  opacity: 1;
}

.intro.is-open .shard {
  background-color: var(--tint, #5b7284);
  animation: shard-fly 0.88s cubic-bezier(0.2, 0.7, 0.3, 1) both;
  transition: background-color 0.6s ease;
}

@keyframes shard-fly {
  0% {
    transform: none;
    opacity: 1;
  }
  10% {
    opacity: 1;
  }
  100% {
    transform: translate(var(--tx), var(--ty)) rotate(var(--rot)) scale(1.02);
    opacity: 0;
  }
}

/* 裂纹：从冲击点向屏幕边缘蔓延 */
.cracks {
  position: absolute;
  inset: 0;
  z-index: 2;
  width: 100%;
  height: 100%;
  overflow: visible;
  pointer-events: none;
}

.cracks path {
  fill: none;
  stroke: rgba(216, 235, 247, 0.5);
  stroke-width: 1px;
  stroke-linecap: round;
  stroke-dasharray: 1;
  stroke-dashoffset: 1;
  vector-effect: non-scaling-stroke;
}

.intro.is-open .cracks path {
  animation: crack-draw 0.32s ease-out forwards;
}

.intro.is-open .cracks {
  animation: crack-fade 0.5s ease 0.6s forwards;
}

@keyframes crack-draw {
  to {
    stroke-dashoffset: 0;
  }
}

@keyframes crack-fade {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

/* 冲击点白光 */
.impact {
  position: absolute;
  left: 50%;
  top: 50%;
  z-index: 3;
  width: 46vmax;
  height: 46vmax;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 248, 226, 0.5), rgba(255, 214, 140, 0.16) 38%, transparent 68%);
  transform: translate(-50%, -50%) scale(0.25);
  opacity: 0;
  pointer-events: none;
}

.intro.is-open .impact {
  animation: impact-flash 0.55s ease-out 0.1s forwards;
}

@keyframes impact-flash {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.25);
  }
  25% {
    opacity: 0.9;
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(1);
  }
}

/* 飞溅的细小玻璃碴：从冲击点向外抛出，边飞边翻转 */
.sliver {
  position: absolute;
  z-index: 5;
  background:
    linear-gradient(96deg, rgba(255, 255, 255, 0.95), rgba(176, 214, 236, 0.72) 48%, rgba(255, 255, 255, 0.86));
  clip-path: polygon(0% 62%, 26% 8%, 74% 0%, 100% 38%, 68% 100%, 22% 86%);
  box-shadow: 0 0 8px rgba(198, 232, 252, 0.45);
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.4);
  pointer-events: none;
  will-change: transform, opacity;
}

.intro.is-open .sliver {
  animation: sliver-fly 0.82s cubic-bezier(0.15, 0.72, 0.28, 1) both;
}

@keyframes sliver-fly {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.3);
  }
  10% {
    opacity: 1;
  }
  68% {
    opacity: 0.95;
  }
  100% {
    opacity: 0;
    transform: translate(calc(-50% + var(--dx)), calc(-50% + var(--dy))) rotate(var(--spin)) scale(0.85);
  }
}

.intro-frame {
  position: absolute;
  inset: 26px;
  z-index: 4;
  border: 1px solid rgba(217, 164, 65, 0.18);
  pointer-events: none;
  transition: opacity 0.4s ease 0.25s;
}

.intro-frame::before,
.intro-frame::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  border: 1px solid rgba(217, 164, 65, 0.5);
}

.intro-frame::before {
  top: -1px;
  left: -1px;
  border-right: none;
  border-bottom: none;
}

.intro-frame::after {
  right: -1px;
  bottom: -1px;
  border-left: none;
  border-top: none;
}

.intro.is-open .intro-frame {
  opacity: 0;
}

/* 幕布中央提示 */
.intro-content {
  position: absolute;
  inset: 0;
  z-index: 5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  transition: opacity 0.18s ease;
  user-select: none;
}

.intro.is-open .intro-content {
  opacity: 0;
}

.intro-seal {
  width: 64px;
  height: 64px;
  display: grid;
  place-items: center;
  font-family: var(--font-display);
  font-size: 32px;
  color: #f0d08e;
  border: 1px solid rgba(217, 164, 65, 0.6);
  box-shadow: inset 0 0 0 4px rgba(217, 164, 65, 0.1), 0 0 44px rgba(217, 164, 65, 0.08);
}

.intro-name {
  margin-top: 24px;
  font-family: var(--font-display);
  font-size: 19px;
  letter-spacing: 0.28em;
  color: #f2e4c8;
}

.intro-en {
  margin-top: 12px;
  font-size: 9px;
  letter-spacing: 0.3em;
  color: rgba(217, 164, 65, 0.55);
}

.intro-hint {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 52px;
  font-size: 11.5px;
  letter-spacing: 0.2em;
  color: rgba(233, 220, 196, 0.58);
}

.hint-dot {
  position: relative;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--butter);
}

.hint-dot::after {
  content: '';
  position: absolute;
  inset: -5px;
  border: 1px solid rgba(217, 164, 65, 0.55);
  border-radius: 50%;
  animation: hint-pulse 1.9s ease-out infinite;
}

@keyframes hint-pulse {
  0% {
    transform: scale(0.6);
    opacity: 0;
  }
  30% {
    opacity: 1;
  }
  100% {
    transform: scale(1.9);
    opacity: 0;
  }
}

/* 刀痕：三条路径叠出锋刃与光晕 */
.slash {
  position: absolute;
  inset: 0;
  z-index: 6;
  width: 100%;
  height: 100%;
  overflow: visible;
  opacity: 0;
  pointer-events: none;
}

.slash path {
  fill: none;
  stroke-linecap: round;
  stroke-dasharray: 1;
  stroke-dashoffset: 1;
  vector-effect: non-scaling-stroke;
}

.blade-core {
  stroke: #fffaf0;
  stroke-width: 1.8px;
}

.blade-mid {
  stroke: rgba(255, 230, 174, 0.7);
  stroke-width: 7px;
}

.blade-glow {
  stroke: rgba(255, 190, 96, 0.32);
  stroke-width: 22px;
}

.intro.is-open .slash {
  opacity: 1;
  animation: scar-fade 1.05s ease 0.45s forwards;
}

.intro.is-open .blade-core {
  animation: blade-draw 0.34s cubic-bezier(0.35, 0, 0.35, 1) forwards;
}

.intro.is-open .blade-mid {
  animation: blade-draw 0.38s cubic-bezier(0.35, 0, 0.35, 1) 0.02s forwards;
}

.intro.is-open .blade-glow {
  animation: blade-draw 0.44s cubic-bezier(0.35, 0, 0.35, 1) 0.03s forwards;
}

@keyframes blade-draw {
  to {
    stroke-dashoffset: 0;
  }
}

@keyframes scar-fade {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

/* 刀锋过境时迸出的碎光 */
.spark {
  position: absolute;
  z-index: 7;
  width: 2px;
  height: 16px;
  background: linear-gradient(180deg, rgba(255, 247, 222, 0), #fff2d0, rgba(255, 214, 140, 0));
  transform: translate(-50%, -50%) rotate(-45deg);
  opacity: 0;
  pointer-events: none;
}

.intro.is-open .spark {
  animation: spark-fly 0.55s ease-out forwards;
}

@keyframes spark-fly {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) rotate(-45deg) scaleY(0.2);
  }
  25% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -150%) rotate(-45deg) scaleY(1);
  }
}

@media (max-width: 960px) {
  .stage {
    flex-direction: column;
  }

  .brand-side {
    flex: 0 0 auto;
    min-width: 0;
    padding: 26px 28px 24px;
  }

  .brand-main {
    margin: 22px 0 12px;
  }

  .brand-main h1 {
    font-size: 28px;
    letter-spacing: 0.08em;
  }

  .brand-desc,
  .keywords,
  .brand-foot,
  .watermark {
    display: none;
  }

  .rule {
    margin: 16px 0 0;
  }

  .form-side {
    flex: 1;
    min-width: 0;
    padding: 32px 28px 56px;
    border-left: none;
    border-top: 1px solid var(--line);
  }
}
</style>
