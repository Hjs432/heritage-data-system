<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <span class="tip">数据来源为独立文档，非遗项目通过 source_ref 引用</span>
        <el-button v-if="userStore.isAdmin" type="success" :icon="Plus" @click="openForm()">新增数据来源</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="sourceName" label="来源平台名称" width="220" />
        <el-table-column label="原始网页链接" min-width="260">
          <template #default="{ row }">
            <el-link v-if="row.originalUrl" type="primary" :href="row.originalUrl" target="_blank">
              {{ row.originalUrl }}
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sourceNote" label="备注说明" min-width="200" show-overflow-tooltip />
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

    <el-dialog v-model="formVisible" :title="form.id ? '编辑数据来源' : '新增数据来源'" width="540px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="来源平台名称" prop="sourceName">
          <el-input v-model="form.sourceName" placeholder="如：中国非物质文化遗产网" />
        </el-form-item>
        <el-form-item label="原始网页链接">
          <el-input v-model="form.originalUrl" placeholder="https://" />
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="form.sourceNote" type="textarea" :rows="3" placeholder="备注说明" />
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
import { sourceApi } from '../../api'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const formVisible = ref(false)
const formRef = ref()

const query = reactive({ pageNum: 1, pageSize: 10 })

const emptyForm = () => ({ id: null, sourceName: '', originalUrl: '', sourceNote: '' })
const form = reactive(emptyForm())

const formRules = {
  sourceName: [{ required: true, message: '请输入来源平台名称', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await sourceApi.page(query)
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
    form.sourceName = row.sourceName
    form.originalUrl = row.originalUrl
    form.sourceNote = row.sourceNote
  }
  formVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await sourceApi.update(form.id, { ...form })
      ElMessage.success('修改成功')
    } else {
      await sourceApi.save({ ...form })
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除数据来源「${row.sourceName}」吗？`, '提示', { type: 'warning' })
  await sourceApi.remove(row.id)
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