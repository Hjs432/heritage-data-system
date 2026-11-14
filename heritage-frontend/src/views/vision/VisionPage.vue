<template>
  <div class="vision">
    <!-- 头部 -->
    <div class="vision-head">
      <div class="vh-left">
        <span class="vh-mark">视界</span>
        <div class="vh-titles">
          <h2>非遗资源空间视界</h2>
          <p>中国版图 · 俯视视角 · 项目地理分布</p>
        </div>
      </div>
      <div class="vh-right">
        <div class="vh-stat"><b>{{ projects.length }}</b><span>项目</span></div>
        <div class="vh-stat"><b>{{ regionCount }}</b><span>地区</span></div>
        <div class="vh-stat"><b>{{ provinceCount }}</b><span>覆盖省区</span></div>
      </div>
    </div>

    <!-- 地图舞台 -->
    <div ref="stageRef" class="stage" :class="{ 'is-dragging': dragging }">
      <div class="stage-grid"></div>
      <div class="stage-halo"></div>

      <div class="plate">
        <svg
          ref="svgRef"
          class="map-svg"
          :viewBox="`0 0 ${MAP_W} ${MAP_H}`"
          preserveAspectRatio="xMidYMid meet"
          @wheel.prevent="onWheel"
          @mousedown.prevent="onDown"
          @dblclick="zoomBy(1.7)"
        >
          <defs>
            <clipPath id="markerClip" clipPathUnits="objectBoundingBox">
              <circle cx="0.5" cy="0.5" r="0.5" />
            </clipPath>
            <linearGradient id="pvPlain" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#2e5170" />
              <stop offset="100%" stop-color="#1b3247" />
            </linearGradient>
            <linearGradient id="pvData" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#6d5a3c" />
              <stop offset="55%" stop-color="#5b4a31" />
              <stop offset="100%" stop-color="#40331f" />
            </linearGradient>
            <filter id="pvGlow" x="-30%" y="-30%" width="160%" height="160%">
              <feGaussianBlur stdDeviation="4" result="b" />
              <feMerge>
                <feMergeNode in="b" />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>
          </defs>

          <g :transform="gTransform">
            <!-- 版图厚度（俯视下向右下偏移，形成版图自身的厚度） -->
            <g class="depth">
              <g v-for="(dy, i) in DEPTH" :key="dy" :transform="`translate(${dy * 0.5} ${dy})`">
                <path
                  v-for="p in provinces"
                  :key="p.name"
                  :d="p.d"
                  :fill="['#081119', '#0b1925', '#0f2231'][i]"
                />
              </g>
            </g>

            <!-- 省级行政区 -->
            <g class="provinces">
              <path
                v-for="p in provinces"
                :key="p.name"
                class="prov"
                :class="{ 'is-data': p.data }"
                :d="p.d"
                :fill="p.data ? 'url(#pvData)' : 'url(#pvPlain)'"
                stroke="rgba(240, 223, 190, 0.62)"
                stroke-width="1"
                stroke-linejoin="round"
                vector-effect="non-scaling-stroke"
              />
            </g>

            <!-- 地级边界：放大后显示，细化省内划分 -->
            <g v-if="tier >= 1 && !rotating" class="cities">
              <path v-for="c in cities" :key="c.name" :d="c.d" vector-effect="non-scaling-stroke" />
            </g>

            <!-- 省简称（最小缩放时显示） -->
            <g class="abbrs" :class="{ 'is-on': tier === 0 }">
              <text
                v-for="p in provinces"
                :key="'abbr-' + p.name"
                :x="p.cx"
                :y="p.cy"
                :font-size="12.5 * u"
                :fill="p.data ? 'rgba(244, 226, 191, 0.92)' : 'rgba(200, 218, 232, 0.62)'"
                text-anchor="middle"
                dominant-baseline="middle"
                class="abbr-text"
              >
                {{ p.abbr }}
              </text>
            </g>

            <!-- 地区锚点 -->
            <g v-if="tier >= 1" class="anchors">
              <g v-for="a in regionLabels" :key="'anchor-' + a.region">
                <circle :cx="a.x" :cy="a.y" :r="8 * u" fill="none" stroke="rgba(217, 164, 65, 0.75)" stroke-width="1.4" vector-effect="non-scaling-stroke" class="anchor-ring" />
                <circle :cx="a.x" :cy="a.y" :r="2.6 * u" fill="#f3d79a" class="anchor-core" />
              </g>
            </g>

            <!-- 项目标注：小圆球 + 指向坐标的箭头 -->
            <g v-if="tier >= 1" class="markers">
              <g
                v-for="m in markers"
                :key="m.id"
                class="marker"
                :class="{ 'is-active': activeId === m.id }"
                @click.stop="openPreview(m)"
              >
                <title>{{ m.projectName }} · {{ m.region }}（点击放大）</title>
                <!-- 引线（指向真实坐标） -->
                <line
                  :x1="m.ax"
                  :y1="m.ay"
                  :x2="m.x"
                  :y2="m.y"
                  stroke="rgba(243, 215, 154, 0.5)"
                  stroke-width="1"
                  vector-effect="non-scaling-stroke"
                />
                <!-- 箭头（尖端落在坐标上） -->
                <path
                  :d="arrowPath(m)"
                  :fill="categoryColor(m.projectCategory)"
                  opacity="0.9"
                />
                <!-- 缩略图标记：底色圆做加载失败兜底，图片按圆形裁剪 -->
                <circle
                  :cx="m.x"
                  :cy="m.y"
                  :r="12 * u"
                  :fill="categoryColor(m.projectCategory)"
                  stroke="rgba(251, 247, 238, 0.85)"
                  :stroke-width="1.6 * u"
                />
                <image
                  v-if="m.thumb"
                  :href="m.thumb"
                  :x="m.x - 12 * u"
                  :y="m.y - 12 * u"
                  :width="24 * u"
                  :height="24 * u"
                  clip-path="url(#markerClip)"
                  preserveAspectRatio="xMidYMid slice"
                  class="marker-photo"
                />
                <circle
                  :cx="m.x"
                  :cy="m.y"
                  :r="12 * u"
                  fill="none"
                  stroke="rgba(251, 247, 238, 0.9)"
                  :stroke-width="1.6 * u"
                />
                <!-- 地点小球：箭头尖端正落在它上面，标示项目实际坐标 -->
                <circle
                  :cx="m.ax"
                  :cy="m.ay"
                  :r="6 * u"
                  fill="none"
                  stroke="rgba(243, 215, 154, 0.42)"
                  :stroke-width="1.2 * u"
                />
                <circle
                  :cx="m.ax"
                  :cy="m.ay"
                  :r="2.8 * u"
                  fill="#f3d79a"
                  stroke="rgba(38, 26, 16, 0.6)"
                  :stroke-width="0.9 * u"
                />
                <!-- 项目名在高倍时统一放到地区标签下方，避免与圆球重叠 -->
              </g>
            </g>

            <!-- 具体地点（高倍时显示） -->
            <g v-if="tier === 2" class="places">
              <text
                v-for="a in regionLabels"
                :key="'place-' + a.region"
                :x="a.x"
                :y="a.y + (a.up ? 26 : -26) * u"
                :font-size="12.5 * u"
                :stroke-width="3.2 * u"
                text-anchor="middle"
                class="place-text"
              >
                {{ a.region }}
              </text>
              <text
                v-for="a in regionLabels"
                :key="'count-' + a.region"
                :x="a.x"
                :y="a.y + (a.up ? 39 : -39) * u"
                :font-size="9.5 * u"
                :stroke-width="2.6 * u"
                text-anchor="middle"
                class="place-count"
              >
                {{ a.count > 1 ? `共 ${a.count} 项` : shortName(a.first) }}
              </text>
            </g>
          </g>
        </svg>

        <div class="plate-rim"></div>
      </div>

      <!-- 缩放控件 -->
      <div class="zoom-panel">
        <button type="button" title="放大" @click="zoomBy(1.45)">＋</button>
        <div class="zoom-readout">{{ Math.round(zoom * 100) }}<i>%</i></div>
        <button type="button" title="缩小" @click="zoomBy(1 / 1.45)">－</button>
        <button type="button" class="zoom-reset" title="回到整图" @click="resetView">整图</button>
      </div>

      <!-- 层级提示 -->
      <div class="tier-bar">
        <span :class="{ 'is-on': tier === 0 }"><i>01</i>省域简称</span>
        <em></em>
        <span :class="{ 'is-on': tier === 1 }"><i>02</i>项目分布</span>
        <em></em>
        <span :class="{ 'is-on': tier === 2 }"><i>03</i>具体地点</span>
      </div>

      <div v-if="loading" class="map-loading">
        <span></span>
        正在投影版图…
      </div>
    </div>

    <!-- 底部图例栏：横向铺开的类别色标，不再遮挡版图 -->
    <div class="vision-foot">
      <div class="foot-legend">
        <span class="foot-label">类别图例</span>
        <div class="legend-items">
          <span v-for="c in categoryStats" :key="c.name" class="legend-item">
            <i :style="{ background: c.color }"></i>
            <span class="legend-name">{{ c.name }}</span>
            <b>{{ c.count }}</b>
          </span>
        </div>
      </div>
      <div class="foot-hint">Ctrl + 拖拽旋转 · 滚轮缩放 · 拖拽平移 · 点击图片放大</div>
    </div>

    <!-- 图片放大预览 -->
    <Teleport to="body">
      <div v-if="preview" class="pv-mask" @click.self="preview = null">
        <figure class="pv-card">
          <img :src="preview.list[preview.index]" :alt="preview.name" />
          <div v-if="preview.list.length > 1" class="pv-dots">
            <button
              v-for="(u, i) in preview.list"
              :key="u"
              type="button"
              :class="{ on: i === preview.index }"
              :aria-label="`第 ${i + 1} 张`"
              @click.stop="preview.index = i"
            />
          </div>
          <figcaption>
            <div class="pv-text">
              <div class="pv-name">{{ preview.name }}</div>
              <div class="pv-meta">{{ preview.category }} · {{ preview.region }} · 点击空白处关闭</div>
            </div>
            <el-button type="primary" size="small" @click="viewPreviewProject">查看项目详情</el-button>
          </figcaption>
        </figure>
      </div>
    </Teleport>

    <HeritageDialog ref="dialogRef" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { projectApi } from '../../api'
