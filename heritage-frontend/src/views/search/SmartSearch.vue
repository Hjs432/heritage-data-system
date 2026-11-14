<template>
  <div class="smart-search">
    <!-- 检索区 -->
    <section class="search-hero">
      <span class="hero-mark">遗</span>

      <div class="hero-head">
        <div class="hero-eyebrow">INTENT SEARCH · 语义检索</div>
        <h1 class="hero-title">用一句话，找到一项非遗</h1>
        <p class="hero-desc">
          不必记得专有名词，描述你看到的场景就好 —— 「一群人围圈跳舞」「过节吃什么东西」。
        </p>
      </div>

      <div class="search-bar">
        <el-input
          v-model="query"
          class="query-input"
          size="large"
          clearable
          placeholder="如：一群人跳舞 / 吃什么东西 / 戴面具跳神 / 骑马比赛"
          @keyup.enter="doSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button class="search-btn" type="primary" size="large" :loading="loading" :icon="MagicStick" @click="doSearch">
          智能匹配
        </el-button>
        <el-select v-model="topK" class="topk" size="large" @change="doSearch">
          <el-option :value="5" label="前 5 条" />
          <el-option :value="10" label="前 10 条" />
          <el-option :value="20" label="前 20 条" />
        </el-select>
      </div>

      <div class="chips">
        <span class="chips-label">猜你想找</span>
        <button
          v-for="chip in CHIPS"
          :key="chip"
          type="button"
          class="chip"
          :class="{ active: query === chip }"
          @click="pickChip(chip)"
        >
          {{ chip }}
        </button>
      </div>
    </section>

    <!-- 意图归一化结果 -->
    <section v-if="intent" class="intent-card">
      <div class="intent-head">
        <span class="intent-mark"></span>
        <span class="intent-title">意图解析</span>
        <span class="intent-summary">{{ intent.summary }}</span>
      </div>
      <div class="intent-body">
        <div v-if="intent.keywords?.length" class="intent-line">
          <span class="intent-label">关键词</span>
          <el-tag v-for="k in intent.keywords" :key="k" size="small" effect="plain">{{ k }}</el-tag>
        </div>
        <div v-if="intent.categories?.length" class="intent-line">
          <span class="intent-label">推断类别</span>
          <el-tag v-for="c in intent.categories" :key="c" size="small" type="warning" effect="plain">{{ c }}</el-tag>
        </div>
        <div v-if="intent.modifiers?.length" class="intent-line">
          <span class="intent-label">形态修饰</span>
          <el-tag v-for="m in intent.modifiers" :key="m" size="small" type="danger" effect="plain">{{ m }}</el-tag>
        </div>
        <div v-if="intent.tags?.length" class="intent-line">
          <span class="intent-label">扩展标签</span>
          <el-tag v-for="t in intent.tags" :key="t" size="small" type="success" effect="plain">{{ t }}</el-tag>
        </div>
      </div>
    </section>

    <!-- 结果区 -->
    <section class="result-panel">
      <div class="result-head">
        <div class="result-head-left">
          <h2>匹配结果</h2>
          <span class="result-sub">MATCHED PROJECTS</span>
        </div>
        <span v-if="searched" class="result-count">共 {{ results.length }} 条候选</span>
      </div>

      <el-skeleton v-if="loading" :rows="6" animated class="skeleton" />

      <el-empty v-else-if="!searched" :image-size="88" description="输入一句口语描述，或用上面的标签快速体验" />

      <el-empty v-else-if="results.length === 0" :image-size="88" description="没有找到相关非遗项目，换个说法试试" />

      <div v-else class="result-list">
        <article
          v-for="(item, index) in results"
          :key="item.project.id"
          class="result-item"
          :style="{ animationDelay: `${index * 45}ms` }"
        >
          <div class="rank" :class="{ 'rank-top': index === 0 }">
            <span class="rank-num">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="rank-label">NO.</span>
          </div>

          <div class="result-body">
            <div class="title-row">
              <span class="project-name" @click="openDetail(item.project)">{{ item.project.projectName }}</span>
              <el-tag size="small" :type="item.project.projectLevel === '国家级' ? 'danger' : 'warning'">
                {{ item.project.projectLevel }}
              </el-tag>
              <el-tag size="small" effect="plain" type="info">{{ item.project.projectCategory }}</el-tag>
              <span class="region">{{ item.project.region }}</span>
            </div>

            <div class="intro">{{ item.project.projectIntro || '暂无简介' }}</div>

            <!-- 匹配分 / 权重 -->
            <div class="metrics">
              <div class="metric">
                <span class="metric-label">匹配分</span>
                <el-progress
                  :percentage="relativePercent(item.score)"
                  :stroke-width="6"
                  :show-text="false"
                  color="#a6372c"
                  class="metric-bar"
                />
                <span class="metric-value">{{ item.score.toFixed(3) }}</span>
              </div>
              <div class="metric">
                <span class="metric-label">权重</span>
                <el-progress
                  :percentage="weightPercent(item.weight)"
                  :stroke-width="6"
                  :show-text="false"
                  color="#b4713a"
                  class="metric-bar"
                />
                <span class="metric-value">{{ weightPercent(item.weight) }}%</span>
              </div>
            </div>

            <!-- 命中标签 -->
            <div v-if="item.matchedTags?.length" class="tag-row">
              <span v-for="tag in item.matchedTags" :key="tag" class="hit-tag"># {{ tag }}</span>
            </div>

            <!-- 匹配理由 -->
            <div v-if="item.reasons?.length" class="reason-row">
              <span v-for="reason in item.reasons" :key="reason" class="reason">{{ reason }}</span>
            </div>

            <div class="actions">
              <el-button link type="primary" @click="openDetail(item.project)">查看详情 →</el-button>
            </div>
          </div>
        </article>
      </div>
    </section>

    <!-- 项目 / 传承人详情弹窗 -->
    <HeritageDialog ref="detailDialogRef" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Search } from '@element-plus/icons-vue'
