<template>
  <el-dialog
    v-model="visible"
    width="760px"
    top="8vh"
    class="inheritor-dialog"
    append-to-body
    destroy-on-close
  >
    <template #header>
      <div class="ih-head">
        <div class="ih-avatar">{{ (detail?.inheritorName || '遗').slice(0, 1) }}</div>
        <div class="ih-head-main">
          <div class="ih-eyebrow">REPRESENTATIVE INHERITOR · 代表性传承人</div>
          <h3 class="ih-title">{{ detail?.inheritorName || '加载中…' }}</h3>
          <div class="ih-tags">
            <span v-if="detail?.gender">{{ detail.gender }}</span>
            <span v-if="detail?.nation">{{ detail.nation }}族</span>
            <span v-if="detail">关联项目 {{ projects.length }} 项</span>
          </div>
        </div>
      </div>
    </template>

    <div v-loading="loading" class="ih-body">
      <section class="ih-section">
        <h4><i></i>传承人简介</h4>
        <p class="ih-intro">{{ detail?.intro || '暂无简介' }}</p>
      </section>

      <section class="ih-section">
        <h4><i></i>与之相关的非遗项目</h4>

        <p v-if="!loading && !projects.length" class="ih-empty">暂未关联非遗项目</p>

        <ul class="ih-projects">
          <li
            v-for="p in projects"
            :key="p.id"
            class="ih-project"
            @click="emit('open-project', p)"
          >
            <span class="ih-project-dot" :style="{ background: categoryColor(p.projectCategory) }"></span>
            <div class="ih-project-main">
              <div class="ih-project-title">
                {{ p.projectName }}
                <em>{{ p.projectLevel }}</em>
              </div>
              <div class="ih-project-meta">{{ p.projectCategory }} · {{ p.region }}</div>
              <p class="ih-project-intro">{{ p.projectIntro }}</p>
            </div>
            <span class="ih-project-go">›</span>
          </li>
        </ul>
      </section>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { inheritorApi } from '../api'
import { categoryColor } from '../utils/heritage'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  inheritorId: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'open-project'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const loading = ref(false)
const detail = ref(null)
const projects = ref([])

async function load(id) {
  if (!id) return
  loading.value = true
  detail.value = null
  projects.value = []
  try {
    const [d, list] = await Promise.all([inheritorApi.detail(id), inheritorApi.projects(id)])
    detail.value = d.data
    projects.value = list.data || []
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.modelValue, props.inheritorId],
  ([open, id]) => {
    if (open && id) load(id)
  },
  { immediate: true }
)
</script>

<style scoped>
.ih-head {
  display: flex;
  align-items: center;
  gap: 14px;
}
.ih-avatar {
  flex: none;
  width: 50px;
  height: 50px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 30%, #c98a4f, #b4713a 60%, #8d5427);
  color: #fbf7ee;
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 22px;
  box-shadow: 0 6px 16px rgba(180, 113, 58, 0.28);
}
.ih-eyebrow {
  font-size: 11px;
  letter-spacing: 0.16em;
  color: var(--ink-4, #8b7a63);
}
.ih-title {
  margin: 4px 0 0;
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 23px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--ink-1, #2b2620);
}
.ih-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 7px;
}
.ih-tags span {
  padding: 2px 9px;
  border-radius: 999px;
  background: rgba(30, 58, 85, 0.07);
  color: var(--lapis, #1e3a55);
  font-size: 11.5px;
}

.ih-body {
  max-height: 64vh;
  overflow-y: auto;
  padding-right: 4px;
}
.ih-section {
  margin-top: 16px;
}
.ih-section h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px;
  font-family: var(--font-display, 'Songti SC', serif);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: var(--ink-1, #2b2620);
}
.ih-section h4 i {
  width: 3px;
  height: 13px;
  border-radius: 2px;
  background: var(--cinnabar, #a6372c);
}
.ih-intro {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.95;
  color: var(--ink-2, #4a4136);
  text-align: justify;
}

.ih-empty {
  margin: 0;
  padding: 22px;
  text-align: center;
  font-size: 12.5px;
  color: var(--ink-4, #8b7a63);
  background: var(--paper-2, #fbf7ee);
  border: 1px dashed var(--line, #e2d7c3);
  border-radius: 6px;
}

.ih-projects {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 8px;
}
.ih-project {
  display: flex;
  gap: 11px;
  align-items: flex-start;
  padding: 12px 14px;
  border: 1px solid var(--line, #e2d7c3);
  border-radius: 6px;
  background: var(--paper-2, #fbf7ee);
  cursor: pointer;
  transition: border-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}
.ih-project:hover {
  border-color: var(--ochre, #b4713a);
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(120, 92, 60, 0.1);
}
.ih-project-dot {
  flex: none;
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
}
.ih-project-main {
  min-width: 0;
  flex: 1;
}
.ih-project-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-1, #2b2620);
}
.ih-project-title em {
  margin-left: 8px;
  padding: 1px 7px;
  border-radius: 3px;
  background: rgba(217, 164, 65, 0.16);
  color: #8a6420;
  font-style: normal;
  font-size: 11px;
  font-weight: 500;
}
.ih-project-meta {
  margin-top: 3px;
  font-size: 11.5px;
  color: var(--ink-4, #8b7a63);
  letter-spacing: 0.04em;
}
.ih-project-intro {
  margin: 6px 0 0;
  font-size: 12.5px;
  line-height: 1.75;
  color: var(--ink-3, #6b5f4e);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.ih-project-go {
  align-self: center;
  color: var(--ink-4, #8b7a63);
  font-size: 18px;
}
</style>
