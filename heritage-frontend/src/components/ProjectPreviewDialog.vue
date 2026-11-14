<template>
  <el-dialog
    v-model="visible"
    width="900px"
    top="6vh"
    class="heritage-preview"
    append-to-body
    destroy-on-close
  >
    <template #header>
      <div v-if="project" class="pv-head">
        <span class="pv-seal">遗</span>
        <div class="pv-head-main">
          <div class="pv-eyebrow">
            {{ project.projectCategory }}
            <i>·</i>
            {{ project.projectLevel }}
            <i>·</i>
            {{ project.region }}
          </div>
          <h3 class="pv-title">{{ project.projectName }}</h3>
        </div>
      </div>
    </template>

    <div v-if="project" class="pv-body">
      <!-- 影像 -->
      <div class="pv-gallery" :style="{ '--n': images.length }">
        <el-image
          v-for="(img, i) in images"
          :key="img"
          class="pv-shot"
          :src="img"
          :preview-src-list="images"
          :initial-index="i"
          fit="cover"
          preview-teleported
          lazy
        >
          <template #error>
            <div class="pv-shot-fallback">
              <span class="pv-shot-mark">影</span>
              <em>影像待补</em>
            </div>
          </template>
          <template #placeholder>
            <div class="pv-shot-fallback is-loading"><em>影像载入中…</em></div>
          </template>
        </el-image>
      </div>
      <p class="pv-gallery-note">图片来自 Wikimedia Commons，按项目类别检索匹配；可在编辑中填写项目自有图片替换。</p>

      <!-- 事实栏 -->
      <div class="pv-facts">
        <div class="pv-fact">
          <span>非遗类别</span>
          <b>{{ project.projectCategory || '—' }}</b>
        </div>
        <div class="pv-fact">
          <span>保护级别</span>
          <b>{{ project.projectLevel || '—' }}</b>
        </div>
        <div class="pv-fact">
          <span>所属地区</span>
          <b>{{ project.region || '—' }}</b>
        </div>
        <div class="pv-fact">
          <span>传承人</span>
          <b>{{ (project.inheritorList || []).length }} 位</b>
        </div>
      </div>

      <!-- 简介 -->
      <section class="pv-section">
        <h4><i></i>项目简介</h4>
        <p class="pv-intro">{{ project.projectIntro || '暂无简介' }}</p>
      </section>

      <!-- 传承现状 -->
      <section v-if="project.inheritStatus" class="pv-section">
        <h4><i></i>传承现状</h4>
        <p class="pv-intro">{{ project.inheritStatus }}</p>
      </section>

      <!-- 传承人 -->
      <section v-if="(project.inheritorList || []).length" class="pv-section">
        <h4><i></i>代表性传承人</h4>
        <div class="pv-chips">
          <button
            v-for="p in project.inheritorList"
            :key="p.id"
            class="pv-chip is-person"
            type="button"
            @click="emit('open-inheritor', p.id)"
          >
            <span class="pv-chip-avatar">{{ (p.inheritorName || '?').slice(0, 1) }}</span>
            {{ p.inheritorName }}
            <em v-if="p.nation">{{ p.nation }}</em>
            <span class="pv-chip-go">›</span>
          </button>
        </div>
      </section>

      <!-- 民俗活动 -->
      <section v-if="(project.activityList || []).length" class="pv-section">
        <h4><i></i>关联民俗活动</h4>
        <div class="pv-chips">
          <span v-for="a in project.activityList" :key="a.id" class="pv-chip">
            {{ a.activityName }}
            <em v-if="a.activityAddr">{{ a.activityAddr }}</em>
          </span>
        </div>
      </section>

      <!-- 数据来源 -->
      <section v-if="project.source" class="pv-section">
        <h4><i></i>数据来源</h4>
        <div class="pv-source">
          <span class="pv-source-name">{{ project.source.sourceName }}</span>
          <el-link
            v-if="project.source.originalUrl"
            type="primary"
            :href="project.source.originalUrl"
            target="_blank"
          >
            {{ project.source.originalUrl }}
          </el-link>
        </div>
      </section>

      <!-- 检索标签 -->
      <section v-if="(project.searchTags || []).length" class="pv-section">
        <h4><i></i>检索标签</h4>
        <div class="pv-tags">
          <span v-for="t in project.searchTags" :key="t">#{{ t }}</span>
        </div>
      </section>

      <!-- 视频素材 -->
      <section v-if="videos.length" class="pv-section">
        <h4><i></i>影像资料</h4>
        <el-link v-for="v in videos" :key="v.mediaUrl" type="primary" :href="v.mediaUrl" target="_blank">
          {{ v.mediaDesc || v.mediaUrl }}
        </el-link>
      </section>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import categoryImages from '../assets/project-images.json'
import projectPhotos from '../assets/project-photos.json'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  project: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'open-inheritor'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

