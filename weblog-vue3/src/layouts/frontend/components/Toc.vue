<template>
    <div v-if="titles.length > 0"
        class="sticky top-[5.5rem] text-sm/[30px] w-full p-5 mb-3 bg-white border border-gray-200 rounded-lg dark:bg-gray-800 dark:border-gray-700">
        <h2 class="flex items-center mb-2 font-bold text-gray-900 uppercase dark:text-white">
            <svg t="1699441758495" class="icon w-3.5 h-3.5 mr-2" viewBox="0 0 1024 1024" version="1.1"
                xmlns="http://www.w3.org/2000/svg" p-id="4043" width="200" height="200">
                <path
                    d="M857.6 25.6a76.8 76.8 0 0 1 76.8 76.8v819.2a76.8 76.8 0 0 1-76.8 76.8H166.4a76.8 76.8 0 0 1-76.8-76.8V102.4a76.8 76.8 0 0 1 76.8-76.8h691.2z m-102.4 678.4H473.6l-2.2528 0.064a38.4 38.4 0 0 0 0 76.672L473.6 780.8h281.6l2.2528-0.064a38.4 38.4 0 0 0 0-76.672L755.2 704z m0-230.4H473.6l-2.2528 0.064a38.4 38.4 0 0 0 0 76.672L473.6 550.4h281.6l2.2528-0.064a38.4 38.4 0 0 0 0-76.672L755.2 473.6z m0-230.4H473.6l-2.2528 0.064a38.4 38.4 0 0 0 0 76.672L473.6 320h281.6l2.2528-0.064a38.4 38.4 0 0 0 0-76.672L755.2 243.2z"
                    fill="#6B57FE" p-id="4044"></path>
                <path
                    d="M281.6 691.2a51.2 51.2 0 1 1 0 102.4 51.2 51.2 0 0 1 0-102.4z m0-230.4a51.2 51.2 0 1 1 0 102.4 51.2 51.2 0 0 1 0-102.4z m0-230.4a51.2 51.2 0 1 1 0 102.4 51.2 51.2 0 0 1 0-102.4z"
                    fill="#FFBA00" p-id="4045"></path>
            </svg>
            文章目录
        </h2>

        <div class="toc-wrapper" :class="[isDark ? 'dark' : '']">
            <ul class="toc">
                <li v-for="title in titles" :key="title.id">
                    <button type="button" class="toc-item hover:text-sky-600"
                        :class="[title.id === activeHeadingId ? 'active py-1 text-sky-600 border-l-2 border-sky-600 font-bold' : 'text-gray-500 font-normal']"
                        :style="{ paddingLeft: `${title.indent}px` }" @click="scrollToHeading(title.id)">
                        {{ title.text }}
                    </button>
                </li>
            </ul>
        </div>
    </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useDark } from '@vueuse/core'

const isDark = useDark({ disableTransition: false })

const titles = ref([])
const activeHeadingId = ref('')

let observer = null
let imageLoadCleanups = []
let refreshTimer = null

const headingSelector = 'h1, h2, h3, h4'
const scrollOffset = 95

const getArticleContainer = () => document.querySelector('.article-content')

