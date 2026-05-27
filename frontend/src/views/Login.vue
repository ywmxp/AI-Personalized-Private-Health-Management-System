<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <h2 class="login-title">AI个性化健康管理系统</h2>
      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        label-width="80px"
        class="login-form"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="loginForm.phone"
            placeholder="请输入手机号"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
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
            @click="handleLogin"
            style="width: 100%; height: 40px; font-size: 16px"
          >
            登录
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
import { ElForm, ElMessage } from 'element-plus'

// 获取路由实例
const router = useRouter()

// 表单引用
const loginFormRef = ref<InstanceType<typeof ElForm>>()

// 表单数据
const loginForm = ref({
  phone: '',
  password: ''
})

// 表单验证规则
const loginRules = ref({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
})

// 登录方法
const handleLogin = async () => {
  if (!loginFormRef.value) return

  // 先执行表单验证
  await loginFormRef.value.validate()

  // 模拟登录成功（后面对接后端接口时替换这部分）
  ElMessage.success('登录成功！')
  // 保存token和用户名到本地存储
  localStorage.setItem('token', 'mock-token-123456')
  localStorage.setItem('username', loginForm.value.phone)
  // 跳转到首页
  router.push('/')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 450px;
  padding: 30px 50px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
  font-weight: 600;
}

.login-form {
  margin-bottom: 20px;
}

.register-link {
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style>