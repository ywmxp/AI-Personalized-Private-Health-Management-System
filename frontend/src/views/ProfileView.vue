<template>
  <div class="page">
    <h2>个人中心</h2>

    <el-card v-loading="loading" class="profile-card">
      <!-- 展示模式 -->
      <template v-if="!editing">
        <el-descriptions :column="2" border class="profile-descriptions">
          <el-descriptions-item label="用户 ID">
            {{ user?.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ user?.phone }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ user?.username }}
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="user?.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="出生日期">
            {{ user?.birthDate || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ genderLabel(user?.gender) }}
          </el-descriptions-item>
          <el-descriptions-item label="身高">
            {{ user?.height ? user.height + ' cm' : '未填写' }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="edit-btn-wrapper">
          <el-button type="primary" @click="startEdit">编辑信息</el-button>
        </div>
      </template>

      <!-- 编辑模式 -->
      <template v-else>
        <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-width="80px"
          class="edit-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" maxlength="20" />
          </el-form-item>
          <el-form-item label="出生日期" prop="birthDate">
            <el-date-picker
              v-model="form.birthDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
              <el-option label="男" value="MALE" />
              <el-option label="女" value="FEMALE" />
              <el-option label="保密" value="UNKNOWN" />
            </el-select>
          </el-form-item>
          <el-form-item label="身高 (cm)" prop="height">
            <el-input-number
              v-model="form.height"
              :min="50"
              :max="300"
              :precision="1"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
        <div class="edit-actions">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
        </div>
      </template>
    </el-card>

    <!-- 修改密码 -->
    <el-card class="password-card">
      <template #header><span>修改密码</span></template>
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px" style="max-width: 400px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="pwdSaving" @click="handleChangePwd">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getCurrentUser, updateProfile, changePassword } from '../api/user'
import { useUserStore } from '../stores/user'
import type { User } from '../types'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const editing = ref(false)
const user = ref<User | null>(null)
const formRef = ref<FormInstance>()

const form = ref({
  username: '',
  birthDate: '',
  gender: '',
  height: undefined as number | undefined,
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符之间', trigger: 'blur' },
  ],
}

function genderLabel(g?: string) {
  const map: Record<string, string> = { MALE: '男', FEMALE: '女', UNKNOWN: '保密' }
  return g ? map[g] || g : '未填写'
}

function startEdit() {
  if (!user.value) return
  form.value = {
    username: user.value.username,
    birthDate: user.value.birthDate || '',
    gender: user.value.gender || '',
    height: user.value.height,
  }
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function saveEdit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const submitData = {
      username: form.value.username,
      birthDate: form.value.birthDate || undefined,
      gender: form.value.gender || undefined,
      height: form.value.height,
    }
    await updateProfile(submitData)
    // 用表单数据更新本地展示和 Pinia store
    if (user.value) {
      user.value.username = form.value.username
      user.value.birthDate = form.value.birthDate || undefined
      user.value.gender = (form.value.gender || undefined) as User['gender']
      user.value.height = form.value.height
    }
    userStore.userInfo = user.value
    ElMessage.success('修改成功')
    editing.value = false
  } catch {
    // 错误已在拦截器中处理
  } finally {
    saving.value = false
  }
}

// ============ 修改密码 ============
const pwdFormRef = ref<FormInstance>()
const pwdSaving = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPwd = (_rule: unknown, value: string, callback: (err?: Error) => void) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' },
  ],
}

async function handleChangePwd() {
  if (!pwdFormRef.value) return
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdSaving.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdFormRef.value.resetFields()
  } catch {
    // handled
  } finally {
    pwdSaving.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getCurrentUser()
    user.value = res.data.data
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.profile-descriptions {
  margin-bottom: 20px;
}

.edit-btn-wrapper {
  display: flex;
  justify-content: flex-end;
}

.edit-form {
  margin-bottom: 20px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.password-card {
  margin-top: 20px;
}
</style>
