<template>
  <div class="common-layout">
    <el-container class="layout-container">
      <el-aside width="240px">
        <div class="logo">
          <el-icon style="margin-right: 10px;"><Platform /></el-icon>
          实验室管理系统
        </div>
        <el-menu
          :default-active="$route.path"
          router
          background-color="#001529"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>系统首页</span>
          </el-menu-item>
          
          <el-menu-item index="/appointment">
            <el-icon><Calendar /></el-icon>
            <span>实验室预约</span>
          </el-menu-item>

          <el-menu-item index="/consumables">
            <el-icon><Sell /></el-icon> <span>耗材管理</span>
          </el-menu-item>

          <el-menu-item v-if="userRole === 'admin'" index="/users">
            <el-icon><User /></el-icon>
            <span>人员管理</span>
          </el-menu-item>

          <el-menu-item v-if="userRole === 'admin'" index="/monitor">
            <el-icon><VideoCamera /></el-icon>
            <span>视频实时监控</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>智慧实验室</el-breadcrumb-item>
              <el-breadcrumb-item>{{ $route.name || '当前页面' }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <div class="user-info">
              <span style="margin-right: 15px;">欢迎您，{{ userName }}</span>
              <el-button type="danger" size="small" link @click="handleLogout">退出系统</el-button>
            </div>
            <el-avatar :size="32" icon="UserFilled" style="margin-left: 10px;" />
          </div>
        </el-header>

        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 修改点 2: 引入需要的图标，防止图标不显示
import { 
  Platform, HomeFilled, Calendar, User, Sell, VideoCamera, UserFilled, Box
} from '@element-plus/icons-vue'

const router = useRouter()
const userRole = ref(localStorage.getItem('role') || '')
const userName = ref(localStorage.getItem('userName') || '游客')

const handleLogout = () => {
  localStorage.clear()
  ElMessage.info('已安全退出系统')
  router.push('/login')
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.el-aside { background-color: #001529; overflow-x: hidden; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: bold; background-color: #002140; color: #fff; }
.el-header { background-color: #fff; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #dcdfe6; height: 60px; padding: 0 20px; }
.header-right { display: flex; align-items: center; font-size: 14px; color: #666; }
.el-main { background-color: #f0f2f5; padding: 20px; }
.user-info { display: flex; align-items: center; }
</style>