const slugify = (text, index) => {
    const slug = text
        .trim()
        .toLowerCase()
        .replace(/\s+/g, '-')
        .replace(/[/?#\[\]@!$&'()*+,;=]/g, '')

    return `article-heading-${slug || index}`
}

const ensureUniqueHeadingId = (heading, index, usedIds) => {
    const baseId = heading.id || slugify(heading.innerText || heading.textContent || '', index)
    let uniqueId = baseId
    let count = 1

    while (usedIds.has(uniqueId)) {
        uniqueId = `${baseId}-${count}`
        count++
    }

    usedIds.add(uniqueId)
    heading.id = uniqueId
    return uniqueId
}

const getHeadingTop = (heading) => {
    return heading.getBoundingClientRect().top + window.scrollY - scrollOffset
}

const buildToc = () => {
    const container = getArticleContainer()
    if (!container) {
        titles.value = []
        activeHeadingId.value = ''
        return
    }

    const headings = Array.from(container.querySelectorAll(headingSelector))
        .filter((heading) => heading.innerText && heading.innerText.trim())

    const usedIds = new Set()
    titles.value = headings.map((heading, index) => {
        const level = Number(heading.tagName.substring(1))
        const id = ensureUniqueHeadingId(heading, index + 1, usedIds)

        return {
            id,
            text: heading.innerText.trim(),
            level,
            indent: Math.max(level - 1, 0) * 14,
            top: getHeadingTop(heading)
        }
    })

    updateActiveHeading()
    bindImageLoadRefresh(container)
}

const scheduleBuildToc = () => {
    if (refreshTimer) {
        clearTimeout(refreshTimer)
    }

    refreshTimer = setTimeout(() => {
        nextTick(buildToc)
    }, 80)
}

const bindImageLoadRefresh = (container) => {
    imageLoadCleanups.forEach((cleanup) => cleanup())
    imageLoadCleanups = []

    const images = Array.from(container.querySelectorAll('img'))
    images.forEach((img) => {
        const onLoad = () => scheduleBuildToc()
        img.addEventListener('load', onLoad)
        imageLoadCleanups.push(() => img.removeEventListener('load', onLoad))
    })
}

const updateActiveHeading = () => {
    if (!titles.value.length) {
        activeHeadingId.value = ''
        return
    }

    const currentY = window.scrollY + scrollOffset + 5
    let currentTitle = titles.value[0]

    for (const title of titles.value) {
        const heading = document.getElementById(title.id)
        const top = heading ? getHeadingTop(heading) : title.top
        title.top = top

        if (currentY >= top) {
            currentTitle = title
        } else {
            break
        }
    }

    activeHeadingId.value = currentTitle.id
}

const scrollToHeading = (id) => {
    const heading = document.getElementById(id)
    if (!heading) {
        return
    }

    window.scrollTo({
        top: getHeadingTop(heading),
        behavior: 'smooth'
    })

    activeHeadingId.value = id
}

const initObserver = () => {
    const container = getArticleContainer()
    if (!container) {
        scheduleBuildToc()
        return
    }

    observer = new MutationObserver(scheduleBuildToc)
    observer.observe(container, { childList: true, subtree: true })
    buildToc()
}

onMounted(() => {
    nextTick(initObserver)
    window.addEventListener('scroll', updateActiveHeading, { passive: true })
    window.addEventListener('resize', scheduleBuildToc)
})

onBeforeUnmount(() => {
    observer?.disconnect()
    imageLoadCleanups.forEach((cleanup) => cleanup())
    window.removeEventListener('scroll', updateActiveHeading)
    window.removeEventListener('resize', scheduleBuildToc)

    if (refreshTimer) {
        clearTimeout(refreshTimer)
    }
})
</script>

<style scoped>
::v-deep(.toc-wrapper) {
    position: relative;
    overflow-x: hidden;
    overflow-y: auto;
    max-height: 75vh;
    text-overflow: ellipsis;
    white-space: nowrap;
    scroll-behavior: smooth;
}

::v-deep(.toc:before) {
    content: " ";
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    z-index: -1;
    width: 2px;
    background: #eaecef;
}

.toc-item {
    display: block;
    width: 100%;
    text-align: left;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: pointer;
}

.toc-item.active {
    background: transparent;
}

::v-deep(.dark .toc:before) {
    content: " ";
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    z-index: -1;
    width: 2px;
    background: #30363d;
}

::v-deep(.dark .toc li .toc-item) {
    color: #9e9e9e;
}

::v-deep(.dark .toc li .active) {
    color: rgb(2 132 199 / 1);
}

::v-deep(.dark .toc li .toc-item:hover) {
    color: rgb(2 132 199 / 1);
}
</style>
