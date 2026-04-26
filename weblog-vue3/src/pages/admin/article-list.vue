<template>
    <div>
        <!-- 表头分页查询条件， shadow="never" 指定 card 卡片组件没有阴影 -->
        <el-card shadow="never" class="mb-5">
            <!-- flex 布局，内容垂直居中 -->
            <div class="flex items-center">
                <el-text>文章标题</el-text>
                <div class="ml-3 w-52 mr-5"><el-input v-model="searchArticleTitle" placeholder="请输入（模糊查询）" /></div>

                <el-text>创建日期</el-text>
                <div class="ml-3 w-30 mr-5">
                    <!-- 日期选择组件（区间选择） -->
                    <el-date-picker v-model="pickDate" type="daterange" range-separator="至" start-placeholder="开始时间"
                        end-placeholder="结束时间" size="default" :shortcuts="shortcuts" @change="datepickerChange" />
                </div>

                <el-button type="primary" class="ml-3" :icon="Search" @click="getTableData">查询</el-button>
                <el-button class="ml-3" :icon="RefreshRight" @click="reset">重置</el-button>
            </div>
        </el-card>

        <el-card shadow="never">
            <!-- 写文章按钮 -->
            <div class="mb-5">
                <el-button type="primary" @click="isArticlePublishEditorShow = true">
                    <el-icon class="mr-1">
                        <EditPen />
                    </el-icon>
                    写文章</el-button>
            </div>

            <!-- 分页列表 -->
            <el-table :data="tableData" border stripe size="small" class="article-admin-table" style="width: 100%" v-loading="tableLoading">
                <el-table-column prop="id" label="ID" width="60" align="center" />
                <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
                <el-table-column prop="cover" label="封面" width="105" align="center">
                    <template #default="scope">
                        <el-image style="width: 72px; height: 40px;" fit="cover" :src="scope.row.cover" />
                    </template>
                </el-table-column>
                <el-table-column prop="isPrivate" label="可见性" width="88" align="center">
                    <template #default="scope">
                        <el-button text :type="scope.row.isPrivate ? 'info' : 'success'" @click="handlePrivacyChange(scope.row)">
                            <el-icon class="mr-1">
                                <Hide v-if="scope.row.isPrivate" />
                                <View v-else />
                            </el-icon>
                            {{ scope.row.isPrivate ? '私密' : '公开' }}
                        </el-button>
                    </template>
                </el-table-column>
                <el-table-column prop="type" label="状态" width="82" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.type === 1 ? 'success' : 'info'">
                            {{ scope.row.type === 1 ? '已发布' : '草稿' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="150" />
                <el-table-column label="操作" width="210" fixed="right" align="center">
                    <template #default="scope">
                        <div class="article-table-actions">
                            <el-button size="small" @click="showArticleUpdateEditor(scope.row)">
                                <el-icon class="mr-1">
                                    <Edit />
                                </el-icon>
                                编辑</el-button>
                            <el-button size="small" @click="goArticleDetailPage(scope.row.id)">
                                <el-icon class="mr-1">
                                    <View />
                                </el-icon>
                                预览</el-button>
                            <el-button type="danger" size="small" @click="deleteArticleSubmit(scope.row)">
                                <el-icon class="mr-1">
                                    <Delete />
                                </el-icon>
                                删除
                            </el-button>
                        </div>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="mt-10 flex justify-center">
                <el-pagination v-model:current-page="current" v-model:page-size="size" :page-sizes="[10, 20, 50]"
                    :small="false" :background="true" layout="total, sizes, prev, pager, next, jumper" :total="total"
                    @size-change="handleSizeChange" @current-change="getTableData" />
            </div>

        </el-card>

        <!-- 写博客 -->
        <el-dialog v-model="isArticlePublishEditorShow" :fullscreen="true" :show-close="false"
            :close-on-press-escape="false">
            <template #header="{ close, titleId, titleClass }">
                <!-- 固钉组件，固钉到顶部 -->
                <el-affix :offset="20" style="width: 100%;">
                    <!-- 指定 flex 布局， 高度为 10， 背景色为白色 -->
                    <div class="flex h-10 bg-white">
                        <!-- 字体加粗 -->
                        <h4 class="font-bold">写文章</h4>
                        <!-- 靠右对齐 -->
                        <div class="ml-auto flex">
                            <el-button @click="isArticlePublishEditorShow = false">取消</el-button>
                            <el-button @click="saveDraftSubmit">
                                保存
                            </el-button>
                            <el-button type="primary" @click="publishArticleSubmit">
                                <el-icon class="mr-1">
                                    <Promotion />
                                </el-icon>
                                发布
                            </el-button>
                        </div>
                    </div>
                </el-affix>
            </template>
            <!-- label-position="top" 用于指定 label 元素在上面 -->
            <el-form :model="form" ref="publishArticleFormRef" label-position="top" size="large" :rules="rules">
                <el-form-item label="标题" prop="title">
                    <el-input v-model="form.title" autocomplete="off" size="large" maxlength="40" show-word-limit
                        clearable />
                </el-form-item>
                <el-form-item label="内容" prop="content">
                    <!-- Markdown 编辑器 -->
                    <div class="article-editor-with-toc">
                        <div class="article-md-editor" @paste.capture="handlePublishEditorPaste">
                            <MdEditor v-model="form.content" @onUploadImg="onUploadImg" editorId="publishArticleEditor" />
                        </div>
                        <div class="admin-editor-toc">
                            <div class="admin-editor-toc-title">文章目录</div>
                            <div v-if="publishEditorToc.length > 0" class="admin-editor-toc-list">
                                <button v-for="item in publishEditorToc" :key="item.lineIndex" type="button"
                                    class="admin-editor-toc-item" :style="{ paddingLeft: `${item.indent}px` }"
                                    @click="jumpToMarkdownHeading('publishArticleEditor', item.lineIndex)">
                                    {{ item.text }}
                                </button>
                            </div>
                            <div v-else class="admin-editor-toc-empty">暂无目录，请在正文中添加标题</div>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="封面" prop="cover">
                    <el-upload class="avatar-uploader" action="#" :on-change="handleCoverChange" :auto-upload="false"
                        :show-file-list="false">
                        <img v-if="form.cover" :src="form.cover" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon">
                            <Plus />
                        </el-icon>
                    </el-upload>
                </el-form-item>
                <el-form-item label="摘要" prop="summary">
                    <!-- :rows="3" 指定 textarea 默认显示 3 行 -->
                    <el-input v-model="form.summary" :rows="3" type="textarea" placeholder="请输入文章摘要" />
                </el-form-item>
                <el-form-item label="是否置顶">
                    <el-switch v-model="form.isTop" inline-prompt active-text="置顶" inactive-text="不置顶" />
                </el-form-item>
                <el-form-item label="分类" prop="categoryId">
                    <el-select v-model="form.categoryId" clearable placeholder="---请选择---" size="large">
                        <el-option v-for="item in categories" :key="item.value" :label="item.label"
                            :value="item.value" />
                    </el-select>
                </el-form-item>
                <el-form-item label="标签" prop="tags">
                    <span class="w-60">
                        <!-- 标签选择 -->
                        <el-select v-model="form.tags" multiple filterable remote reserve-keyword placeholder="请输入文章标签"
                            remote-show-suffix allow-create default-first-option :remote-method="remoteMethod"
                            :loading="tagSelectLoading" size="large">
                            <el-option v-for="item in tags" :key="item.value" :label="item.label" :value="item.value" />
                        </el-select>
                    </span>
                </el-form-item>
            </el-form>
        </el-dialog>

        <!-- 编辑博客 -->
        <el-dialog v-model="isArticleUpdateEditorShow" :fullscreen="true" :show-close="false"
            :close-on-press-escape="false">
            <template #header="{ close, titleId, titleClass }">
                <!-- 固钉组件，固钉到顶部 -->
                <el-affix :offset="20" style="width: 100%;">
                    <!-- 指定 flex 布局， 高度为 10， 背景色为白色 -->
                    <div class="flex h-10 bg-white">
                        <!-- 字体加粗 -->
                        <h4 class="font-bold">编辑文章</h4>
                        <!-- 靠右对齐 -->
                        <div class="ml-auto flex">
                            <el-button @click="isArticleUpdateEditorShow = false">取消</el-button>
                            <el-button @click="updateDraftSubmit">
                                保存
                            </el-button>
                            <el-button type="primary" @click="updateSubmit">
                                <el-icon class="mr-1">
                                    <Promotion />
                                </el-icon>
                                发布
                            </el-button>
                        </div>
                    </div>
                </el-affix>
            </template>
            <!-- label-position="top" 用于指定 label 元素在上面 -->
            <el-form :model="updateArticleForm" ref="updateArticleFormRef" label-position="top" size="large"
                :rules="rules">
                <el-form-item label="标题" prop="title">
                    <el-input v-model="updateArticleForm.title" autocomplete="off" size="large" maxlength="40"
                        show-word-limit clearable />
                </el-form-item>
                <el-form-item label="内容" prop="content">
                    <!-- Markdown 编辑器 -->
                    <div class="article-editor-with-toc">
                        <div class="article-md-editor" @paste.capture="handleUpdateEditorPaste">
                            <MdEditor v-model="updateArticleForm.content" @onUploadImg="onUploadImg"
                                editorId="updateArticleEditor" />
                        </div>
                        <div class="admin-editor-toc">
                            <div class="admin-editor-toc-title">文章目录</div>
                            <div v-if="updateEditorToc.length > 0" class="admin-editor-toc-list">
                                <button v-for="item in updateEditorToc" :key="item.lineIndex" type="button"
                                    class="admin-editor-toc-item" :style="{ paddingLeft: `${item.indent}px` }"
                                    @click="jumpToMarkdownHeading('updateArticleEditor', item.lineIndex)">
                                    {{ item.text }}
                                </button>
                            </div>
                            <div v-else class="admin-editor-toc-empty">暂无目录，请在正文中添加标题</div>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="封面" prop="cover">
                    <el-upload class="avatar-uploader" action="#" :on-change="handleUpdateCoverChange"
                        :auto-upload="false" :show-file-list="false">
                        <img v-if="updateArticleForm.cover" :src="updateArticleForm.cover" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon">
                            <Plus />
                        </el-icon>
                    </el-upload>
                </el-form-item>
                <el-form-item label="摘要" prop="summary">
                    <!-- :rows="3" 指定 textarea 默认显示 3 行 -->
                    <el-input v-model="updateArticleForm.summary" :rows="3" type="textarea" placeholder="请输入文章摘要" />
                </el-form-item>
                <el-form-item label="是否置顶">
                    <el-switch v-model="updateArticleForm.isTop" inline-prompt active-text="置顶" inactive-text="不置顶" />
                </el-form-item>
                <el-form-item label="分类" prop="categoryId">
                    <el-select v-model="updateArticleForm.categoryId" clearable placeholder="---请选择---" size="large">
                        <el-option v-for="item in categories" :key="item.value" :label="item.label"
                            :value="item.value" />
                    </el-select>
                </el-form-item>
                <el-form-item label="标签" prop="tags">
                    <span class="w-60">
                        <!-- 标签选择 -->
                        <el-select v-model="updateArticleForm.tags" multiple filterable remote reserve-keyword
                            placeholder="请输入文章标签" remote-show-suffix allow-create default-first-option
                            :remote-method="remoteMethod" :loading="tagSelectLoading" size="large">
                            <el-option v-for="item in tags" :key="item.value" :label="item.label" :value="item.value" />
                        </el-select>
                    </span>
                </el-form-item>
            </el-form>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, nextTick, computed } from 'vue'
import { Search, RefreshRight, View, Hide } from '@element-plus/icons-vue'
import { getArticlePageList, deleteArticle, publishArticle, getArticleDetail, updateArticle, updateArticlePrivacy } from '@/api/admin/article'
import { uploadFile } from '@/api/admin/file'
import { getCategorySelectList } from '@/api/admin/category'
import { searchTags, getTagSelectList } from '@/api/admin/tag'
import moment from 'moment'
import { showMessage, showModel } from '@/composables/util'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useRouter } from 'vue-router'