import geo from '../../assets/china-geo.json'
import cityGeo from '../../assets/china-cities.json'
import projectCoords from '../../assets/project-coords.json'
import projectImages from '../../assets/project-images.json'
import projectPhotos from '../../assets/project-photos.json'
import HeritageDialog from '../../components/HeritageDialog.vue'
import { categoryColor, CATEGORY_LEGEND } from '../../utils/heritage'

/* —————————— 投影：经纬度 → 版面坐标 —————————— */
const MAP_W = 1000
const MAP_H = 720
const LNG0 = 73.2
const LNG1 = 135.5
const LAT0 = 17.5
const LAT1 = 53.7
const PAD = 26

const mercY = (lat) => Math.log(Math.tan(Math.PI / 4 + (lat * Math.PI) / 360))
const RAD2DEG = 180 / Math.PI
const spanX = LNG1 - LNG0
/* 墨卡托 y 是弧度，必须换算成“度”才能和经度同一比例尺，否则版图会被压成一条线 */
const spanY = (mercY(LAT1) - mercY(LAT0)) * RAD2DEG
const scale = Math.min((MAP_W - PAD * 2) / spanX, (MAP_H - PAD * 2) / spanY)
const offX = (MAP_W - spanX * scale) / 2
const offY = (MAP_H - spanY * scale) / 2

