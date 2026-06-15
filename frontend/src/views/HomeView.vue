<template>
  <div class="home-page">
    <!-- 欢迎横幅 -->
    <section class="welcome-banner">
      <div class="banner-left">
        <h1 class="greeting">{{ greeting }}，{{ username }} 👋</h1>
        <p class="banner-sub">{{ todayStr }} · {{ weekdayStr }}</p>
        <p class="banner-quote">{{ randomQuote }}</p>
      </div>
      <div class="banner-right">
        <div class="banner-stat">
          <span class="banner-stat-num">6</span>
          <span class="banner-stat-label">功能模块</span>
        </div>
        <div class="banner-stat">
          <span class="banner-stat-num">{{ healthScore }}</span>
          <span class="banner-stat-label">健康评分</span>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-section">
      <h2 class="section-title">⚡ 快捷入口</h2>
      <div class="quick-grid">
        <div class="quick-card health-data" @click="$router.push('/health-data')">
          <span class="quick-icon">💪</span>
          <div class="quick-info">
            <h4>健康数据</h4>
            <p>记录和管理日常健康指标</p>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="quick-card ai-portrait" @click="$router.push('/ai-profile')">
          <span class="quick-icon">🧬</span>
          <div class="quick-info">
            <h4>AI 健康画像</h4>
            <p>AI 分析生成个性化健康报告</p>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="quick-card ai-plan" @click="$router.push('/ai-plan')">
          <span class="quick-icon">📋</span>
          <div class="quick-info">
            <h4>AI 健康计划</h4>
            <p>定制专属饮食 / 运动 / 睡眠方案</p>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="quick-card trends" @click="$router.push('/health-trends')">
          <span class="quick-icon">📈</span>
          <div class="quick-info">
            <h4>健康趋势</h4>
            <p>可视化查看健康数据变化</p>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="quick-card knowledge" @click="$router.push('/knowledge')">
          <span class="quick-icon">📚</span>
          <div class="quick-info">
            <h4>健康知识</h4>
            <p>AI 推送个性化健康资讯</p>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="quick-card reminders" @click="$router.push('/reminders')">
          <span class="quick-icon">⏰</span>
          <div class="quick-info">
            <h4>健康提醒</h4>
            <p>定时提醒养成健康习惯</p>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>

    <!-- 健康概览 + 今日建议 -->
    <div class="bottom-section">
      <section class="overview-section">
        <h2 class="section-title">📊 健康概览</h2>
        <el-card class="overview-card" shadow="never">
          <div class="overview-grid">
            <div class="overview-item">
              <span class="ov-icon">⚖️</span>
              <div>
                <span class="ov-value">70.5</span>
                <span class="ov-unit"> kg</span>
              </div>
              <span class="ov-label">最新体重</span>
            </div>
            <div class="overview-item">
              <span class="ov-icon">😴</span>
              <div>
                <span class="ov-value">7.5</span>
                <span class="ov-unit"> 小时</span>
              </div>
              <span class="ov-label">平均睡眠</span>
            </div>
            <div class="overview-item">
              <span class="ov-icon">🏃</span>
              <div>
                <span class="ov-value">45</span>
                <span class="ov-unit"> 分钟</span>
              </div>
              <span class="ov-label">平均运动</span>
            </div>
            <div class="overview-item">
              <span class="ov-icon">📋</span>
              <div>
                <span class="ov-value">5</span>
                <span class="ov-unit"> 条</span>
              </div>
              <span class="ov-label">本周记录</span>
            </div>
          </div>
        </el-card>
      </section>

      <section class="tip-section">
        <h2 class="section-title">💡 今日健康小贴士</h2>
        <el-card class="tip-card" shadow="never">
          <div class="tip-content">
            <span class="tip-icon">{{ currentTip.icon }}</span>
            <div class="tip-body">
              <h4>{{ currentTip.title }}</h4>
              <p>{{ currentTip.content }}</p>
            </div>
          </div>
        </el-card>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const username = computed(() => userStore.userInfo?.username || '用户')

// ===== 问候语 =====
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const todayStr = computed(() => {
  return new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
})

const weekdayStr = computed(() => {
  const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return days[new Date().getDay()]
})

// ===== 随机名言 =====
const quotes = [
  '健康是人生的第一财富。— 爱默生',
  '生命在于运动。— 伏尔泰',
  '最好的医生是自己，最好的药物是时间。',
  '一个健康的身体是灵魂的客厅。',
  '早睡早起，使人健康、富有、明智。— 富兰克林',
  '心情愉快是肉体和精神的最佳卫生法。',
]
const randomQuote = quotes[new Date().getDate() % quotes.length]

// ===== 健康评分 =====
const healthScore = computed(() => {
  // 简单模拟：基于当前分钟数变化
  const m = new Date().getMinutes()
  if (m < 20) return 92
  if (m < 40) return 88
  return 85
})

// ===== 今日小贴士 =====
const tips = [
  { icon: '💧', title: '多喝水', content: '每天喝 8 杯水，保持身体水分充足，有助于新陈代谢和皮肤健康。' },
  { icon: '🚶', title: '多走动', content: '久坐后起身活动 5 分钟，每小時走一走，減少久坐帶來的健康風險。' },
  { icon: '🥗', title: '均衡饮食', content: '多吃蔬菜水果，减少高油高盐食物摄入，保持营养均衡。' },
  { icon: '😴', title: '保证睡眠', content: '成年人每晚应保证 7-8 小时优质睡眠，睡前远离手机。' },
  { icon: '🧘', title: '保持好心态', content: '学会释放压力，保持积极乐观的心态，心理健康同样重要。' },
  { icon: '🏃', title: '坚持运动', content: '每周至少 150 分钟中等强度运动，让运动成为生活的一部分。' },
]
const currentTip = tips[new Date().getDay() % tips.length]
</script>