import { projectApi } from '../../api'
import HeritageDialog from '../../components/HeritageDialog.vue'

/** 快捷口语查询：点一下就能看到效果 */
const CHIPS = ['一群人跳舞', '两个人跳舞', '穿铠甲跳舞', '吃什么东西', '戴面具跳神', '唱歌弹琴', '骑马比赛', '过节热闹', '怎么治病', '做手工']

const query = ref('')
const topK = ref(10)
const loading = ref(false)
const searched = ref(false)
const results = ref([])
const intent = ref(null)

const detailDialogRef = ref()

/** 最高分，用于把匹配分换算成相对进度条长度 */
const bestScore = computed(() => (results.value.length ? results.value[0].score : 0))

function relativePercent(score) {
  if (!bestScore.value) return 0
  return Math.round((score / bestScore.value) * 100)
}

function weightPercent(weight) {
  return Math.round((weight || 0) * 1000) / 10
}

function pickChip(chip) {
  query.value = chip
  doSearch()
}

async function doSearch() {
  const text = query.value.trim()
  if (!text) {
    ElMessage.warning('请先输入一句描述')
    return
  }
  loading.value = true
  try {
    const [searchRes, intentRes] = await Promise.all([
      projectApi.search({ query: text, topK: topK.value, minScore: 0.15 }),
      projectApi.searchIntent({ query: text })
    ])
    results.value = searchRes.data || []
    intent.value = intentRes.data
    searched.value = true
  } finally {
    loading.value = false
  }
}

/** 详情直接用检索结果里已拼装好的 VO，避免再次请求 */
function openDetail(project) {
  detailDialogRef.value?.openProject(project)
}
</script>