/** 已录入的真实图片（排除自动生成的示意图） */
const realImages = computed(() =>
  (props.project?.mediaList || [])
    .filter((m) => m.mediaType === '图片' && m.mediaUrl && !/text_to_image/i.test(m.mediaUrl))
    .map((m) => m.mediaUrl)
)

/** 项目专属配图（按项目名整理的真实照片，优先展示） */
const ownImages = computed(() => {
  const p = props.project
  if (!p) return []
  const photos = projectPhotos[p.projectName]
  return photos && photos.full ? photos.full : []
})

/** 配图优先级：项目专属照片 > 已录入真实图片 > 类别图库 */
const images = computed(() => {
  const p = props.project
  if (!p) return []
  if (ownImages.value.length) return ownImages.value.slice(0, 3)
  const matched = categoryImages[p.projectCategory] || []
  return [...realImages.value, ...matched].slice(0, 3)
})

const videos = computed(() =>
  (props.project?.mediaList || []).filter((m) => m.mediaType === '视频' && m.mediaUrl)
)
</script>

<style scoped>
.pv-head {
  display: flex;
  align-items: center;
  gap: 14px;
}
.pv-seal {
  flex: none;
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 4px;
  background: var(--cinnabar, #a6372c);
  color: #fbf7ee;
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 22px;
  letter-spacing: 0;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.25);
}
.pv-eyebrow {
  font-size: 12px;
  color: var(--ink-4, #8b7a63);
  letter-spacing: 0.14em;
}
.pv-eyebrow i {
  margin: 0 6px;
  font-style: normal;
  opacity: 0.5;
}
.pv-title {
  margin: 4px 0 0;
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 24px;
  font-weight: 600;
  color: var(--ink-1, #2b2620);
  letter-spacing: 0.05em;
}

.pv-body {
  max-height: 68vh;
  overflow-y: auto;
  padding-right: 4px;
}

.pv-gallery {
  display: grid;
  grid-template-columns: repeat(var(--n, 3), 1fr);
  gap: 8px;
}
.pv-shot {
  width: 100%;
  height: 158px;
  border-radius: 6px;
  overflow: hidden;
  background: #16283a;
  box-shadow: 0 6px 18px rgba(35, 28, 20, 0.14);
}
.pv-shot-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  gap: 6px;
  background: linear-gradient(160deg, #22405a, #16283a);
  color: rgba(240, 226, 200, 0.6);
  font-size: 12px;
}
.pv-shot-fallback em {
  font-style: normal;
  letter-spacing: 0.18em;
}
.pv-shot-mark {
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 26px;
  color: rgba(217, 164, 65, 0.55);
}
.pv-gallery-note {
  margin: 8px 0 0;
  font-size: 11.5px;
  color: var(--ink-4, #8b7a63);
  letter-spacing: 0.02em;
}

.pv-facts {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  margin: 16px 0 4px;
  border: 1px solid var(--line, #e2d7c3);
  border-radius: 6px;
  overflow: hidden;
  background: var(--paper-2, #fbf7ee);
}
.pv-fact {
  padding: 10px 14px;
  border-right: 1px solid var(--line, #e2d7c3);
}
.pv-fact:last-child {
  border-right: none;
}
.pv-fact span {
  display: block;
  font-size: 11.5px;
  color: var(--ink-4, #8b7a63);
  letter-spacing: 0.1em;
}
.pv-fact b {
  display: block;
  margin-top: 3px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-1, #2b2620);
}

.pv-section {
  margin-top: 20px;
}
.pv-section h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 8px;
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-1, #2b2620);
  letter-spacing: 0.12em;
}
.pv-section h4 i {
  width: 3px;
  height: 13px;
  border-radius: 2px;
  background: var(--cinnabar, #a6372c);
}
.pv-intro {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.95;
  color: var(--ink-2, #4a4136);
  text-align: justify;
}

.pv-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.pv-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid var(--line, #e2d7c3);
  border-radius: 999px;
  background: var(--paper-2, #fbf7ee);
  font-size: 12.5px;
  color: var(--ink-2, #4a4136);
}
.pv-chip em {
  font-style: normal;
  font-size: 11px;
  color: var(--ink-4, #8b7a63);
}
.pv-chip.is-person {
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}
.pv-chip.is-person:hover {
  border-color: var(--ochre, #b4713a);
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(180, 113, 58, 0.16);
}
.pv-chip-avatar {
  width: 20px;
  height: 20px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--ochre, #b4713a);
  color: #fbf7ee;
  font-size: 11px;
}
.pv-chip-go {
  color: var(--ink-4, #8b7a63);
  font-size: 14px;
  line-height: 1;
}

.pv-source {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-size: 12.5px;
}
.pv-source-name {
  padding: 4px 10px;
  border-radius: 4px;
  background: rgba(79, 122, 94, 0.1);
  color: var(--malachite, #4f7a5e);
}

.pv-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.pv-tags span {
  padding: 3px 9px;
  border-radius: 4px;
  background: rgba(30, 58, 85, 0.07);
  color: var(--lapis, #1e3a55);
  font-size: 11.5px;
}
</style>
