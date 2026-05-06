/**
 * AI 写作辅助 API
 * 使用 fetch + ReadableStream 实现 SSE 流式调用，实时接收后端生成的文章草稿。
 */

/**
 * 流式调用 AI 草稿生成接口
 *
 * @param {Object} params - 请求参数
 * @param {string} params.title - 文章标题（必填）
 * @param {string} [params.categoryName] - 文章分类名称
 * @param {string[]} [params.tagNames] - 文章标签列表
 * @param {function} onChunk - 每收到一段文本时的回调函数，参数为该段文本
 * @param {AbortSignal} [signal] - 用于中断请求的 AbortSignal
 * @returns {Promise<void>} 流式读取完成后 resolve
 */
export async function generateDraftStream({ title, categoryName, tagNames }, onChunk, signal) {
  const token = localStorage.getItem('token')

  const response = await fetch('/api/ai/generate-draft', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify({ title, categoryName, tagNames }),
    signal
  })

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(`请求失败 (${response.status}): ${errorText}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  // 缓冲区：处理跨 read() 边界的不完整行
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      // 将新数据追加到缓冲区
      buffer += decoder.decode(value, { stream: true })

      // 按行分割，最后一行可能不完整，留在缓冲区等下次处理
      const lines = buffer.split('\n')
      // 最后一段可能是不完整的行，保留在缓冲区
      buffer = lines.pop()

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || !trimmed.startsWith('data:')) continue

        const data = trimmed.slice(5).trim()

        // 流结束标记
        if (data === '[DONE]') return

        // 尝试从 JSON 中提取 content 字段
        // Spring AI + ServerSentEvent 可能返回纯文本或 JSON
        const content = extractContent(data)
        if (content) {
          onChunk(content)
        }
      }
    }

    // 处理缓冲区中剩余的数据
    if (buffer.trim()) {
      const remaining = buffer.trim()
      if (remaining.startsWith('data:')) {
        const data = remaining.slice(5).trim()
        if (data !== '[DONE]') {
          const content = extractContent(data)
          if (content) onChunk(content)
        }
      }
    }
  } finally {
    reader.releaseLock()
  }
}

/**
 * 从 SSE data 字段中提取文本内容
 * 兼容两种格式：
 *   1. 纯文本（ServerSentEvent 直接传文本）
 *   2. JSON 格式 {"choices":[{"delta":{"content":"..."}}]}
 *
 * @param {string} data - data: 后面的原始字符串
 * @returns {string|null} 提取到的文本内容，无效则返回 null
 */
function extractContent(data) {
  if (!data) return null

  // 尝试 JSON 解析（兼容 OpenAI 格式）
  try {
    const json = JSON.parse(data)
    // OpenAI 流式格式: {"choices":[{"delta":{"content":"文本"}}]}
    if (json.choices && json.choices[0]?.delta?.content) {
      return json.choices[0].delta.content
    }
    // 如果有 content 字段但不在 delta 中
    if (json.content) {
      return json.content
    }
    // JSON 但没有可识别的内容字段，跳过
    return null
  } catch {
    // 不是 JSON，视为纯文本直接返回
    return data
  }
}
