<template>
  <div class="stat-page">
    <!-- 数据概览 -->
    <div class="stat-cards">
      <div
        v-for="(card, index) in cards"
        :key="card.label"
        class="stat-card"
        :class="`tone-${card.tone}`"
        :style="{ animationDelay: `${index * 70}ms` }"
      >
        <div class="stat-top">
          <span class="stat-label">{{ card.label }}</span>
          <span class="stat-en">{{ card.en }}</span>
        </div>
        <div class="stat-value">
          {{ card.value }}<span class="stat-unit">{{ card.unit }}</span>
        </div>
        <div class="stat-foot">
          <span class="stat-rule"></span>
          <span>{{ card.note }}</span>
        </div>
      </div>
    </div>

    <!-- 图表 -->
    <div class="chart-grid">
      <el-card class="chart-card" shadow="never" v-loading="loading">
        <template #header>
          <div class="chart-head">
            <span class="chart-title">各地区非遗数量分布</span>
            <span class="chart-en">REGIONAL DISTRIBUTION</span>
          </div>
        </template>
        <div ref="barRef" class="chart"></div>
      </el-card>

      <el-card class="chart-card" shadow="never" v-loading="loading">
        <template #header>
          <div class="chart-head">
            <span class="chart-title">级别构成</span>
            <span class="chart-en">LEVEL COMPOSITION</span>
          </div>
        </template>
        <div ref="pieRef" class="chart"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { projectApi } from '../../api'

const loading = ref(false)
const barRef = ref()
const pieRef = ref()

const stat = reactive({ total: 0, regionStat: [], levelStat: [] })

let barChart = null
let pieChart = null

const SERIF = "'Songti SC', 'Source Han Serif SC', 'STSong', 'SimSun', serif"
const INK = '#241a12'
const INK_3 = '#7d6a55'
const LINE = '#e6d9c1'

function levelCount(level) {
  const item = stat.levelStat.find((it) => it.name === level)
  return item ? item.value : 0
}

const cards = computed(() => [
  { label: '非遗项目总数', en: 'TOTAL PROJECTS', value: stat.total, unit: '项', note: '已收录于档案库', tone: 'lapis' },
  { label: '覆盖地区数', en: 'REGIONS', value: stat.regionStat.length, unit: '个', note: '青藏高原及周边地区', tone: 'malachite' },
  { label: '国家级项目', en: 'NATIONAL LEVEL', value: levelCount('国家级'), unit: '项', note: '国家级名录收录', tone: 'red' },
  { label: '省级项目', en: 'PROVINCIAL LEVEL', value: levelCount('省级'), unit: '项', note: '省级名录收录', tone: 'ochre' }
])

const tooltipStyle = {
  backgroundColor: '#fbf7ee',
  borderColor: '#d3bf9f',
  borderWidth: 1,
  padding: [10, 14],
  textStyle: { color: '#4a3a2a', fontSize: 12 },
  extraCssText: 'box-shadow: 0 10px 26px rgba(64,45,28,.14); border-radius: 2px;'
}

/** 渲染柱状图：各地区非遗数量 */
function renderBar() {
  barChart = echarts.init(barRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis', ...tooltipStyle, axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(180,113,58,0.08)' } } },
    grid: { left: 44, right: 24, top: 34, bottom: 52 },
    xAxis: {
      type: 'category',
      data: stat.regionStat.map((it) => it.name),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: LINE } },
      axisLabel: { interval: 0, rotate: 32, color: INK_3, fontSize: 11, margin: 12 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: INK_3, fontSize: 11 },
      splitLine: { lineStyle: { color: LINE, type: [4, 4] } }
    },
    series: [
      {
        name: '非遗数量',
        type: 'bar',
        barWidth: '42%',
        showBackground: true,
        backgroundStyle: { color: '#f4ebda' },
        itemStyle: {
          borderRadius: [2, 2, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#b4713a' },
            { offset: 1, color: '#dfb877' }
          ])
        },
        label: { show: true, position: 'top', color: INK_3, fontSize: 11 },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#a6372c' },
              { offset: 1, color: '#d9a441' }
            ])
          }
        },
        data: stat.regionStat.map((it) => it.value)
      }
    ]
  })
}

