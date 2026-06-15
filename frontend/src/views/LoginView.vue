<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <h2 class="login-title">AI 个性化健康管理</h2>
      <p class="login-subtitle">登录您的健康账户</p>

      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        class="login-form"
      >
        <el-form-item prop="phone">
          <label class="field-label">手机号</label>
          <el-input
            v-model="loginForm.phone"
            placeholder="请输入手机号"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <label class="field-label">密码</label>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="submit-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-link">
        还没有账号？
        <el-link type="primary" @click="$router.push('/register')">
          立即注册
        </el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = ref({
  phone: '',
  password: '',
})

const loginRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

async function handleLogin() {
  if (!loginFormRef.value) return
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(loginForm.value.phone, loginForm.value.password)
    userStore.setLoginInfo(res.data.data!)
    ElMessage.success('登录成功')
    router.push('/home')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== Organic fluid gradient background ===== */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background:
    radial-gradient(ellipse 70% 70% at 20% 30%, rgba(13,115,119,0.15) 0%, transparent 50%),
    radial-gradient(ellipse 50% 50% at 80% 20%, rgba(13,115,119,0.08) 0%, transparent 40%),
    radial-gradient(ellipse 60% 60% at 60% 70%, rgba(232,93,74,0.06) 0%, transparent 45%),
    radial-gradient(ellipse 40% 50% at 40% 90%, rgba(212,145,62,0.07) 0%, transparent 40%),
    linear-gradient(175deg, #F5F0EB 0%, #FAF8F5 40%, #F0ECE7 100%);
  position: relative;
}
.login-container::after {
  content: '';
  position: absolute; inset: 0; pointer-events: none;
  background-image: var(--noise); opacity: 0.25;
}

/* ===== Card ===== */
.login-card {
  width: 440px; padding: 0;
  border-radius: var(--r-xl);
  box-shadow: var(--s-modal);
  border: 1px solid var(--c-border-light);
  overflow: hidden;
  position: relative; z-index: 1;
}
.login-card::before {
  content: ''; display: block; height: 3px;
  background: linear-gradient(90deg, var(--c-primary), #14919B, var(--c-accent));
}
.login-card :deep(.el-card__body) {
  padding: 44px 48px 36px;
}

/* ===== Title ===== */
.login-title {
  text-align: center; margin-bottom: 6px;
  color: var(--c-text);
  font-family: var(--font-display);
  font-weight: 600; font-size: 24px;
}
.login-subtitle {
  text-align: center; margin-bottom: 32px;
  color: var(--c-text-muted); font-size: 13px;
}

/* ===== Form ===== */
.login-form { margin-bottom: 8px; }

/* Hide default element-plus label */
.login-form :deep(.el-form-item__label) { display: none; }

/* Custom label above input */
.field-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px; font-weight: 600;
  color: var(--c-text-secondary);
  font-family: var(--font-body);
  letter-spacing: 0.01em;
}

/* Form item spacing */
.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}
.login-form :deep(.el-form-item:last-of-type) {
  margin-bottom: 0;
}

/* Input wrapper — clean line style */
.login-form :deep(.el-input__wrapper) {
  border-radius: var(--r-md);
  padding: 2px 14px; height: 46px;
  box-shadow: 0 0 0 1px var(--c-border) inset !important;
  background: var(--c-bg);
  transition: box-shadow var(--t-fast), background var(--t-fast);
}
.login-form :deep(.el-input__wrapper:hover) {
  background: var(--c-surface);
  box-shadow: 0 0 0 1px var(--c-primary) inset !important;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  background: var(--c-surface);
  box-shadow: 0 0 0 2px rgba(13,115,119,0.25) inset !important;
}
.login-form :deep(.el-input__inner) {
  font-size: 15px; color: var(--c-text);
}
.login-form :deep(.el-input__inner::placeholder) {
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
  margin-top: 4px;
}
.submit-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 8px 28px rgba(13,115,119,0.3) !important;
}

/* ===== Footer link ===== */
.register-link {
  text-align: center; color: var(--c-text-muted); font-size: 13px; margin-top: 24px;
}
.register-link :deep(.el-link) {
  font-weight: 600; color: var(--c-primary);
}
</style>