function proj(lng, lat) {
  return [offX + (lng - LNG0) * scale, offY + (mercY(LAT1) - mercY(lat)) * RAD2DEG * scale]
}

/* —————————— 斜视投影：几何变换把版图压成“斜着俯看”的梯形 ——————————
 * 在 SVG 坐标里做（而非 CSS 3D），任意缩放都保持矢量清晰。
 * yaw 绕竖轴左右旋转、pitch 控制俯仰角，按住 Ctrl 拖拽即可像陀螺一样转动。 */
const SLANT_D = 1400 // 观察距离，越小透视越强
const SLANT_FIT = 0.9
const SLANT_Y_BIAS = -48

const yawDeg = ref(0)
const pitchDeg = ref(34)

/** 平面坐标 → 斜视坐标（先绕竖轴 yaw，再绕横轴 pitch，最后透视） */
function makeTransform() {
  const yaw = (yawDeg.value * Math.PI) / 180
  const pitch = (pitchDeg.value * Math.PI) / 180
  const cyw = Math.cos(yaw)
  const syw = Math.sin(yaw)
  const cp = Math.cos(pitch)
  const sp = Math.sin(pitch)
  return (x, y) => {
    const x0 = x - MAP_W / 2
    const y0 = y - MAP_H / 2
    const xr = x0 * cyw
    const zr = -x0 * syw
    const yr = y0 * cp - zr * sp
    const zr2 = y0 * sp + zr * cp
    const f = SLANT_D / (SLANT_D - zr2)
    return [MAP_W / 2 + xr * f * SLANT_FIT, MAP_H / 2 + (yr * f + SLANT_Y_BIAS) * SLANT_FIT]
  }
}

function slant(x, y) {
  return makeTransform()(x, y)
}

/* —————————— 项目地区 → 经纬度 —————————— */
const REGION_COORD = {
  西藏拉萨: [91.14, 29.65],
  西藏山南: [91.77, 29.24],
  西藏那曲: [92.05, 31.48],
  西藏昌都: [97.17, 31.14],
  西藏日喀则: [88.88, 29.27],
  西藏林芝: [94.36, 29.65],
  西藏阿里: [80.1, 32.5],
  青海玉树: [97.0, 33.0],
  青海西宁: [101.78, 36.62],
  青海黄南: [102.02, 35.52],
  青海果洛: [100.24, 34.48],
  青海海东: [102.1, 36.5],
  青海海南: [100.62, 36.28],
  青海海西: [97.37, 37.37],
  甘肃甘南: [102.91, 34.99],
  云南迪庆: [99.71, 27.82],
  云南怒江: [98.86, 25.85],
  四川甘孜: [101.96, 30.05],
  四川阿坝: [102.22, 31.9],
  四川凉山: [102.26, 27.88],
  四川绵阳: [104.68, 31.47],
  四川成都: [104.07, 30.57]
}