<style scoped>
.smart-search {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ============================ 检索 Hero ============================ */

.search-hero {
  position: relative;
  overflow: hidden;
  padding: 30px 32px 26px;
  border: 1px solid rgba(217, 164, 65, 0.28);
  border-radius: 3px;
  background-color: var(--lapis-deep);
  background-image:
    radial-gradient(circle at 8% -10%, rgba(217, 164, 65, 0.2), transparent 44%),
    radial-gradient(circle at 96% 110%, rgba(166, 55, 44, 0.26), transparent 46%),
    repeating-linear-gradient(0deg, rgba(255, 255, 255, 0.022) 0 1px, transparent 1px 24px);
  box-shadow: 0 16px 36px rgba(30, 40, 52, 0.18);
}

.search-hero::before {
  content: '';
  position: absolute;
  inset: 14px;
  border: 1px solid rgba(217, 164, 65, 0.16);
  pointer-events: none;
}

.hero-mark {
  position: absolute;
  right: 34px;
  bottom: -74px;
  font-family: var(--font-display);
  font-size: 250px;
  line-height: 1;
  color: rgba(217, 164, 65, 0.055);
  pointer-events: none;
  user-select: none;
}

.hero-head {
  position: relative;
  margin-bottom: 22px;
}

.hero-eyebrow {
  font-size: 9.5px;
  letter-spacing: 0.34em;
  color: rgba(217, 164, 65, 0.72);
  margin-bottom: 12px;
}

.hero-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: #f5e8cf;
  text-shadow: 0 2px 18px rgba(0, 0, 0, 0.22);
}

.hero-desc {
  margin: 10px 0 0;
  font-size: 12.5px;
  letter-spacing: 0.05em;
  color: rgba(233, 220, 196, 0.66);
}

.search-bar {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
}

.query-input :deep(.el-input__wrapper) {
  height: 48px;
  padding: 0 16px;
  background: #fdfaf2;
  box-shadow: 0 0 0 1px rgba(217, 164, 65, 0.4) inset;
}

.query-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--butter) inset, 0 0 0 3px rgba(217, 164, 65, 0.18);
}

.query-input :deep(.el-input__inner) {
  font-size: 14px;
  letter-spacing: 0.03em;
}

.query-input :deep(.el-input__prefix) {
  color: var(--ochre);
}

.search-btn {
  height: 48px;
  padding: 0 22px;
  font-family: var(--font-display);
  font-size: 14px;
  letter-spacing: 0.16em;
  flex-shrink: 0;
}

.topk {
  width: 118px;
  flex-shrink: 0;
}

.topk :deep(.el-select__wrapper) {
  height: 48px;
  background: #f7f0e1;
  box-shadow: 0 0 0 1px rgba(217, 164, 65, 0.4) inset;
}

.chips {
  position: relative;
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.chips-label {
  margin-right: 4px;
  font-size: 11px;
  letter-spacing: 0.16em;
  color: rgba(217, 164, 65, 0.62);
}

.chip {
  padding: 6px 13px;
  font-family: var(--font-body);
  font-size: 12px;
  letter-spacing: 0.04em;
  color: #d8c7a9;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(217, 164, 65, 0.3);
  border-radius: 1px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip:hover {
  color: #f6e9cf;
  background: rgba(217, 164, 65, 0.14);
  border-color: rgba(217, 164, 65, 0.6);
}

.chip.active {
  color: #2b2119;
  background: var(--butter);
  border-color: var(--butter);
}

/* ============================ 意图解析 ============================ */

.intent-card {
  padding: 16px 22px 18px;
  background: var(--paper-2);
  border: 1px solid var(--line);
  border-radius: 3px;
  box-shadow: var(--shadow-card);
}

.intent-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.intent-mark {
  width: 3px;
  height: 16px;
  background: var(--tibet-red);
  border-radius: 1px;
}

.intent-title {
  font-family: var(--font-display);
  font-size: 14px;
  letter-spacing: 0.14em;
  color: var(--ink);
}

.intent-summary {
  font-size: 12px;
  letter-spacing: 0.03em;
  color: var(--ink-3);
}

.intent-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.intent-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.intent-label {
  min-width: 62px;
  font-size: 11px;
  letter-spacing: 0.1em;
  color: var(--ink-4);
}

/* ============================ 结果区 ============================ */

.result-panel {
  padding: 20px 22px 22px;
  background: var(--paper-2);
  border: 1px solid var(--line);
  border-radius: 3px;
  box-shadow: var(--shadow-card);
}

.result-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding-bottom: 12px;
  margin-bottom: 14px;
  border-bottom: 1px solid var(--line);
}

.result-head-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.result-head h2 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.14em;
  color: var(--ink);
}