<style scoped>
.home-page { max-width: 1100px; margin: 0 auto; }

/* ===== Welcome banner ===== */
.welcome-banner {
  background:
    radial-gradient(ellipse 70% 50% at 15% 40%, rgba(232,93,74,0.12) 0%, transparent 50%),
    linear-gradient(135deg, rgba(26,44,43,0.94) 0%, rgba(26,44,43,0.88) 100%);
  border-radius: var(--r-xl); padding: 40px 44px;
  display: flex; justify-content: space-between; align-items: center;
  color: #fff; margin-bottom: 30px;
  box-shadow: var(--s-float); position: relative; overflow: hidden;
}
.welcome-banner::after {
  content: ''; position: absolute; top: -60%; right: -20%;
  width: 320px; height: 320px; border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.04) 0%, transparent 70%);
  pointer-events: none;
}
.banner-left { flex: 1; position: relative; z-index: 1; }
.greeting {
  margin: 0 0 8px; font-size: 28px; font-weight: 600;
  font-family: var(--font-display); letter-spacing: 0.01em;
}
.banner-sub { margin: 0 0 12px; font-size: 14px; opacity: 0.72; }
.banner-quote {
  margin: 0; font-size: 14px; font-style: italic; opacity: 0.5;
  max-width: 460px; font-family: var(--font-display);
}
.banner-right { display: flex; gap: 20px; flex-shrink: 0; position: relative; z-index: 1; }
.banner-stat {
  text-align: center; background: rgba(255,255,255,0.08);
  border-radius: var(--r-lg); padding: 18px 26px;
  backdrop-filter: blur(8px); border: 1px solid rgba(255,255,255,0.1);
}
.banner-stat-num {
  display: block; font-size: 36px; font-weight: 600;
  font-family: var(--font-display); letter-spacing: -0.01em;
}
.banner-stat-label { font-size: 12px; opacity: 0.65; margin-top: 3px; font-family: var(--font-body); }

/* ===== Section title ===== */
.section-title {
  margin: 0 0 16px; font-size: 18px; font-weight: 600;
  font-family: var(--font-display); color: var(--c-text);
}

/* ===== Quick cards ===== */
.quick-section { margin-bottom: 30px; }
.quick-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.quick-card {
  background: var(--c-surface); border-radius: var(--r-lg); padding: 22px 20px;
  display: flex; align-items: center; gap: 14px;
  cursor: pointer; transition: all var(--t-normal);
  border: 1px solid var(--c-border-light); position: relative; overflow: hidden;
}
.quick-card::before {
  content: ''; position: absolute; left: 0; top: 0; bottom: 0;
  width: 3px; border-radius: 0 2px 2px 0;
  transition: width var(--t-normal), opacity var(--t-normal);
}
.quick-card.health-data::before { background: var(--c-primary); }
.quick-card.ai-portrait::before { background: #7C6FF7; }
.quick-card.ai-plan::before { background: #0D7377; }
.quick-card.trends::before { background: var(--c-accent); }
.quick-card.knowledge::before { background: var(--c-amber); }
.quick-card.reminders::before { background: #5B7BD5; }
.quick-card:hover {
  transform: translateY(-3px); box-shadow: var(--s-float);
  border-color: transparent; background: var(--c-surface);
}
.quick-card:hover::before { width: 4px; }
.quick-icon { font-size: 36px; flex-shrink: 0; }
.quick-info { flex: 1; min-width: 0; }
.quick-info h4 {
  margin: 0 0 3px; font-size: 15px; font-weight: 600;
  color: var(--c-text); font-family: var(--font-display);
}
.quick-info p { margin: 0; font-size: 12px; color: var(--c-text-muted); }
.quick-arrow {
  color: var(--c-text-muted); font-size: 18px; flex-shrink: 0;
  transition: all var(--t-normal); opacity: 0.3;
}
.quick-card:hover .quick-arrow {
  color: var(--c-primary); opacity: 1; transform: translateX(5px);
}

/* ===== Bottom ===== */
.bottom-section { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.overview-card, .tip-card { border-radius: var(--r-lg); border: 1px solid var(--c-border-light); }

/* ===== Overview ===== */
.overview-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.overview-item {
  text-align: center; padding: 18px 10px;
  background: var(--c-bg); border-radius: var(--r-md);
  transition: background var(--t-fast);
}
.overview-item:hover { background: var(--c-primary-light); }
.overview-item .ov-icon { font-size: 28px; display: block; margin-bottom: 8px; }
.ov-value { font-size: 26px; font-weight: 600; font-family: var(--font-display); color: var(--c-text); }
.ov-unit { font-size: 13px; color: var(--c-text-muted); }
.ov-label { display: block; margin-top: 3px; font-size: 11px; color: var(--c-text-muted); }

/* ===== Tip ===== */
.tip-card { height: 100%; }
.tip-content { display: flex; align-items: flex-start; gap: 18px; }
.tip-icon { font-size: 44px; flex-shrink: 0; }
.tip-body h4 { margin: 0 0 8px; font-size: 16px; font-weight: 600; font-family: var(--font-display); color: var(--c-text); }
.tip-body p { margin: 0; color: var(--c-text-secondary); line-height: 1.7; font-size: 14px; }

@media (max-width: 768px) {
  .welcome-banner { flex-direction: column; gap: 24px; padding: 28px; }
  .banner-right { width: 100%; justify-content: space-around; }
  .quick-grid { grid-template-columns: 1fr; }
  .bottom-section { grid-template-columns: 1fr; }
}
</style>
