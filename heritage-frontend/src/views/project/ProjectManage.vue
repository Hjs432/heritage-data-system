<template>
  <div>
    <!-- 搜索栏 -->
    <el-card shadow="never">
      <template #header>
        <div class="archive-head">
          <span class="archive-title">项目档案</span>
          <span class="archive-sub">PROJECT ARCHIVE</span>
          <span class="archive-count">共 {{ total }} 条</span>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="项目名称">
          <el-input v-model="query.projectName" placeholder="请输入项目名称" clearable style="width: 180px"
                    @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="非遗类别">
          <el-select v-model="query.projectCategory" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="c in CATEGORY_OPTIONS" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属地区">
          <el-select v-model="query.region" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="r in REGION_OPTIONS" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <!-- 仅 admin 可见的写操作按钮 -->
          <el-button v-if="userStore.isAdmin" type="success" :icon="Plus" @click="openForm()">新增项目</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="projectName" label="项目名称" min-width="140">
          <template #default="{ row }">
            <button type="button" class="name-link" @click="openDetail(row)">{{ row.projectName }}</button>
          </template>
        </el-table-column>
        <el-table-column prop="projectCategory" label="类别" width="110" />
        <el-table-column prop="projectLevel" label="级别" width="90">
          <template #default="{ row }">
            <el-tag :type="row.projectLevel === '国家级' ? 'danger' : 'warning'" size="small">
              {{ row.projectLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="region" label="所属地区" width="120" />
        <el-table-column label="关联传承人" min-width="160">
          <template #default="{ row }">
            <button
              v-for="item in row.inheritorList"
              :key="item.id"
              type="button"
              class="person-link"
              @click="openInheritor(item.id)"
            >
              {{ item.inheritorName }}
            </button>
            <span v-if="!row.inheritorList || row.inheritorList.length === 0" class="empty">-</span>
          </template>
        </el-table-column>
        <el-table-column label="关联民俗活动" min-width="160">
          <template #default="{ row }">
            <el-tag v-for="item in row.activityList" :key="item.id" size="small" type="success" style="margin: 2px">
              {{ item.activityName }}
            </el-tag>
            <span v-if="!row.activityList || row.activityList.length === 0" class="empty">-</span>
          </template>
        </el-table-column>
        <el-table-column label="数据来源" min-width="150">
          <template #default="{ row }">
            {{ row.source ? row.source.sourceName : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="多媒体" width="90">
          <template #default="{ row }">{{ row.mediaList ? row.mediaList.length : 0 }} 条</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <template v-if="userStore.isAdmin">
              <el-button link type="primary" @click="openForm(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 14px; justify-content: flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑非遗项目' : '新增非遗项目'" width="780px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="如：藏族锅庄舞" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="非遗类别" prop="projectCategory">
              <el-select v-model="form.projectCategory" placeholder="请选择" style="width: 100%">
                <el-option v-for="c in CATEGORY_OPTIONS" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="级别" prop="projectLevel">
              <el-select v-model="form.projectLevel" placeholder="请选择" style="width: 100%">
                <el-option label="国家级" value="国家级" />
                <el-option label="省级" value="省级" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属地区" prop="region">
              <el-select v-model="form.region" placeholder="请选择" filterable allow-create style="width: 100%">
                <el-option v-for="r in REGION_OPTIONS" :key="r" :label="r" :value="r" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="项目简介">
          <el-input v-model="form.projectIntro" type="textarea" :rows="2" placeholder="项目简介" />
        </el-form-item>
        <el-form-item label="传承现状">
          <el-input v-model="form.inheritStatus" placeholder="传承现状描述" />
        </el-form-item>

        <el-form-item label="关联传承人">
          <el-select v-model="form.inheritorRefList" multiple filterable placeholder="可多选"
                     style="width: 100%">
            <el-option v-for="item in inheritorOptions" :key="item.id" :label="item.inheritorName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="关联民俗活动">
          <el-select v-model="form.activityRefList" multiple filterable placeholder="可多选"
                     style="width: 100%">
            <el-option v-for="item in activityOptions" :key="item.id" :label="item.activityName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="数据来源">
          <el-select v-model="form.sourceRef" clearable placeholder="单选" style="width: 100%">
            <el-option v-for="item in sourceOptions" :key="item.id" :label="item.sourceName" :value="item.id" />
          </el-select>
        </el-form-item>

        <!-- 内嵌多媒体素材：可动态增删 -->
        <el-form-item label="多媒体素材">
          <div style="width: 100%">
            <div v-for="(media, index) in form.mediaList" :key="index" class="media-row">
              <el-select v-model="media.mediaType" style="width: 100px">
                <el-option label="图片" value="图片" />
                <el-option label="视频" value="视频" />
              </el-select>
              <el-input v-model="media.mediaUrl" placeholder="素材网络链接 URL" style="flex: 1" />
              <el-input v-model="media.mediaDesc" placeholder="素材说明" style="width: 160px" />
              <el-button type="danger" :icon="Delete" circle @click="form.mediaList.splice(index, 1)" />
            </div>
            <el-button type="primary" plain :icon="Plus" @click="addMedia">添加素材</el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 项目 / 传承人详情弹窗 -->
    <HeritageDialog ref="detailDialogRef" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { activityApi, inheritorApi, projectApi, sourceApi } from '../../api'
import { useUserStore } from '../../stores/user'
import HeritageDialog from '../../components/HeritageDialog.vue'

const userStore = useUserStore()

const CATEGORY_OPTIONS = ['传统舞蹈', '传统戏剧', '传统医药', '民俗', '传统美术', '传统技艺', '民间文学', '传统音乐', '传统歌舞', '传统体育游艺', '传统知识']
const REGION_OPTIONS = ['西藏拉萨', '西藏山南', '西藏那曲', '西藏昌都', '西藏日喀则', '西藏林芝', '西藏阿里', '青海玉树', '青海西宁', '青海黄南', '青海果洛', '青海海东', '青海海南', '甘肃甘南', '云南迪庆', '云南怒江', '四川甘孜', '四川阿坝', '四川凉山', '四川绵阳']

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  projectName: '',
  projectCategory: '',
  region: '',
  pageNum: 1,
  pageSize: 10
})

const formVisible = ref(false)
const formRef = ref()
const detailDialogRef = ref()

// 下拉选项
const inheritorOptions = ref([])
const activityOptions = ref([])
const sourceOptions = ref([])

const emptyForm = () => ({
  id: null,
  projectName: '',
  projectCategory: '',
  projectLevel: '',
  region: '',
  projectIntro: '',
  inheritStatus: '',
  sourceRef: '',
  inheritorRefList: [],
  activityRefList: [],
  mediaList: []
})

const form = reactive(emptyForm())

const formRules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectCategory: [{ required: true, message: '请选择非遗类别', trigger: 'change' }],
  projectLevel: [{ required: true, message: '请选择级别', trigger: 'change' }],
  region: [{ required: true, message: '请选择所属地区', trigger: 'change' }]
}

/** 分页多条件查询 */
async function loadData() {
  loading.value = true
  try {
    const res = await projectApi.page(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  loadData()
}

function handleReset() {
  query.projectName = ''
  query.projectCategory = ''
  query.region = ''
  query.pageNum = 1
  loadData()
}

/** 加载下拉选项（传承人 / 活动 / 数据源） */
async function loadOptions() {
  const [inheritorRes, activityRes, sourceRes] = await Promise.all([
    inheritorApi.page({ pageNum: 1, pageSize: 200 }),
    activityApi.page({ pageNum: 1, pageSize: 200 }),
    sourceApi.page({ pageNum: 1, pageSize: 200 })
  ])
  inheritorOptions.value = inheritorRes.data.list
  activityOptions.value = activityRes.data.list
  sourceOptions.value = sourceRes.data.list
}

function openForm(row) {
  Object.assign(form, emptyForm())
  if (row) {
    const params = row
    form.id = params.id
    form.projectName = params.projectName
    form.projectCategory = params.projectCategory
    form.projectLevel = params.projectLevel
    form.region = params.region
    form.projectIntro = params.projectIntro
    form.inheritStatus = params.inheritStatus
    form.sourceRef = params.sourceRef || ''
    form.inheritorRefList = params.inheritorRefList ? [...params.inheritorRefList] : []
    form.activityRefList = params.activityRefList ? [...params.activityRefList] : []
    form.mediaList = params.mediaList ? params.mediaList.map((m) => ({ ...m })) : []
  }
  formVisible.value = true
}

function addMedia() {
  form.mediaList.push({ mediaType: '图片', mediaUrl: '', mediaDesc: '' })
}

/** 新增 / 修改 */
async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { ...form }
    if (form.id) {
      await projectApi.update(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await projectApi.save(payload)
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

/** 删除 */
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除非遗项目「${row.projectName}」吗？`, '提示', { type: 'warning' })
  await projectApi.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

/** 查看项目详情（弹窗展示简介与影像） */
function openDetail(row) {
  detailDialogRef.value?.openProject(row)
}

/** 查看传承人详情（弹窗展示简介与关联项目） */
function openInheritor(id) {
  detailDialogRef.value?.openInheritor(id)
}

onMounted(() => {
  loadData()
  loadOptions()
})
</script>

<style scoped>
.archive-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.archive-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.14em;
  color: var(--ink);
}

.archive-sub {
  font-size: 8.5px;
  letter-spacing: 0.2em;
  color: var(--ink-4);
}

.archive-count {
  margin-left: auto;
  padding: 3px 10px;
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--ink-3);
  background: #f1e7d5;
  border: 1px solid var(--line);
}

.empty {
  color: var(--ink-4);
}
.media-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

/* 可点击的项目名称 */
.name-link {
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 13px;
  color: var(--lapis);
  letter-spacing: 0.02em;
  cursor: pointer;
  border-bottom: 1px dashed rgba(30, 58, 85, 0.35);
  transition: color 0.18s ease, border-color 0.18s ease;
}
.name-link:hover {
  color: var(--cinnabar);
  border-bottom-color: var(--cinnabar);
}

/* 可点击的传承人 */
.person-link {
  margin: 2px 4px 2px 0;
  padding: 1px 8px;
  border: 1px solid var(--line);
  border-radius: 3px;
  background: #f7efe0;
  font-family: inherit;
  font-size: 11.5px;
  color: var(--ochre);
  cursor: pointer;
  transition: all 0.18s ease;
}
.person-link:hover {
  border-color: var(--ochre);
  background: #f3e3ca;
  color: #8a5424;
}
</style>