const router = useRouter()

// 模糊搜索的文章标题
const searchArticleTitle = ref('')
// 日期
const pickDate = ref('')

// 查询条件：开始结束时间
const startDate = reactive({})
const endDate = reactive({})

// 监听日期组件改变事件，并将开始结束时间设置到变量中
const datepickerChange = (e) => {
    startDate.value = moment(e[0]).format('YYYY-MM-DD')
    endDate.value = moment(e[1]).format('YYYY-MM-DD')

    console.log('开始时间：' + startDate.value + ', 结束时间：' + endDate.value)
}

const shortcuts = [
    {
        text: '最近一周',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
        },
    },
    {
        text: '最近一个月',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
        },
    },
    {
        text: '最近三个月',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
            return [start, end]
        },
    },
]

// 重置
const reset = () => {
    pickDate.value = ''
    startDate.value = null
    endDate.value = null
    searchArticleTitle.value = ''
}

// 表格加载 Loading
const tableLoading = ref(false)
// 表格数据
const tableData = ref([])
// 当前页码，给了一个默认值 1
const current = ref(1)
// 总数据量，给了个默认值 0
const total = ref(0)
// 每页显示的数据量，给了个默认值 10
const size = ref(10)


// 获取分页数据
function getTableData() {
    // 显示表格 loading
    tableLoading.value = true
    // 调用后台分页接口，并传入所需参数
    getArticlePageList({ current: current.value, size: size.value, startDate: startDate.value, endDate: endDate.value, title: searchArticleTitle.value })
        .then((res) => {
            if (res.success == true) {
                tableData.value = res.data
                current.value = res.current
                size.value = res.size
                total.value = res.total
            }
        })
        .finally(() => tableLoading.value = false) // 隐藏表格 loading
}
getTableData()

