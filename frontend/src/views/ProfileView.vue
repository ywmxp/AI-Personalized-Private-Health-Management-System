<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <p class="page-motto">您的健康档案，由您掌握</p>

    <!-- ====== Info Card ====== -->
    <el-card v-loading="loading" class="info-card">
      <!-- View mode -->
      <template v-if="!editing">
        <div class="info-header">
          <div class="avatar-circle">
            <span class="avatar-text">{{ (user?.username || 'U')[0] }}</span>
          </div>
          <div class="info-title">
            <h3>{{ user?.username }}</h3>
            <span class="info-role">
              <el-tag :type="user?.role === 'ADMIN' ? 'warning' : 'info'" size="small" effect="plain">
                {{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </span>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">手机号</span>
            <span class="info-value highlight">{{ user?.phone || '—' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">用户 ID</span>
            <span class="info-value">{{ user?.userId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">出生日期</span>
            <span class="info-value">{{ user?.birthDate || '未填写' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">性别</span>
            <span class="info-value">{{ genderLabel(user?.gender) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">身高</span>
            <span class="info-value">{{ user?.height ? user.height + ' cm' : '未填写' }}</span>
          </div>
        </div>

        <div class="info-actions">
          <el-button type="primary" @click="startEdit">编辑信息</el-button>
        </div>
      </template>

      <!-- Edit mode -->
      <template v-else>
        <div class="edit-header">
          <h3>编辑个人信息</h3>
        </div>

        <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-position="top"
          class="profile-edit-form"
        >
          <div class="edit-grid">
            <el-form-item prop="username">
              <label class="field-label">用户名</label>
              <el-input v-model="form.username" maxlength="20" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item prop="birthDate">
              <label class="field-label">出生日期</label>
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item prop="gender">
              <label class="field-label">性别</label>
              <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
                <el-option label="男" value="MALE" />
                <el-option label="女" value="FEMALE" />
                <el-option label="保密" value="UNKNOWN" />
              </el-select>
            </el-form-item>
            <el-form-item prop="height">
              <label class="field-label">身高 (cm)</label>
              <el-input-number
                v-model="form.height"
                :min="50" :max="300" :precision="1"
                style="width: 100%" placeholder="请输入身高"
              />
            </el-form-item>
          </div>
        </el-form>

        <div class="edit-actions">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveEdit">保存修改</el-button>
        </div>
      </template>
    </el-card>

    <!-- ====== Password Card ====== -->
    <el-card class="password-card">
      <h3 class="pwd-title">修改密码</h3>

      <el-form
        :model="pwdForm"
        :rules="pwdRules"
        ref="pwdFormRef"
        label-position="top"
        class="pwd-form"
      >
        <div class="pwd-grid">
          <el-form-item prop="oldPassword">
            <label class="field-label">原密码</label>
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item prop="newPassword">
            <label class="field-label">新密码</label>
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位新密码" />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <label class="field-label">确认新密码</label>
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
        </div>
        <div class="pwd-actions">
          <el-button type="primary" :loading="pwdSaving" @click="handleChangePwd">修改密码</el-button>
        </div>
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
.profile-page { max-width: 720px; margin: 0 auto; }

/* ===== Card base ===== */
.info-card, .password-card {
  border-radius: var(--r-lg);
  border: 1px solid var(--c-border-light);
  margin-bottom: 22px;
}
.info-card :deep(.el-card__body),
.password-card :deep(.el-card__body) {
  padding: 28px 32px;
}

/* ===== View mode — info header ===== */
.info-header {
  display: flex; align-items: center; gap: 18px;
  margin-bottom: 28px; padding-bottom: 22px;
  border-bottom: 1px solid var(--c-border-light);
}
.avatar-circle {
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, var(--c-primary), #14919B);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.avatar-text {
  font-size: 22px; font-weight: 700; color: #fff;
  font-family: var(--font-display);
  text-transform: uppercase;
}
.info-title h3 {
  margin: 0 0 4px; font-size: 20px;
  font-family: var(--font-display);
  color: var(--c-text);
}
.info-role { display: inline-block; }

/* ===== Info grid ===== */
.info-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 20px 16px;
  margin-bottom: 24px;
}
.info-item { grid-column: span 4; }
.info-item:nth-child(1) { grid-column: span 6; }
.info-item:nth-child(2) { grid-column: span 6; }

.info-label {
  display: block; font-size: 11px; font-weight: 600;
  color: var(--c-text-muted);
  text-transform: uppercase; letter-spacing: 0.06em;
  margin-bottom: 4px;
}
.info-value {
  font-size: 15px; color: var(--c-text);
  font-weight: 500;
}
.info-value.highlight {
  font-weight: 600; color: var(--c-primary);
  font-size: 16px; font-family: var(--font-display);
}

.info-actions {
  display: flex; justify-content: flex-end;
  padding-top: 4px;
}

/* ===== Edit mode ===== */
.edit-header h3 {
  margin-bottom: 22px; padding-bottom: 16px;
  border-bottom: 1px solid var(--c-border-light);
  font-family: var(--font-display); font-size: 18px;
}
.edit-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 20px;
}

/* Hide default element-plus labels */
.profile-edit-form :deep(.el-form-item__label),
.pwd-form :deep(.el-form-item__label) { display: none; }

.field-label {
  display: block; margin-bottom: 6px;
  font-size: 12px; font-weight: 600;
  color: var(--c-text-secondary);
  text-transform: uppercase; letter-spacing: 0.04em;
}

/* Form input styling */
.profile-edit-form :deep(.el-input__wrapper),
.pwd-form :deep(.el-input__wrapper) {
  border-radius: var(--r-md);
  box-shadow: 0 0 0 1px var(--c-border) inset !important;
  background: var(--c-bg);
  transition: box-shadow var(--t-fast), background var(--t-fast);
}
.profile-edit-form :deep(.el-input__wrapper:hover),
.pwd-form :deep(.el-input__wrapper:hover) {
  background: var(--c-surface);
  box-shadow: 0 0 0 1px var(--c-primary) inset !important;
}
.profile-edit-form :deep(.el-input__wrapper.is-focus),
.pwd-form :deep(.el-input__wrapper.is-focus) {
  background: var(--c-surface);
  box-shadow: 0 0 0 2px rgba(13,115,119,0.25) inset !important;
}

.edit-actions {
  display: flex; justify-content: flex-end; gap: 12px;
  margin-top: 8px; padding-top: 20px;
  border-top: 1px solid var(--c-border-light);
}

/* ===== Password section ===== */
.pwd-title {
  margin-bottom: 22px; padding-bottom: 14px;
  border-bottom: 1px solid var(--c-border-light);
  font-family: var(--font-display); font-size: 18px;
}
.pwd-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 20px;
}
.pwd-grid .el-form-item:nth-child(3) {
  grid-column: span 2;
}

.pwd-actions {
  display: flex; justify-content: flex-end;
  margin-top: 8px;
}
</style>
