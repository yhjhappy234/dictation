<template>
  <div class="history-page">
    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleFilter"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="statusFilter"
            placeholder="全部状态"
            clearable
            @change="handleFilter"
          >
            <el-option label="已完成" value="completed" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已暂停" value="paused" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleFilter">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批次列表 -->
    <div class="batch-list" v-loading="loading">
      <el-empty v-if="batchList.length === 0" description="暂无听写记录">
        <el-button type="primary" @click="$router.push('/')">开始听写</el-button>
      </el-empty>

      <el-row :gutter="20" v-else>
        <el-col
          v-for="batch in batchList"
          :key="batch.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="batch-card" shadow="hover" @click="showDetail(batch)">
            <div class="batch-header">
              <div class="batch-name">
                <el-icon><Document /></el-icon>
                <span>{{ batch.name }}</span>
              </div>
              <el-tag
                :type="getStatusType(batch.status)"
                size="small"
              >
                {{ getStatusText(batch.status) }}
              </el-tag>
            </div>

            <div class="batch-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(batch.createdAt) }}</span>
            </div>

            <div class="batch-words">
              <div class="words-preview">
                <el-tag
                  v-for="(word, index) in batch.words.slice(0, 5)"
                  :key="index"
                  size="small"
                  class="word-tag"
                >
                  {{ word }}
                </el-tag>
                <span v-if="batch.words.length > 5" class="more-tag">
                  +{{ batch.words.length - 5 }}
                </span>
              </div>
            </div>

            <div class="batch-stats">
              <div class="stat-item">
                <span class="stat-label">完成进度</span>
                <span class="stat-value">
                  {{ batch.completedCount || 0 }}/{{ batch.totalCount || batch.words.length }}
                </span>
              </div>
              <div class="stat-item">
                <span class="stat-label">总耗时</span>
                <span class="stat-value">{{ formatDuration(batch.duration) }}</span>
              </div>
            </div>

            <div class="batch-actions">
              <el-button
                type="primary"
                size="small"
                text
                @click.stop="showDetail(batch)"
              >
                查看详情
              </el-button>
              <el-button
                type="danger"
                size="small"
                text
                @click.stop="handleDelete(batch)"
              >
                删除
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36, 48]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleFilter"
          @current-change="handleFilter"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="currentBatch?.name || '批次详情'"
      width="700px"
      class="detail-dialog"
    >
      <div class="detail-content" v-if="currentBatch">
        <div class="detail-header">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(currentBatch.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(currentBatch.status)">
                {{ getStatusText(currentBatch.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="总耗时">
              {{ formatDuration(currentBatch.duration) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider>词语列表</el-divider>

        <div class="word-list">
          <div
            v-for="(word, index) in currentBatch.wordDetails"
            :key="index"
            class="word-item"
          >
            <div class="word-index">{{ index + 1 }}</div>
            <div class="word-content">
              <span class="word-text">{{ word.word }}</span>
              <span class="word-time">耗时: {{ word.time }}秒</span>
            </div>
            <el-tag
              v-if="word.isCorrect"
              type="success"
              size="small"
            >
              正确
            </el-tag>
            <el-tag
              v-else-if="word.isCorrect === false"
              type="danger"
              size="small"
            >
              错误
            </el-tag>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleRestart">
          重新听写
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBatches, getBatchById, deleteBatch } from '@/api/batch'

const router = useRouter()

// 筛选条件
const dateRange = ref([])
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 列表数据
const loading = ref(false)
const batchList = ref([])

// 详情
const detailVisible = ref(false)
const currentBatch = ref(null)

// 模拟数据
const mockBatches = [
  {
    id: 1,
    name: '水果词语练习',
    createdAt: '2024-01-15 14:30:00',
    status: 'completed',
    words: ['苹果', '香蕉', '橘子', '葡萄', '西瓜', '草莓', '樱桃', '桃子'],
    completedCount: 8,
    totalCount: 8,
    duration: 320,
    wordDetails: [
      { word: '苹果', time: 15, isCorrect: true },
      { word: '香蕉', time: 22, isCorrect: true },
      { word: '橘子', time: 35, isCorrect: false },
      { word: '葡萄', time: 18, isCorrect: true },
      { word: '西瓜', time: 25, isCorrect: true },
      { word: '草莓', time: 40, isCorrect: false },
      { word: '樱桃', time: 30, isCorrect: true },
      { word: '桃子', time: 20, isCorrect: true }
    ]
  },
  {
    id: 2,
    name: '动物词语练习',
    createdAt: '2024-01-14 10:15:00',
    status: 'completed',
    words: ['狮子', '老虎', '大象', '长颈鹿', '斑马', '熊猫'],
    completedCount: 6,
    totalCount: 6,
    duration: 280,
    wordDetails: []
  },
  {
    id: 3,
    name: '颜色词语练习',
    createdAt: '2024-01-13 16:45:00',
    status: 'in_progress',
    words: ['红色', '蓝色', '绿色', '黄色', '紫色', '橙色'],
    completedCount: 3,
    totalCount: 6,
    duration: 150,
    wordDetails: []
  },
  {
    id: 4,
    name: '数字词语练习',
    createdAt: '2024-01-12 09:00:00',
    status: 'paused',
    words: ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十'],
    completedCount: 5,
    totalCount: 10,
    duration: 200,
    wordDetails: []
  }
]

// 获取批次列表
const fetchBatches = async () => {
  loading.value = true
  try {
    // 实际项目中调用API
    // const res = await getBatches({
    //   page: currentPage.value,
    //   pageSize: pageSize.value,
    //   startDate: dateRange.value?.[0],
    //   endDate: dateRange.value?.[1],
    //   status: statusFilter.value
    // })
    // batchList.value = res.data.list
    // total.value = res.data.total

    // 使用模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    batchList.value = mockBatches
    total.value = mockBatches.length
  } catch (error) {
    console.error('获取批次列表失败:', error)
    ElMessage.error('获取批次列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选
const handleFilter = () => {
  currentPage.value = 1
  fetchBatches()
}

// 重置筛选
const resetFilter = () => {
  dateRange.value = []
  statusFilter.value = ''
  currentPage.value = 1
  fetchBatches()
}

// 显示详情
const showDetail = async (batch) => {
  try {
    // 实际项目中调用API获取详情
    // const res = await getBatchById(batch.id)
    // currentBatch.value = res.data

    currentBatch.value = batch
    detailVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 删除批次
const handleDelete = async (batch) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除批次"${batch.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // await deleteBatch(batch.id)
    ElMessage.success('删除成功')
    fetchBatches()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 重新听写
const handleRestart = () => {
  if (currentBatch.value) {
    router.push({
      path: '/',
      query: { batchId: currentBatch.value.id }
    })
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    completed: 'success',
    in_progress: 'warning',
    paused: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    completed: '已完成',
    in_progress: '进行中',
    paused: '已暂停'
  }
  return texts[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}

onMounted(() => {
  fetchBatches()
})
</script>

<style lang="scss" scoped>
.history-page {
  .filter-card {
    margin-bottom: 24px;
    border-radius: 16px;

    .filter-form {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }
  }
}

.batch-list {
  min-height: 300px;
}

.batch-card {
  margin-bottom: 20px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);

    .batch-name {
      color: #409EFF;
    }
  }

  .batch-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .batch-name {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: bold;
      color: #303133;
    }
  }

  .batch-time {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: #909399;
    margin-bottom: 12px;
  }

  .batch-words {
    margin-bottom: 12px;

    .words-preview {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      align-items: center;

      .word-tag {
        font-size: 12px;
      }

      .more-tag {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .batch-stats {
    display: flex;
    justify-content: space-between;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 12px;

    .stat-item {
      text-align: center;

      .stat-label {
        font-size: 12px;
        color: #909399;
        display: block;
      }

      .stat-value {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }
    }
  }

  .batch-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding-top: 12px;
    border-top: 1px solid #EBEEF5;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.detail-dialog {
  .detail-content {
    .detail-header {
      margin-bottom: 20px;
    }

    .word-list {
      max-height: 400px;
      overflow-y: auto;

      .word-item {
        display: flex;
        align-items: center;
        padding: 12px;
        border-bottom: 1px solid #EBEEF5;

        &:last-child {
          border-bottom: none;
        }

        .word-index {
          width: 32px;
          height: 32px;
          background: #409EFF;
          color: white;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
          margin-right: 16px;
        }

        .word-content {
          flex: 1;
          display: flex;
          flex-direction: column;

          .word-text {
            font-size: 18px;
            font-weight: 500;
            color: #303133;
          }

          .word-time {
            font-size: 12px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .filter-form {
    :deep(.el-form-item) {
      width: 100%;
      margin-right: 0;
    }
  }
}
</style>