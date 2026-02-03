# 本地漫画索引缓存与同步（首版）

## 目标与范围
- 缓存粒度：漫画 + 章节 + 页（全量索引）
- 同步策略：Android 实时监听；iOS 轮询/手动刷新
- 存储建议：SQLDelight（跨平台持久化）

## 索引表结构
### Comics
- `id`：主键（可使用路径/URI 哈希）
- `title`：漫画标题
- `root_uri`：漫画根目录 URI/路径
- `cover_page_uri`：封面页 URI/路径（可选）
- `updated_at`：最近扫描时间戳
- `sort_key`：自然排序键（用于列表稳定排序）

### Chapters
- `id`：主键
- `comic_id`：外键（Comics.id）
- `title`：章节标题
- `chapter_uri`：章节目录/CBZ URI
- `updated_at`：最近扫描时间戳
- `sort_key`：自然排序键

### Pages
- `id`：主键
- `chapter_id`：外键（Chapters.id）
- `file_name`：页文件名
- `page_index`：页序号
- `page_uri`：页 URI/路径
- `width`、`height`：可选尺寸缓存

### 约束与去重
- `Comics.root_uri` 唯一
- `Chapters.chapter_uri` 唯一
- `Pages.page_uri` 唯一
- 删除漫画/章节时级联清理下层数据

## 扫描与缓存更新策略
### 初始扫描
- 从本地库入口或应用启动触发
- 构建漫画 -> 章节 -> 页的全量索引
- 扫描结束后一次性提交索引变更，避免 UI 读到半成品

### 增量更新
- 基于路径/URI 进行增删改识别
- 目录/CBZ 更新时间变化 -> 重建对应章节索引
- 新增/删除 -> 按 URI 对比增删索引记录
- `sort_key` 与 `page_index` 始终基于自然排序生成

## 平台同步策略
### Android（实时监听）
- SAF 授权目录后使用 DocumentFile 进行遍历
- 结合 FileObserver/ContentObserver 监听变更
- 监听到变更后触发增量扫描（仅更新受影响漫画/章节）

### iOS（轮询/手动刷新）
- 每次进入本地库触发增量扫描
- 提供“刷新”按钮执行全量扫描
- 仅处理沙盒内导入内容，不扫描系统目录

## 实现落点
- `data/local`：扫描器、索引仓库、SQLDelight 表与 DAO
- `domain/comic`：漫画/章节/页实体与用例
- `feature/local-library`：展示与刷新入口