const DATA_PROVINCES = new Set(['西藏自治区', '青海省', '四川省', '甘肃省', '云南省'])

/* —————————— 省界路径（随旋转角度重算，三角函数只算一次） —————————— */
const provinces = computed(() => {
  const toScreen = makeTransform()
  return geo.map((p) => {
    const [cx, cy] = toScreen(...proj(p.c[0], p.c[1]))
    const d = p.p
      .map(
        (ring) =>
          'M' +
          ring
            .map(([lng, lat]) => {
              const [x, y] = toScreen(...proj(lng, lat))
              return `${x.toFixed(1)},${y.toFixed(1)}`
            })
            .join('L') +
          'Z'
      )
      .join('')
    return { name: p.name, abbr: p.abbr, d, cx, cy, data: DATA_PROVINCES.has(p.name) }
  })
})

/* —————————— 地级边界（细化省内划分，仅放大后渲染） —————————— */
const cities = computed(() => {
  const toScreen = makeTransform()
  return cityGeo.map((c) => {
    const d = c.p
      .map(
        (ring) =>
          'M' +
          ring
            .map(([lng, lat]) => {
              const [x, y] = toScreen(...proj(lng, lat))
              return `${x.toFixed(1)},${y.toFixed(1)}`
            })
            .join('L') +
          'Z'
      )
      .join('')
    return { name: c.name, d }
  })
})

const DEPTH = [18, 12, 6]

/* —————————— 缩放 / 平移 —————————— */
const MIN_ZOOM = 1
const MAX_ZOOM = 7

const s = ref(1)
const cx = ref(MAP_W / 2)
const cy = ref(MAP_H / 2)
const zoom = computed(() => s.value)

const gTransform = computed(
  () =>
    `translate(${MAP_W / 2} ${MAP_H / 2}) scale(${s.value}) translate(${-cx.value} ${-cy.value})`
)

const tier = computed(() => (s.value < 1.55 ? 0 : s.value < 3 ? 1 : 2))

function clampView() {
  const halfW = MAP_W / (2 * s.value)
  const halfH = MAP_H / (2 * s.value)
  cx.value = Math.min(MAP_W - halfW, Math.max(halfW, cx.value))
  cy.value = Math.min(MAP_H - halfH, Math.max(halfH, cy.value))
}

const stageRef = ref(null)
const svgRef = ref(null)

/** 屏幕像素 / 版面坐标 的换算基准（容器尺寸变化时更新） */
const unitPx = ref(0.7)

function metrics() {
  const el = svgRef.value
  const r = el.getBoundingClientRect()
  const k = Math.min(el.clientWidth / MAP_W, el.clientHeight / MAP_H)
  return {
    cx: r.left + r.width / 2,
    cy: r.top + r.height / 2,
    drawW: MAP_W * k,
    drawH: MAP_H * k,
    k
  }
}

function measure() {
  if (!svgRef.value) return
  unitPx.value = metrics().k || 0.7
}

/** 屏幕上恒定 1px 对应多少版面单位（缩放与容器尺寸双重补偿） */
const u = computed(() => 1 / (s.value * unitPx.value))

/** 屏幕坐标 → 版面归一化坐标（0~1，超出表示在版图外） */
function normalize(clientX, clientY) {
  const m = metrics()
  return [0.5 + (clientX - m.cx) / m.drawW, 0.5 + (clientY - m.cy) / m.drawH]
}

function zoomAt(next, nx, ny) {
  const target = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, next))
  if (target === s.value) return
  const mapX = cx.value + (nx - 0.5) * (MAP_W / s.value)
  const mapY = cy.value + (ny - 0.5) * (MAP_H / s.value)
  s.value = target
  cx.value = mapX - (nx - 0.5) * (MAP_W / target)
  cy.value = mapY - (ny - 0.5) * (MAP_H / target)
  clampView()
}

function zoomBy(factor) {
  zoomAt(s.value * factor, 0.5, 0.5)
}

function resetView() {
  s.value = 1
  cx.value = MAP_W / 2
  cy.value = MAP_H / 2
  yawDeg.value = 0
  pitchDeg.value = 34
}

function onWheel(e) {
  const [nx, ny] = normalize(e.clientX, e.clientY)
  zoomAt(s.value * Math.exp(-e.deltaY * 0.0016), nx, ny)
}

