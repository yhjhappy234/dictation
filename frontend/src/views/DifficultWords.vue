<template>
  <div class="difficult-words-page">
    <!-- 操作栏 -->
    <el-card class="action-bar" shadow="hover">
      <div class="action-content">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索词语"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>

        <div class="action-buttons">
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出生词本
          </el-button>
          <el-button type="primary" @click="handlePractice">
            <el-icon><Edit /></el-icon>
            针对性练习
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon><Collection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">生词总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon mastered">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.mastered }}</div>
            <div class="stat-label">已掌握</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon learning">
            <el-icon><Loading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.learning }}</div>
            <div class="stat-label">学习中</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon difficult">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.difficult }}</div>
            <div class="stat-label">困难词</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 生词表格 -->
    <el-card class="table-card" shadow="hover">
      <el-table
        :data="wordList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column prop="word" label="词语" width="150">
          <template #default="{ row }">
            <span class="word-text">{{ row.word }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="errorCount" label="错误次数" width="120" sortable>
          <template #default="{ row }">
            <el-tag :type="row.errorCount > 3 ? 'danger' : 'warning'">
              {{ row.errorCount }} 次
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="avgTime" label="平均耗时" width="120" sortable>
          <template #default="{ row }">
            {{ row.avgTime }} 秒
          </template>
        </el-table-column>

        <el-table-column prop="mastery" label="掌握度" width="200">
          <template #default="{ row }">
            <div class="mastery-cell">
              <el-rate
                v-model="row.mastery"
                :max="5"
                :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                @change="(val) => handleMasteryChange(row, val)"
              />
              <span class="mastery-text">{{ getMasteryText(row.mastery) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="lastPractice" label="最后练习" width="180">
          <template #default="{ row }">
            {{ formatDate(row.lastPractice) }}
          </template>
        </el-table-column>

        <el-table-column prop="remark" label="备注" min-width="150">
          <template #default="{ row }">
            <el-input
              v-if="row.editing"
              v-model="row.remark"
              size="small"
              @blur="row.editing = false"
              @keyup.enter="row.editing = false"
            />
            <span v-else class="remark-text" @click="row.editing = true">
              {{ row.remark || '点击添加备注' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="handlePracticeWord(row)">
              练习
            </el-button>
            <el-button type="danger" size="small" text @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchWordList"
          @current-change="fetchWordList"
        />
      </div>
    </el-card>

    <!-- 选择练习词语弹窗 -->
    <el-dialog
      v-model="practiceDialogVisible"
      title="选择练习词语"
      width="500px"
    >
      <div class="practice-dialog">
        <p>请选择要练习的词语范围：</p>
        <el-radio-group v-model="practiceMode">
          <el-radio value="selected">已选中的词语 ({{ selectedWords.length }} 个)</el-radio>
          <el-radio value="difficult">困难词语 (掌握度低于3星)</el-radio>
          <el-radio value="all">全部生词</el-radio>
        </el-radio-group>

        <div class="selected-preview" v-if="practiceMode === 'selected' && selectedWords.length > 0">
          <el-tag
            v-for="word in selectedWords.slice(0, 10)"
            :key="word.id"
            class="preview-tag"
          >
            {{ word.word }}
          </el-tag>
          <span v-if="selectedWords.length > 10" class="more-text">
            等 {{ selectedWords.length }} 个词语
          </span>
        </div>
      </div>

      <template #footer>
        <el-button @click="practiceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="startPractice">开始练习</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDifficultWords,
  updateDifficultWord,
  deleteDifficultWord,
  updateMastery,
  exportDifficultWords
} from '@/api/difficultWord'

const router = useRouter()

// 搜索和筛选
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 统计数据
const stats = reactive({
  total: 25,
  mastered: 8,
  learning: 12,
  difficult: 5
})

// 生词列表
const wordList = ref([])
const selectedWords = ref([])

// 练习弹窗
const practiceDialogVisible = ref(false)
const practiceMode = ref('difficult')

// 模拟数据
const mockWords = [
  { id: 1, word: '葡萄', errorCount: 5, avgTime: 35, mastery: 1, lastPractice: '2024-01-15', remark: '容易写成"葡桃"' },
  { id: 2, word: '樱桃', errorCount: 4, avgTime: 30, mastery: 2, lastPractice: '2024-01-14', remark: '' },
  { id: 3, word: '草莓', errorCount: 3, avgTime: 28, mastery: 2, lastPractice: '2024-01-13', remark: '' },
  { id: 4, word: '橘子', errorCount: 2, avgTime: 25, mastery: 3, lastPractice: '2024-01-12', remark: '' },
  { id: 5, word: '苹果', errorCount: 1, avgTime: 15, mastery: 4, lastPractice: '2024-01-11', remark: '已经掌握' },
  { id: 6, word: '西瓜', errorCount: 1, avgTime: 12, mastery: 5, lastPractice: '2024-01-10', remark: '' },
  { id: 7, word: '香蕉', errorCount: 2, avgTime: 20, mastery: 3, lastPractice: '2024-01-09', remark: '' },
  { id: 8, word: '长颈鹿', errorCount: 6, avgTime: 40, mastery: 1, lastPractice: '2024-01-08', remark: '拼音容易错' },
  { id: 9, word: '斑马', errorCount: 3, avgTime: 22, mastery: 3, lastPractice: '2024-01-07', remark: '' },
  { id: 10, word: '熊猫', errorCount: 4, avgTime: 32, mastery: 2, lastPractice: '2024-01-06', remark: '' }
]

// 获取生词列表
const fetchWordList = async () => {
  loading.value = true
  try {
    // 实际项目中调用API
    // const res = await getDifficultWords({
    //   keyword: searchKeyword.value,
    //   page: currentPage.value,
    //   pageSize: pageSize.value
    // })
    // wordList.value = res.data.list
    // total.value = res.data.total

    await new Promise(resolve => setTimeout(resolve, 500))
    wordList.value = mockWords.map(w => ({ ...w, editing: false }))
    total.value = mockWords.length
  } catch (error) {
    console.error('获取生词列表失败:', error)
    ElMessage.error('获取生词列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchWordList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedWords.value = selection
}

// 更改掌握度
const handleMasteryChange = async (row, value) => {
  try {
    // await updateMastery(row.id, value)
    ElMessage.success('掌握度已更新')
  } catch (error) {
    console.error('更新掌握度失败:', error)
    ElMessage.error('更新失败')
  }
}

// 练习单个词语
const handlePracticeWord = (row) => {
  router.push({
    path: '/',
    query: { words: row.word }
  })
}

// 删除生词
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要从生词本中删除"${row.word}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // await deleteDifficultWord(row.id)
    ElMessage.success('删除成功')
    fetchWordList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 导出生词本
const handleExport = async () => {
  try {
    // const blob = await exportDifficultWords('txt')
    // 创建下载链接
    const content = wordList.value.map(w => w.word).join('\n')
    const blob = new Blob([content], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `生词本_${new Date().toISOString().split('T')[0]}.txt`
    a.click()
    URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 针对性练习
const handlePractice = () => {
  practiceDialogVisible.value = true
}

// 开始练习
const startPractice = () => {
  let words = []

  if (practiceMode.value === 'selected') {
    if (selectedWords.value.length === 0) {
      ElMessage.warning('请先选择要练习的词语')
      return
    }
    words = selectedWords.value.map(w => w.word)
  } else if (practiceMode.value === 'difficult') {
    words = wordList.value.filter(w => w.mastery < 3).map(w => w.word)
    if (words.length === 0) {
      ElMessage.info('没有困难词语，继续加油！')
      return
    }
  } else {
    words = wordList.value.map(w => w.word)
  }

  practiceDialogVisible.value = false
  router.push({
    path: '/',
    query: { words: words.join(' ') }
  })
}

// 获取掌握度文本
const getMasteryText = (mastery) => {
  const texts = ['', '不会', '困难', '一般', '熟悉', '掌握']
  return texts[mastery] || ''
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr
}

onMounted(() => {
  fetchWordList()
})
</script>

<style lang="scss" scoped>
.difficult-words-page {
  .action-bar {
    margin-bottom: 20px;
    border-radius: 16px;

    .action-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;

      .search-box {
        display: flex;
        gap: 10px;
        flex: 1;
        max-width: 400px;

        :deep(.el-input) {
          flex: 1;
        }
      }

      .action-buttons {
        display: flex;
        gap: 10px;
      }
    }
  }
}

.stats-row {
  margin-bottom: 20px;

  .stat-card {
    background: white;
    border-radius: 16px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: white;

      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.mastered {
        background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
      }

      &.learning {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.difficult {
        background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
        color: #F56C6C;
      }
    }

    .stat-info {
      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

.table-card {
  border-radius: 16px;

  .word-text {
    font-size: 18px;
    font-weight: 500;
  }

  .mastery-cell {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .mastery-text {
      font-size: 12px;
      color: #909399;
    }
  }

  .remark-text {
    color: #606266;
    cursor: pointer;

    &:hover {
      color: #409EFF;
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.practice-dialog {
  .el-radio-group {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-top: 16px;
  }

  .selected-preview {
    margin-top: 16px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;

    .preview-tag {
      margin: 4px;
    }

    .more-text {
      color: #909399;
      font-size: 14px;
    }
  }
}

@media (max-width: 768px) {
  .action-bar {
    .action-content {
      flex-direction: column;

      .search-box {
        max-width: 100%;
      }

      .action-buttons {
        width: 100%;
        justify-content: center;
      }
    }
  }

  .stats-row {
    .stat-card {
      margin-bottom: 10px;
    }
  }
}
</style>