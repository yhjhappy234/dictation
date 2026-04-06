<template>
  <div class="app-container">
    <el-container>
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo" @click="$router.push('/')">
            <el-icon :size="32" color="#409EFF"><Edit /></el-icon>
            <span class="title">小学生听写助手</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            :ellipsis="false"
            class="nav-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <span>听写</span>
            </el-menu-item>
            <el-menu-item index="/history">
              <el-icon><Clock /></el-icon>
              <span>历史记录</span>
            </el-menu-item>
            <el-menu-item index="/difficult">
              <el-icon><Star /></el-icon>
              <span>生词本</span>
            </el-menu-item>
            <el-menu-item index="/reports">
              <el-icon><DataAnalysis /></el-icon>
              <span>报表</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 浏览器兼容性提示 -->
    <el-dialog
      v-model="showBrowserTip"
      title="浏览器兼容性提示"
      width="400px"
      center
    >
      <div class="browser-tip">
        <el-icon :size="48" color="#E6A23C"><Warning /></el-icon>
        <p>语音识别功能需要使用Chrome浏览器才能正常工作。</p>
        <p>请使用Chrome浏览器访问本应用以获得最佳体验。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const showBrowserTip = ref(false)

const handleMenuSelect = (index) => {
  router.push(index)
}

// 检查浏览器兼容性
const checkBrowserCompatibility = () => {
  const isChrome = /Chrome/.test(navigator.userAgent) && /Google Inc/.test(navigator.vendor)
  const hasSpeechRecognition = 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window

  if (!isChrome || !hasSpeechRecognition) {
    showBrowserTip.value = true
  }
}

onMounted(() => {
  checkBrowserCompatibility()
})
</script>

<style lang="scss">
.app-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.app-header {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 70px !important;

  .header-content {
    max-width: 1400px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;

    .title {
      font-size: 24px;
      font-weight: bold;
      color: #409EFF;
    }
  }

  .nav-menu {
    border-bottom: none;
    background: transparent;

    .el-menu-item {
      font-size: 16px;
      padding: 0 20px;

      &:hover {
        background-color: rgba(64, 158, 255, 0.1);
      }

      &.is-active {
        border-bottom: 3px solid #409EFF;
        color: #409EFF;
      }
    }
  }
}

.app-main {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.browser-tip {
  text-align: center;
  padding: 20px;

  p {
    margin: 15px 0;
    font-size: 16px;
    color: #606266;
  }
}
</style>