const dragging = ref(false)
const rotating = ref(false)
const suppressClick = ref(false)
let dragState = null

function onDown(e) {
  dragState = {
    startX: e.clientX,
    startY: e.clientY,
    originCx: cx.value,
    originCy: cy.value,
    moved: 0,
    // 按住 Ctrl / ⌘ 时进入“地球仪”旋转模式
    rotate: e.ctrlKey || e.metaKey,
    startYaw: yawDeg.value,
    startPitch: pitchDeg.value
  }
  dragging.value = true
  rotating.value = dragState.rotate
  window.addEventListener('mousemove', onMove)
  window.addEventListener('mouseup', onUp)
}

function onMove(e) {
  if (!dragState) return
  const dx = e.clientX - dragState.startX
  const dy = e.clientY - dragState.startY
  dragState.moved = Math.max(dragState.moved, Math.hypot(dx, dy))
  if (dragState.rotate) {
    // 横向拖动绕竖轴转，纵向拖动改俯仰角
    yawDeg.value = Math.max(-42, Math.min(42, dragState.startYaw + dx * 0.22))
    pitchDeg.value = Math.max(16, Math.min(68, dragState.startPitch - dy * 0.18))
    return
  }
  const m = metrics()
  cx.value = dragState.originCx - (dx / m.drawW) * (MAP_W / s.value)
  cy.value = dragState.originCy - (dy / m.drawH) * (MAP_H / s.value)
  clampView()
}

function onUp() {
  dragging.value = false
  rotating.value = false
  // 拖拽过就不再触发标注点击，避免误开弹窗
  if (dragState && dragState.moved > 5) suppressClick.value = true
  dragState = null
  window.removeEventListener('mousemove', onMove)
  window.removeEventListener('mouseup', onUp)
  setTimeout(() => (suppressClick.value = false), 0)
}

/* —————————— 数据 —————————— */
const loading = ref(false)
const projects = ref([])
const dialogRef = ref(null)
const activeId = ref('')

const regionCount = computed(() => new Set(projects.value.map((p) => p.region)).size)
const provinceCount = computed(
  () => new Set(projects.value.map((p) => (p.region || '').slice(0, 2))).size
)

const regionLabels = computed(() => {
  const acc = new Map()
  for (const p of projects.value) {
    if (!REGION_COORD[p.region] && !projectCoords[p.projectName]) continue
    const coord = projectCoords[p.projectName] || REGION_COORD[p.region]
    const cur = acc.get(p.region)
    if (cur) {
      cur.lng += coord[0]
      cur.lat += coord[1]
      cur.n += 1
    } else {
      acc.set(p.region, { lng: coord[0], lat: coord[1], n: 1, first: p.projectName })
    }
  }
  return [...acc.entries()].map(([region, a]) => {
    // 地区名落在该地区项目坐标的质心，避免与项目簇脱节
    const [px, py] = proj(a.lng / a.n, a.lat / a.n)
    const [x, y] = slant(px, py)
    return { region, x, y, up: y > MAP_H / 2, count: a.n, first: a.first }
  })
})

/**
 * 项目标注布局：优先使用项目精确坐标（project-coords.json），
 * 同一坐标上的多个项目按网格顺序排布，避免重叠；
 * 尺寸按 1/缩放 补偿，保证任何缩放级别下球体与文字在屏幕上大小一致，
 * 而锚点（ax, ay）始终是真实地理位置，缩放与旋转都不会漂移。
 */
const markers = computed(() => {
  const u = 1 / (s.value * unitPx.value)
  const groups = new Map()
  for (const p of projects.value) {
    const coord = projectCoords[p.projectName] || REGION_COORD[p.region]
    if (!coord) continue
    const key = `${coord[0].toFixed(3)},${coord[1].toFixed(3)}`
    if (!groups.has(key)) groups.set(key, { coord, list: [] })
    groups.get(key).list.push(p)
  }

  const out = []
  groups.forEach(({ coord, list }) => {
    const [px, py] = proj(coord[0], coord[1])
    const [ax, ay] = slant(px, py)
    const n = list.length
    const cols = n <= 4 ? n : Math.ceil(Math.sqrt(n))
    const sx = 34 * u
    const sy = 32 * u
    const up = ay > MAP_H / 2
    const base = 46 * u
    list.forEach((p, i) => {
      const col = i % cols
      const row = Math.floor(i / cols)
      const dx = (col - (cols - 1) / 2) * sx
      const dy = up ? -(base + row * sy) : base + row * sy
      out.push({
        id: p.id,
        projectName: p.projectName,
        projectCategory: p.projectCategory,
        region: p.region,
        p,
        thumb: thumbOf(p),
        ax,
        ay,
        x: ax + dx,
        y: ay + dy,
        groupSize: n,
        isFirstOfGroup: i === 0
      })
    })
  })
  return out
})