// 每页展示数量变更事件
const handleSizeChange = (chooseSize) => {
    console.log('选择的页码' + chooseSize)
    size.value = chooseSize
    getTableData()
}

// 删除文章
const deleteArticleSubmit = (row) => {
    console.log(row)
    showModel('是否确定要删除该文章？').then(() => {
        deleteArticle(row.id).then((res) => {
            if (res.success == false) {
                // 获取服务端返回的错误消息
                let message = res.message
                // 提示错误消息
                showMessage(message, 'error')
                return
            }

            showMessage('删除成功')
            // 重新请求分页接口，渲染数据
            getTableData()
        })
    }).catch(() => {
        console.log('取消了')
    })
}

// 是否显示文章发布对话框
const isArticlePublishEditorShow = ref(false)
// 发布文章表单引用
const publishArticleFormRef = ref(null)

// 表单对象
const form = reactive({
    id: null,
    title: '',
    content: '请输入内容',
    cover: '',
    categoryId: null,
    tags: [],
    summary: "",
    isPublish: false,
    isPrivate: false,
    isTop: false
})

// 修改文章表单对象
const updateArticleForm = reactive({
    id: null,
    title: '',
    content: '请输入内容',
    cover: '',
    categoryId: null,
    tags: [],
    summary: "",
    isPublish: false,
    isPrivate: false,
    isTop: false
})

