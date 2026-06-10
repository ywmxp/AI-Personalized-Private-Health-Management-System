/** 健康知识条目 */
export interface KnowledgeItem {
  knowledgeId: number
  title: string
  content: string
  relateTag: string
  source: string
  createTime: string
}

/** 知识推送记录 */
export interface KnowledgePush {
  pushId: number
  knowledgeId: number
  title: string
  relateTag: string
  pushTime: string
  isRead: number // 0=未读, 1=已读
}

/** 分页数据 */
export interface PageData<T> {
  items: T[]
  page: number
  size: number
  total: number
}