const categoryStats = computed(() => {
  const counter = new Map()
  for (const p of projects.value) {
    counter.set(p.projectCategory, (counter.get(p.projectCategory) || 0) + 1)
  }
  return CATEGORY_LEGEND.map((c) => ({ ...c, count: counter.get(c.name) || 0 })).filter(
    (c) => c.count > 0
  )
})

/** 地图标签只保留前 12 个字，长项目名不铺开压住版图 */
function shortName(name) {
  if (!name) return ''
  return name.length > 12 ? name.slice(0, 12) + '…' : name
}

function arrowPath(m) {
  const u = 1 / (s.value * unitPx.value)
  // 箭头落在真实坐标上，方向由标注指向坐标
  const dx = m.ax - m.x
  const dy = m.ay - m.y
  const len = Math.hypot(dx, dy) || 1
  const tipX = m.ax
  const tipY = m.ay
  const backX = tipX - (dx / len) * 9 * u
  const backY = tipY - (dy / len) * 9 * u
  const px = (-dy / len) * 3.6 * u
  const py = (dx / len) * 3.6 * u
  return `M ${tipX} ${tipY} L ${backX + px} ${backY + py} L ${backX - px} ${backY - py} Z`
}

/** 项目缩略图：优先真实图片，否则按类别图库、以项目名散列稳定取一张 */
function hashOf(str) {
  let h = 0
  for (let i = 0; i < str.length; i += 1) h = (h * 31 + str.charCodeAt(i)) >>> 0
  return h
}

function thumbOf(p) {
  const photo = projectPhotos[p.projectName]
  if (photo && photo.thumb) return photo.thumb
  const local = (p.mediaList || []).find((m) => m.url && m.url.startsWith('/'))
  if (local) return local.url
  const arr = projectImages[p.projectCategory]
  if (!arr || !arr.length) return ''
  const pool = arr.filter((u) => u.startsWith('/'))
  const list = pool.length ? pool : arr
  return list[hashOf(p.projectName) % list.length]
}

const preview = ref(null)

/** 点击缩略图：有图则放大预览，无图直接进项目详情 */
function openPreview(m) {
  if (suppressClick.value) return
  activeId.value = m.id
  const photo = projectPhotos[m.projectName]
  const list = photo && photo.full && photo.full.length ? photo.full : (m.thumb ? [m.thumb] : [])
  if (!list.length) {
    dialogRef.value?.openProject(m.p)
    return
  }
  preview.value = {
    list,
    index: 0,
    name: m.projectName,
    category: m.projectCategory,
    region: m.region,
    project: m.p
  }
}

function viewPreviewProject() {
  const proj = preview.value?.project
  preview.value = null
  if (proj) dialogRef.value?.openProject(proj)
}

onMounted(async () => {
  loading.value = true
  measure()
  window.addEventListener('resize', measure)
  try {
    const res = await projectApi.page({ pageNum: 1, pageSize: 500 })
    projects.value = res.data?.list || []
  } finally {
    loading.value = false
    requestAnimationFrame(measure)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onMove)
  window.removeEventListener('mouseup', onUp)
  window.removeEventListener('resize', measure)
})
</script>

<style scoped>
.vision {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 132px);
  min-height: 560px;
  gap: 12px;
}

