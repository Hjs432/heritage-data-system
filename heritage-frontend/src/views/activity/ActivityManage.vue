<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <span class="tip">民俗活动为独立文档，多个非遗项目可引用同一活动</span>
        <el-button v-if="userStore.isAdmin" type="success" :icon="Plus" @click="openForm()">新增民俗活动</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="activityName" label="活动名称" width="160" />
        <el-table-column prop="activityAddr" label="举办地点" width="180" />
        <el-table-column prop="activityTime" label="举办时间" width="160" />
        <el-table-column prop="activityDesc" label="活动描述" min-width="240" show-overflow-tooltip />
        <el-table-column v-if="userStore.isAdmin" label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="formVisible" :title="form.id ? '编辑民俗活动' : '新增民俗活动'" width="540px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="form.activityName" placeholder="如：玉树赛马会" />
        </el-form-item>
        <el-form-item label="举办地点">
          <el-input v-model="form.activityAddr" placeholder="如：青海省玉树藏族自治州" />
        </el-form-item>
        <el-form-item label="举办时间">
          <el-input v-model="form.activityTime" placeholder="如：每年7月下旬" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.activityDesc" type="textarea" :rows="3" placeholder="活动描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { activityApi } from '../../api'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const formVisible = ref(false)
const formRef = ref()

const query = reactive({ pageNum: 1, pageSize: 10 })

const emptyForm = () => ({
  id: null,
  activityName: '',
  activityAddr: '',
  activityTime: '',
  activityDesc: ''
})
const form = reactive(emptyForm())

const formRules = {
  activityName: [{ required: true, message: '请输入民俗活动名称', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await activityApi.page(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  Object.assign(form, emptyForm())
  if (row) {
    form.id = row.id
    form.activityName = row.activityName
    form.activityAddr = row.activityAddr
    form.activityTime = row.activityTime
    form.activityDesc = row.activityDesc
  }
  formVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await activityApi.update(form.id, { ...form })
      ElMessage.success('修改成功')
    } else {
      await activityApi.save({ ...form })
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除民俗活动「${row.activityName}」吗？`, '提示', { type: 'warning' })
  await activityApi.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line);
}
.tip {
  color: var(--ink-4);
  font-size: 12.5px;
  letter-spacing: 0.04em;
}
</style>