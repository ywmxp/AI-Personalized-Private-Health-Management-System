<template>
  <div class="register-container">
    <el-card class="register-card" shadow="hover">
      <h2 class="register-title">创建健康账户</h2>
      <p class="register-subtitle">加入 AI 个性化健康管理</p>

      <el-form
        :model="registerForm"
        :rules="registerRules"
        ref="registerFormRef"
        class="register-form"
      >
        <el-form-item prop="phone">
          <label class="field-label">手机号</label>
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>

        <el-form-item prop="username">
          <label class="field-label">用户名</label>
          <el-input v-model="registerForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>

        <el-form-item prop="password">
          <label class="field-label">密码</label>
          <el-input v-model="registerForm.password" type="password" placeholder="至少 6 位密码" show-password clearable />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <label class="field-label">确认密码</label>
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password clearable />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="submit-btn" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-link">
        已有账号？
        <el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { register } from '../api/auth'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = ref({
  phone: '',
  username: '',
  password: '',
  confirmPassword: '',
})

const registerRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' },
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符之间', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (err?: Error) => void) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function handleRegister() {
  if (!registerFormRef.value) return
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register(
      registerForm.value.phone,
      registerForm.value.username,
      registerForm.value.password,
      registerForm.value.confirmPassword,
    )
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== Background ===== */
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background:
    radial-gradient(ellipse 60% 50% at 80% 75%, rgba(13,115,119,0.08) 0%, transparent 50%),
    radial-gradient(ellipse 50% 40% at 20% 15%, rgba(232,93,74,0.04) 0%, transparent 40%),
    linear-gradient(180deg, #FAF8F5 0%, #F5F0EB 100%);
}

/* ===== Card ===== */
.register-card {
  width: 440px; padding: 0;
  border-radius: var(--r-xl);
  box-shadow: var(--s-modal);
  border: 1px solid var(--c-border-light);
  overflow: hidden;
}
.register-card::before {
  content: ''; display: block; height: 3px;
  background: linear-gradient(90deg, var(--c-primary), #14919B, var(--c-accent));
}
.register-card :deep(.el-card__body) {
  padding: 40px 48px 32px;
}

/* ===== Title ===== */
.register-title {
  text-align: center; margin-bottom: 4px;
  color: var(--c-text);
  font-family: var(--font-display);
  font-weight: 600; font-size: 24px;
}
.register-subtitle {
  text-align: center; margin-bottom: 28px;
  color: var(--c-text-muted); font-size: 13px;
}

/* ===== Form ===== */
.register-form { margin-bottom: 4px; }

.register-form :deep(.el-form-item__label) { display: none; }

.field-label {
  display: block; margin-bottom: 8px;
  font-size: 13px; font-weight: 600;
  color: var(--c-text-secondary);
  font-family: var(--font-body);
}

.register-form :deep(.el-form-item) { margin-bottom: 18px; }
.register-form :deep(.el-form-item:last-of-type) { margin-bottom: 0; }

.register-form :deep(.el-input__wrapper) {
  border-radius: var(--r-md);
  padding: 2px 14px; height: 46px;
  box-shadow: 0 0 0 1px var(--c-border) inset !important;
  background: var(--c-bg);
  transition: box-shadow var(--t-fast), background var(--t-fast);
}
.register-form :deep(.el-input__wrapper:hover) {
  background: var(--c-surface);
  box-shadow: 0 0 0 1px var(--c-primary) inset !important;
}
.register-form :deep(.el-input__wrapper.is-focus) {
  background: var(--c-surface);
  box-shadow: 0 0 0 2px rgba(13,115,119,0.25) inset !important;
}
.register-form :deep(.el-input__inner) {
  font-size: 15px; color: var(--c-text);
}
.register-form :deep(.el-input__inner::placeholder) {
  color: var(--c-text-muted);
}

/* Submit button */
.submit-btn {
  width: 100%; height: 48px !important;
  font-size: 16px !important; font-weight: 600 !important;
  letter-spacing: 0.08em;
  font-family: var(--font-display);
  border-radius: var(--r-md) !important;
  background: linear-gradient(135deg, var(--c-primary), #14919B) !important;
  border: none !important;
  transition: all var(--t-normal) !important;
  margin-top: 2px;
}
.submit-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 8px 28px rgba(13,115,119,0.3) !important;
}

/* ===== Footer ===== */
.login-link {
  text-align: center; color: var(--c-text-muted); font-size: 13px; margin-top: 22px;
}
.login-link :deep(.el-link) {
  font-weight: 600; color: var(--c-primary);
}
</style>