/* 头部 */
.vision-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}
.vh-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.vh-mark {
  padding: 5px 9px;
  border: 1px solid rgba(166, 55, 44, 0.5);
  border-radius: 3px;
  color: #a6372c;
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 15px;
  letter-spacing: 0.16em;
  writing-mode: vertical-rl;
}
.vh-titles h2 {
  margin: 0;
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 21px;
  font-weight: 600;
  letter-spacing: 0.1em;
  color: var(--ink-1, #2b2620);
}
.vh-titles p {
  margin: 3px 0 0;
  font-size: 12px;
  letter-spacing: 0.14em;
  color: var(--ink-4, #8b7a63);
}
.vh-right {
  display: flex;
  align-items: baseline;
  gap: 20px;
  padding-bottom: 4px;
}
.vh-stat {
  display: flex;
  align-items: baseline;
  gap: 5px;
  text-align: left;
}
.vh-stat + .vh-stat {
  position: relative;
  padding-left: 20px;
}
.vh-stat + .vh-stat::before {
  content: '';
  position: absolute;
  left: 0;
  top: 3px;
  bottom: 2px;
  width: 1px;
  background: var(--line, #e2d7c3);
}
.vh-stat b {
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 22px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: 0.02em;
  color: var(--ink-1, #2b2620);
  font-variant-numeric: tabular-nums;
}
.vh-stat span {
  font-size: 10.5px;
  letter-spacing: 0.16em;
  color: var(--ink-4, #8b7a63);
}

/* 舞台 */
.stage {
  position: relative;
  flex: 1;
  min-height: 0;
  border-radius: 10px;
  overflow: hidden;
  background:
    radial-gradient(120% 90% at 50% 0%, #1d3348 0%, transparent 60%),
    radial-gradient(120% 100% at 50% 10%, #213a52 0%, #14232f 45%, #0c151d 100%);
  box-shadow: inset 0 0 0 1px rgba(217, 164, 65, 0.18), 0 18px 40px rgba(20, 16, 12, 0.22);
  cursor: grab;
  user-select: none;
}
.stage.is-dragging {
  cursor: grabbing;
}
.stage-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(217, 164, 65, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(217, 164, 65, 0.07) 1px, transparent 1px);
  background-size: 46px 46px;
  mask-image: radial-gradient(circle at 50% 45%, #000 25%, transparent 78%);
  -webkit-mask-image: radial-gradient(circle at 50% 45%, #000 25%, transparent 78%);
}
.stage-halo {
  position: absolute;
  left: 50%;
  top: 52%;
  width: 62%;
  height: 56%;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, rgba(217, 164, 65, 0.16), transparent 68%);
  filter: blur(6px);
  pointer-events: none;
}

.plate {
  position: absolute;
  inset: 5% 4% 11%;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 244, 224, 0.06), rgba(6, 12, 18, 0.6));
  /* 斜视由 SVG 内的几何投影完成，这里只保留板体厚度与投影，避免 3D 栅格化发虚 */
  box-shadow:
    0 1px 0 rgba(255, 235, 200, 0.16),
    0 4px 0 -1px rgba(217, 164, 65, 0.3),
    0 10px 0 -1px #0b1721,
    0 13px 0 -1px rgba(217, 164, 65, 0.22),
    0 36px 56px -22px rgba(0, 0, 0, 0.78),
    0 0 0 1px rgba(217, 164, 65, 0.16),
    inset 0 1px 0 rgba(255, 235, 200, 0.1);
}
.plate-rim {
  position: absolute;
  inset: -1px;
  border-radius: 19px;
  border: 1px solid rgba(217, 164, 65, 0.22);
  pointer-events: none;
}
.map-svg {
  width: 100%;
  height: 100%;
  display: block;
  border-radius: 16px;
  overflow: hidden;
}

.prov {
  transition: fill 0.3s ease, opacity 0.3s ease;
  stroke-linejoin: round;
}
.prov.is-data {
  stroke: rgba(217, 164, 65, 0.72);
  filter: drop-shadow(0 0 6px rgba(217, 164, 65, 0.3));
}

/* 地级边界：比省界更淡更细，只做“省内划分”的底纹 */
.cities path {
  fill: none;
  stroke: rgba(240, 223, 190, 0.24);
  stroke-width: 0.7;
  stroke-linejoin: round;
}

.abbrs {
  opacity: 0;
  transition: opacity 0.35s ease;
  pointer-events: none;
}
.abbrs.is-on {
  opacity: 1;
}
.abbr-text {
  font-family: var(--font-display, 'Songti SC', serif);
  letter-spacing: 0;
}

.anchor-ring {
  transform-origin: center;
  animation: anchorPulse 2.4s ease-out infinite;
}
@keyframes anchorPulse {
  0% {
    opacity: 0.9;
  }
  70% {
    opacity: 0.1;
  }
  100% {
    opacity: 0.9;
  }
}

.marker {
  cursor: pointer;
}
.marker circle {
  transition: stroke 0.15s ease;
}
.marker:hover circle {
  stroke: #fff;
}
.marker.is-active circle {
  stroke: #f3d79a;
}

.place-text {
  fill: #f3d79a;
  font-family: var(--font-display, 'Songti SC', serif);
  letter-spacing: 0.12em;
  paint-order: stroke;
  stroke: rgba(10, 20, 28, 0.9);
  stroke-linejoin: round;
  pointer-events: none;
}
.place-count {
  fill: rgba(236, 222, 195, 0.72);
  font-family: var(--font-sans, sans-serif);
  letter-spacing: 0.06em;
  paint-order: stroke;
  stroke: rgba(10, 20, 28, 0.85);
  stroke-linejoin: round;
  pointer-events: none;
}

/* 控件 */
.zoom-panel {
  position: absolute;
  right: 16px;
  top: 16px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 6px;
  padding: 8px;
  border-radius: 8px;
  background: rgba(12, 22, 31, 0.78);
  border: 1px solid rgba(217, 164, 65, 0.24);
  backdrop-filter: blur(8px);
}
.zoom-panel button {
  width: 34px;
  height: 30px;
  border: 1px solid rgba(217, 164, 65, 0.28);
  border-radius: 5px;
  background: rgba(255, 244, 224, 0.06);
  color: #f0dfc0;
  font-size: 15px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease;
}
.zoom-panel button:hover {
  background: rgba(217, 164, 65, 0.2);
  border-color: rgba(217, 164, 65, 0.55);
}
.zoom-panel .zoom-reset {
  font-size: 11px;
  letter-spacing: 0.08em;
}
.zoom-readout {
  text-align: center;
  font-size: 11px;
  color: #d9a441;
  font-variant-numeric: tabular-nums;
}
.zoom-readout i {
  font-style: normal;
  font-size: 9px;
  opacity: 0.7;
}

.tier-bar {
  position: absolute;
  left: 50%;
  bottom: 14px;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(12, 22, 31, 0.72);
  border: 1px solid rgba(217, 164, 65, 0.2);
  backdrop-filter: blur(8px);
  font-size: 11px;
  letter-spacing: 0.1em;
  color: rgba(232, 219, 195, 0.45);
}
.tier-bar span {
  display: inline-flex;
  align-items: baseline;
  gap: 5px;
  transition: color 0.2s ease;
}
.tier-bar span i {
  font-style: normal;
  font-size: 9px;
  letter-spacing: 0.06em;
  color: rgba(217, 164, 65, 0.5);
  font-variant-numeric: tabular-nums;
}
.tier-bar span.is-on {
  color: #f3d79a;
}
.tier-bar span.is-on i {
  color: #d9a441;
}
.tier-bar em {
  width: 14px;
  height: 1px;
  background: rgba(217, 164, 65, 0.35);
}

/* 底部图例栏 */
.vision-foot {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 0 2px;
}
.foot-legend {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
}
.foot-label {
  flex: none;
  padding-right: 14px;
  border-right: 1px solid var(--line, #e2d7c3);
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 11.5px;
  letter-spacing: 0.18em;
  color: #8a6a3a;
}
.legend-items {
  display: flex;
  flex-wrap: wrap;
  gap: 3px 16px;
}
.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  line-height: 1.75;
  color: var(--ink-3, #5c4f3f);
  white-space: nowrap;
}
.legend-item i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex: none;
}
.legend-item b {
  font-weight: 600;
  color: var(--ink-1, #2b2620);
  font-variant-numeric: tabular-nums;
}
.foot-hint {
  flex: none;
  font-size: 10.5px;
  letter-spacing: 0.08em;
  color: var(--ink-4, #8b7a63);
  white-space: nowrap;
}

.map-loading {
  position: absolute;
  inset: 0;
  display: grid;
  place-content: center;
  gap: 10px;
  justify-items: center;
  background: rgba(8, 14, 20, 0.7);
  color: #d9a441;
  font-size: 12px;
  letter-spacing: 0.16em;
}
.map-loading span {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid rgba(217, 164, 65, 0.25);
  border-top-color: #d9a441;
  animation: spin 0.9s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 图片放大预览 */
.pv-mask {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: grid;
  place-items: center;
  padding: 32px;
  background: rgba(10, 16, 22, 0.78);
  backdrop-filter: blur(6px);
  animation: pvFade 0.18s ease;
}
.pv-card {
  margin: 0;
  max-width: min(920px, 92vw);
  overflow: hidden;
  border: 1px solid rgba(217, 164, 65, 0.4);
  border-radius: 6px;
  background: #0f1a24;
  box-shadow: 0 30px 70px rgba(0, 0, 0, 0.5);
}
.pv-card img {
  display: block;
  max-width: 100%;
  max-height: 68vh;
  object-fit: contain;
  background: #0a1219;
}
.pv-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 10px 0 4px;
  background: #0a1219;
}
.pv-dots button {
  width: 8px;
  height: 8px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: rgba(232, 219, 195, 0.28);
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}
.pv-dots button.on {
  background: var(--ochre, #d9a441);
  transform: scale(1.3);
}
.pv-card figcaption {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  border-top: 1px solid rgba(217, 164, 65, 0.22);
}
.pv-text {
  min-width: 0;
}
.pv-name {
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 15px;
  letter-spacing: 0.04em;
  color: #f3e3c2;
}
.pv-meta {
  margin-top: 4px;
  font-size: 11.5px;
  letter-spacing: 0.08em;
  color: rgba(232, 219, 195, 0.6);
}
.pv-card figcaption .el-button {
  margin-left: auto;
  flex: none;
}
@keyframes pvFade {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@media (max-width: 1100px) {
  .vh-right {
    display: none;
  }
  .foot-hint,
  .foot-label {
    display: none;
  }
  .legend-items {
    gap: 2px 12px;
  }
}
</style>