.result-sub {
  font-size: 8.5px;
  letter-spacing: 0.24em;
  color: var(--ink-4);
}

.result-count {
  padding: 3px 10px;
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--ink-3);
  background: #f1e7d5;
  border: 1px solid var(--line);
}

.skeleton {
  padding: 16px 0;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  position: relative;
  display: flex;
  gap: 16px;
  padding: 16px 18px;
  background: var(--paper-3);
  border: 1px solid var(--line);
  border-radius: 2px;
  animation: rise-in 0.45s cubic-bezier(0.2, 0.7, 0.3, 1) both;
  transition: border-color 0.24s ease, box-shadow 0.24s ease, transform 0.24s ease;
}

.result-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 2px;
  background: transparent;
  transition: background 0.24s ease;
}

.result-item:hover {
  border-color: #d5bf9b;
  box-shadow: var(--shadow-lift);
  transform: translateY(-1px);
}

.result-item:hover::before {
  background: var(--ochre);
}

.rank {
  flex: 0 0 46px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 2px;
  border-right: 1px solid var(--line);
}

.rank-num {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 600;
  line-height: 1.1;
  color: var(--ink-4);
  font-variant-numeric: tabular-nums;
}

.rank-label {
  margin-top: 4px;
  font-size: 8px;
  letter-spacing: 0.16em;
  color: var(--ink-4);
}

.rank-top .rank-num {
  color: var(--tibet-red);
}

.rank-top .rank-label {
  color: var(--ochre);
}

.result-body {
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.project-name {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--ink);
  cursor: pointer;
  transition: color 0.2s ease;
}

.project-name:hover {
  color: var(--tibet-red);
}

.region {
  margin-left: auto;
  font-size: 11.5px;
  letter-spacing: 0.06em;
  color: var(--ink-4);
}

.intro {
  margin-top: 8px;
  font-size: 12.5px;
  line-height: 1.75;
  color: var(--ink-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 26px;
  margin-top: 12px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 8px;
}

.metric-label {
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--ink-4);
}

.metric-bar {
  width: 140px;
}

.metric-bar :deep(.el-progress-bar__outer) {
  background-color: #ece1cc;
  border-radius: 0;
}

.metric-bar :deep(.el-progress-bar__inner) {
  border-radius: 0;
}

.metric-value {
  font-size: 11.5px;
  color: var(--ink-3);
  font-variant-numeric: tabular-nums;
}

.tag-row {
  margin-top: 11px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.hit-tag {
  padding: 2px 8px;
  font-size: 11px;
  letter-spacing: 0.03em;
  color: #6b5433;
  background: #f7edd9;
  border: 1px solid #e5d3b2;
}

.reason-row {
  margin-top: 9px;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.reason {
  font-size: 11.5px;
  color: #587a60;
}

.reason::before {
  content: '✓ ';
  color: var(--ochre);
}

.actions {
  margin-top: 4px;
  text-align: right;
}

.actions :deep(.el-button.is-link) {
  font-size: 12px;
  letter-spacing: 0.06em;
}

@media (max-width: 1280px) {
  .region {
    margin-left: 0;
  }

  .search-hero {
    padding: 24px 22px 22px;
  }

  .hero-title {
    font-size: 25px;
  }
}
</style>