// 表单校验规则
const rules = {
    title: [
        { required: true, message: '请输入文章标题', trigger: 'blur' },
        { min: 1, max: 40, message: '文章标题要求大于1个字符，小于40个字符', trigger: 'blur' },
    ],
    content: [{ required: true }],
    cover: [{ required: true }],
    categoryId: [{ required: true, message: '请选择文章分类', trigger: 'blur' }],
    tags: [{ required: true, message: '请选择文章标签', trigger: 'blur' }],
}

const parseMarkdownToc = (content) => {
    if (!content) {
        return []
    }

    return content
        .split('\n')
        .map((line, lineIndex) => {
            const matched = line.match(/^(#{1,4})\s+(.+)$/)
            if (!matched) {
                return null
            }

            const level = matched[1].length
            const text = matched[2].replace(/#+\s*$/, '').trim()
            if (!text) {
                return null
            }

            return {
                level,
                text,
                lineIndex,
                indent: (level - 1) * 14
            }
        })
        .filter(Boolean)
}

const publishEditorToc = computed(() => parseMarkdownToc(form.content))
const updateEditorToc = computed(() => parseMarkdownToc(updateArticleForm.content))

const getCursorPositionByLineIndex = (content, lineIndex) => {
    if (lineIndex <= 0) {
        return 0
    }

    const lines = content.split('\n')
    return lines.slice(0, lineIndex).reduce((position, line) => position + line.length + 1, 0)
}

const jumpToMarkdownHeading = async (editorId, lineIndex) => {
    const targetForm = editorId === 'publishArticleEditor' ? form : updateArticleForm
    const textarea = getEditorTextarea(editorId)
    if (!textarea || typeof textarea.setSelectionRange !== 'function') {
        return
    }

    const position = getCursorPositionByLineIndex(targetForm.content || '', lineIndex)

    await nextTick()
    textarea.focus()
    textarea.setSelectionRange(position, position)

    const lineHeight = 24
    textarea.scrollTop = Math.max(lineIndex * lineHeight - textarea.clientHeight / 3, 0)
}

// 上传文章封面图片
const handleCoverChange = (file) => {
    // 表单对象
    let formData = new FormData()
    // 添加 file 字段，并将文件传入 
    formData.append('file', file.raw)
    uploadFile(formData).then((e) => {
        // 响参失败，提示错误消息
        if (e.success == false) {
            let message = e.message
            showMessage(message, 'error')
            return
        }

        // 成功则设置表单对象中的封面链接，并提示上传成功
        form.cover = e.data.url
        showMessage('上传成功')
    })
}

// 编辑文章：上传文章封面图片
const handleUpdateCoverChange = (file) => {
    // 表单对象
    let formData = new FormData()
    // 添加 file 字段，并将文件传入 
    formData.append('file', file.raw)
    uploadFile(formData).then((e) => {
        // 响参失败，提示错误消息
        if (e.success == false) {
            let message = e.message
            showMessage(message, 'error')
            return
        }

        // 成功则设置表单对象中的封面链接，并提示上传成功
        updateArticleForm.cover = e.data.url
        showMessage('上传成功')
    })
}

// 上传单个图片文件
const uploadImageFile = async (file) => {
    const formData = new FormData()
    formData.append('file', file)

    const res = await uploadFile(formData)
    if (res.success == false) {
        showMessage(res.message || '图片上传失败', 'error')
        return null
    }

    return res.data.url
}

// 编辑器工具栏图片上传
const onUploadImg = async (files, callback) => {
    try {
        const urls = await Promise.all(files.map(uploadImageFile))
        callback(urls.filter(Boolean))
    } catch (error) {
        console.error(error)
        showMessage('图片上传失败，请稍后重试', 'error')
    }
}

const getPasteImageFiles = (event) => {
    const items = Array.from(event.clipboardData?.items || [])
    return items
        .filter((item) => item.kind === 'file' && item.type.startsWith('image/'))
        .map((item) => item.getAsFile())
        .filter(Boolean)
}

const getEditorTextarea = (editorId) => {
    const editor = document.getElementById(editorId)
    return editor?.querySelector('textarea') || document.activeElement
}

const insertMarkdownAtCursor = async (targetForm, editorId, markdownText, selectionStart, selectionEnd) => {
    const content = targetForm.content || ''
    const start = typeof selectionStart === 'number' ? selectionStart : content.length
    const end = typeof selectionEnd === 'number' ? selectionEnd : start

    const prefix = start > 0 && !content.slice(0, start).endsWith('\n') ? '\n' : ''
    const suffix = content.slice(end).startsWith('\n') ? '' : '\n'
    const insertText = `${prefix}${markdownText}${suffix}`

    targetForm.content = content.slice(0, start) + insertText + content.slice(end)

    await nextTick()
    const textarea = getEditorTextarea(editorId)
    if (textarea && typeof textarea.focus === 'function') {
        const cursorPosition = start + insertText.length
        textarea.focus()
        if (typeof textarea.setSelectionRange === 'function') {
            textarea.setSelectionRange(cursorPosition, cursorPosition)
        }
    }
}

const handleEditorPasteImage = async (event, targetForm, editorId) => {
    const imageFiles = getPasteImageFiles(event)
    if (!imageFiles.length) {
        return
    }

    event.preventDefault()

    const textarea = getEditorTextarea(editorId)
    const selectionStart = textarea && typeof textarea.selectionStart === 'number'
        ? textarea.selectionStart
        : (targetForm.content || '').length
    const selectionEnd = textarea && typeof textarea.selectionEnd === 'number'
        ? textarea.selectionEnd
        : selectionStart

    try {
        showMessage('图片上传中...')
        const urls = (await Promise.all(imageFiles.map(uploadImageFile))).filter(Boolean)
        if (!urls.length) {
            return
        }

        const markdownText = urls.map((url) => `![图片](${url})`).join('\n')
        await insertMarkdownAtCursor(targetForm, editorId, markdownText, selectionStart, selectionEnd)
        showMessage('图片已插入')
    } catch (error) {
        console.error(error)
        showMessage('图片上传失败，请稍后重试', 'error')
    }
}

const handlePublishEditorPaste = (event) => {
    handleEditorPasteImage(event, form, 'publishArticleEditor')
}

const handleUpdateEditorPaste = (event) => {
    handleEditorPasteImage(event, updateArticleForm, 'updateArticleEditor')
}

// 文章分类
const categories = ref([])
getCategorySelectList().then((e) => {
    console.log('获取分类数据')
    categories.value = e.data
})

// 标签 select Loading 状态，默认不显示
const tagSelectLoading = ref(false)
// 文章标签
const tags = ref([])
// 渲染标签数据
getTagSelectList().then(res => {
    tags.value = res.data
})


// 根据用户输入的标签名称，远程模糊查询
const remoteMethod = (query) => {
    console.log('远程搜索：' + tags.value)
    // 如果用户的查询关键词不为空
    if (query) {
        // 显示 loading
        tagSelectLoading.value = true
        // 调用标签模糊查询接口
        searchTags(query).then((e) => {
            if (e.success) {
                // 设置到 tags 变量中
                tags.value = e.data
            }
        }).finally(() => tagSelectLoading.value = false) // 隐藏 loading
    }
}

const validateDraftFields = (formData, formRef, submit) => {
    if (!formData.title || formData.title.length < 1 || formData.title.length > 40) {
        formRef.value.validateField('title')
        return
    }

    if (!formData.content) {
        formRef.value.validateField('content')
        return
    }

    submit()
}

// 发布文章
const publishArticleSubmit = () => {
    form.isPublish = true
    // isArticlePublishEditorShow.value = true
    console.log('提交 md 内容：' + form.content)
    // 校验表单
    publishArticleFormRef.value.validate((valid) => {
        if (!valid) {
            return false
        }

        publishArticle(form).then((res) => {
            if (res.success == false) {
                // 获取服务端返回的错误消息
                let message = res.message
                // 提示错误消息
                showMessage(message, 'error')
                return
            }

            showMessage('发布成功')
            // 隐藏发布文章对话框
            isArticlePublishEditorShow.value = false
            // 将 form 表单字段置空
            form.title = ''
            form.content = ''
            form.cover = ''
            form.summary = ''
            form.categoryId = null
            form.tags = []
            form.isPublish = false
            form.isPrivate = false
            form.isTop = false
            // 重新请求分页接口，渲染列表数据
            getTableData()
        })
    })
}


// 保存草稿
const saveDraftSubmit = () => {
    form.isPublish = false
    validateDraftFields(form, publishArticleFormRef, () => {
        publishArticle(form).then((res) => {
            if (res.success == false) {
                let message = res.message
                showMessage(message, 'error')
                return
            }

            showMessage('保存成功')
            isArticlePublishEditorShow.value = false
            form.title = ''
            form.content = ''
            form.cover = ''
            form.summary = ''
            form.categoryId = null
            form.tags = []
            form.isPublish = false
            form.isPrivate = false
            form.isTop = false
            getTableData()
        })
    })
}

// 是否显示编辑文章对话框
const isArticleUpdateEditorShow = ref(false)
// 编辑文章表单引用
const updateArticleFormRef = ref(null)
// 编辑文章按钮点击事件
const showArticleUpdateEditor = (row) => {
    // 显示编辑文章对话框
    isArticleUpdateEditorShow.value = true
    // 拿到文章 ID
    let articleId = row.id
    getArticleDetail(articleId).then((res) => {
        if (res.success) {
            // 设置表单数据
            updateArticleForm.id = res.data.id
            updateArticleForm.title = res.data.title
            updateArticleForm.cover = res.data.cover
            updateArticleForm.content = res.data.content
            updateArticleForm.categoryId = res.data.categoryId
            updateArticleForm.tags = res.data.tagIds
            updateArticleForm.summary = res.data.summary
            updateArticleForm.isPublish = res.data.type === 1
            updateArticleForm.isPrivate = res.data.isPrivate
            updateArticleForm.isTop = res.data.isTop
        }
    })
}

// 发布文章
const updateSubmit = () => {
    updateArticleForm.isPublish = true
    console.log('tijiao')
    updateArticleFormRef.value.validate((valid) => {
        // 校验表单
        if (!valid) {
            return false
        }

        // 请求更新文章接口
        updateArticle(updateArticleForm).then((res) => {
            if (res.success == false) {
                // 获取服务端返回的错误消息
                let message = res.message
                // 提示错误消息
                showMessage(message, 'error')
                return
            }

            showMessage('发布成功')
            // 隐藏编辑框
            isArticleUpdateEditorShow.value = false
            // 重新请求分页接口，渲染列表数据
            getTableData()
        })
    })
}


// 保存文章草稿
const updateDraftSubmit = () => {
    updateArticleForm.isPublish = false
    validateDraftFields(updateArticleForm, updateArticleFormRef, () => {
        updateArticle(updateArticleForm).then((res) => {
            if (res.success == false) {
                let message = res.message
                showMessage(message, 'error')
                return
            }

            showMessage('保存成功')
            isArticleUpdateEditorShow.value = false
            getTableData()
        })
    })
}

// 跳转文章详情页
const goArticleDetailPage = (articleId) => {
    router.push('/article/' + articleId)
}


// 点击公开/私密切换事件
const handlePrivacyChange = (row) => {
    updateArticlePrivacy({ id: row.id, isPrivate: !row.isPrivate }).then((res) => {
        if (res.success == false) {
            let message = res.message
            showMessage(message, 'error')
            return
        }

        row.isPrivate = !row.isPrivate
        showMessage(row.isPrivate ? '已设为私密' : '已设为公开')
    })
}

</script>

<style scoped>
.article-table-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    white-space: nowrap;
}

.article-table-actions .el-button + .el-button {
    margin-left: 0;
}

/* 封面图片样式 */
.avatar-uploader .avatar {
    width: 200px;
    height: 100px;
    display: block;
}

.el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 200px;
    height: 100px;
    text-align: center;
}

/* 指定 select 下拉框宽度 */
.el-select--large {
    width: 600px;
}

.article-editor-with-toc {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 240px;
    gap: 16px;
    width: 100%;
}

.article-md-editor {
    min-width: 0;
}

.admin-editor-toc {
    position: sticky;
    top: 74px;
    align-self: flex-start;
    max-height: 620px;
    padding: 14px 12px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #fff;
}

.admin-editor-toc-title {
    margin-bottom: 10px;
    color: #111827;
    font-size: 14px;
    font-weight: 700;
}

.admin-editor-toc-list {
    max-height: 560px;
    overflow-y: auto;
}

.admin-editor-toc-item {
    display: block;
    width: 100%;
    padding-top: 4px;
    padding-bottom: 4px;
    color: #6b7280;
    font-size: 13px;
    line-height: 22px;
    text-align: left;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: pointer;
}

.admin-editor-toc-item:hover {
    color: #0284c7;
}

.admin-editor-toc-empty {
    color: #9ca3af;
    font-size: 13px;
    line-height: 22px;
}

@media (max-width: 1024px) {
    .article-editor-with-toc {
        grid-template-columns: 1fr;
    }

    .admin-editor-toc {
        position: static;
        max-height: 240px;
    }

    .admin-editor-toc-list {
        max-height: 180px;
    }
}
</style>

<style>
.md-editor-footer {
    height: 40px;
}
</style>