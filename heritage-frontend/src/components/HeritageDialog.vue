<template>
  <div>
    <ProjectPreviewDialog
      v-model="projectVisible"
      :project="activeProject"
      @open-inheritor="openInheritor"
    />
    <InheritorDialog
      v-model="inheritorVisible"
      :inheritor-id="activeInheritorId"
      @open-project="openProject"
    />
  </div>
</template>

<script setup>
/**
 * 项目 / 传承人详情弹窗的统一入口
 * 页面里引入后用 ref 调用 openProject(row) 或 openInheritor(id) 即可，
 * 两个弹窗之间的跳转（项目 ↔ 传承人）在组件内部完成。
 */
import { ref } from 'vue'
import ProjectPreviewDialog from './ProjectPreviewDialog.vue'
import InheritorDialog from './InheritorDialog.vue'

const projectVisible = ref(false)
const inheritorVisible = ref(false)
const activeProject = ref(null)
const activeInheritorId = ref('')

function openProject(project) {
  if (!project) return
  activeProject.value = project
  inheritorVisible.value = false
  projectVisible.value = true
}

function openInheritor(id) {
  if (!id) return
  activeInheritorId.value = id
  projectVisible.value = false
  inheritorVisible.value = true
}

defineExpose({ openProject, openInheritor })
</script>