/** 渲染饼图：国家级 / 省级占比 */
function renderPie() {
  pieChart = echarts.init(pieRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>{c} 项（{d}%）', ...tooltipStyle },
    legend: {
      bottom: 8,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      itemGap: 22,
      textStyle: { color: INK_3, fontSize: 12 }
    },
    title: {
      text: String(stat.total),
      subtext: '项目总数',
      left: '50%',
      top: '36%',
      textAlign: 'center',
      textStyle: { fontFamily: SERIF, fontSize: 32, fontWeight: 600, color: INK },
      subtextStyle: { color: '#a3927d', fontSize: 11, letterSpacing: 4 }
    },
    color: ['#a6372c', '#d9a441', '#1e3a55', '#4f7a5e'],
    series: [
      {
        name: '非遗级别',
        type: 'pie',
        radius: ['52%', '70%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fbf7ee', borderWidth: 2 },
        label: { formatter: '{b} {d}%', color: INK_3, fontSize: 11 },
        labelLine: { lineStyle: { color: '#d3bf9f' }, length: 10, length2: 10 },
        emphasis: {
          scaleSize: 6,
          label: { color: INK, fontWeight: 600 }
        },
        data: stat.levelStat.map((it) => ({ name: it.name, value: it.value }))
      }
    ]
  })
}

/** 调用后端 /api/project/stat（MongoDB Aggregation 聚合结果） */
async function loadStat() {
  loading.value = true
  try {
    const res = await projectApi.stat()
    stat.total = res.data.total
    stat.regionStat = res.data.regionStat || []
    stat.levelStat = res.data.levelStat || []
    renderBar()
    renderPie()
  } finally {
    loading.value = false
  }
}

function handleResize() {
  barChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  loadStat()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.stat-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ============================ 数据概览 ============================ */

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  padding: 18px 20px 16px;
  background: var(--paper-2);
  border: 1px solid var(--line);
  border-radius: 3px;
  box-shadow: var(--shadow-card);
  animation: rise-in 0.5s cubic-bezier(0.2, 0.7, 0.3, 1) both;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.stat-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--card-tone, var(--ochre));
}

.stat-card::after {
  content: '';
  position: absolute;
  right: -28px;
  bottom: -28px;
  width: 92px;
  height: 92px;
  border: 1px solid rgba(180, 145, 96, 0.18);
  transform: rotate(45deg);
  pointer-events: none;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lift);
}

.tone-lapis {
  --card-tone: var(--lapis);
}

.tone-malachite {
  --card-tone: var(--malachite);
}

.tone-red {
  --card-tone: var(--tibet-red);
}

.tone-ochre {
  --card-tone: var(--ochre);
}

.stat-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.stat-label {
  font-size: 12.5px;
  letter-spacing: 0.1em;
  color: var(--ink-2);
}

.stat-en {
  font-size: 8px;
  letter-spacing: 0.18em;
  color: var(--ink-4);
  white-space: nowrap;
}

.stat-value {
  margin-top: 10px;
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: 0.04em;
  color: var(--ink);
  font-variant-numeric: tabular-nums;
}

.stat-unit {
  margin-left: 6px;
  font-family: var(--font-body);
  font-size: 12px;
  font-weight: 400;
  letter-spacing: 0.08em;
  color: var(--ink-4);
}

.stat-foot {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  font-size: 11px;
  letter-spacing: 0.04em;
  color: var(--ink-4);
}

.stat-rule {
  width: 16px;
  height: 1px;
  background: var(--card-tone, var(--ochre));
  opacity: 0.7;
}

/* ============================ 图表 ============================ */

.chart-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(0, 1fr);
  gap: 16px;
}

.chart-card :deep(.el-card__header) {
  padding: 14px 20px 12px;
}

.chart-card :deep(.el-card__body) {
  padding: 8px 10px 10px;
}

.chart-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.chart-title {
  font-family: var(--font-display);
  font-size: 15px;
  letter-spacing: 0.1em;
  color: var(--ink);
}

.chart-en {
  font-size: 8.5px;
  letter-spacing: 0.2em;
  color: var(--ink-4);
}

.chart {
  width: 100%;
  height: 372px;
}

@media (max-width: 1440px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
