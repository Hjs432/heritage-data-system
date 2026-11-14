<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <span class="tip">传承人为独立文档，可被多个非遗项目引用</span>
        <el-button v-if="userStore.isAdmin" type="success" :icon="Plus" @click="openForm()">新增传承人</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="姓名" width="130">
          <template #default="{ row }">
            <button type="button" class="name-link" @click="openDetail(row)">{{ row.inheritorName }}</button>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="nation" label="民族" width="100" />
        <el-table-column prop="intro" label="简介 / 传承贡献" min-width="260" show-overflow-tooltip />
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

    <el-dialog v-model="formVisible" :title="form.id ? '编辑传承人' : '新增传承人'" width="540px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="姓名" prop="inheritorName">
          <el-input v-model="form.inheritorName" placeholder="请输入传承人姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="民族">
          <el-input v-model="form.nation" placeholder="如：藏族" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="简介、传承贡献" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 传承人 / 项目详情弹窗 -->
    <HeritageDialog ref="detailDialogRef" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { inheritorApi } from '../../api'
import { useUserStore } from '../../stores/user'
import HeritageDialog from '../../components/HeritageDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const formVisible = ref(false)
const formRef = ref()
const detailDialogRef = ref()

const query = reactive({ pageNum: 1, pageSize: 10 })

const emptyForm = () => ({ id: null, inheritorName: '', gender: '男', nation: '', intro: '' })
const form = reactive(emptyForm())

const formRules = {
  inheritorName: [{ required: true, message: '请输入传承人姓名', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await inheritorApi.page(query)
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
    form.inheritorName = row.inheritorName
    form.gender = row.gender
    form.nation = row.nation
    form.intro = row.intro
  }
  formVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await inheritorApi.update(form.id, { ...form })
      ElMessage.success('修改成功')
    } else {
      await inheritorApi.save({ ...form })
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除传承人「${row.inheritorName}」吗？`, '提示', { type: 'warning' })
  await inheritorApi.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

/** 查看传承人详情（弹窗展示简介与关联项目） */
function openDetail(row) {
  detailDialogRef.value?.openInheritor(row.id)
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

/* 可点击的传承人姓名 */
.name-link {
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 13px;
  color: var(--lapis);
  cursor: pointer;
  border-bottom: 1px dashed rgba(30, 58, 85, 0.35);
  transition: color 0.18s ease, border-color 0.18s ease;
}
.name-link:hover {
  color: var(--cinnabar);
  border-bottom-color: var(--cinnabar);
}
</style>