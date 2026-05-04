-- ============================================
-- Blog System 一体化初始化脚本（审核/通知增强版）
-- 一次执行本文件即可完成：建库、建表、插入完整示例数据
-- MySQL 8.0+
-- ============================================

DROP DATABASE IF EXISTS blog_db;
CREATE DATABASE blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog_db;

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `nickname` VARCHAR(50),
    `avatar` VARCHAR(500) DEFAULT '/default-avatar.png',
    `bio` VARCHAR(500),
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '0-普通用户 1-管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分类表
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255),
    `sort_order` INT DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 标签表
CREATE TABLE `tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章表
CREATE TABLE `article` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` MEDIUMTEXT NOT NULL,
    `summary` VARCHAR(500),
    `cover_image` VARCHAR(500),
    `author_id` BIGINT NOT NULL,
    `category_id` BIGINT,
    `views` INT NOT NULL DEFAULT 0,
    `likes` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-草稿 1-已发布',
    `review_status` TINYINT DEFAULT NULL COMMENT '0-待审核 1-审核通过 2-审核不通过',
    `review_reason` VARCHAR(500) DEFAULT NULL,
    `reviewed_at` DATETIME DEFAULT NULL,
    `reviewed_by` BIGINT DEFAULT NULL,
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '0-否 1-置顶',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_author` (`author_id`),
    INDEX `idx_category` (`category_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_review_status` (`review_status`),
    INDEX `idx_created` (`created_at`),
    FOREIGN KEY (`author_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章标签关联表
CREATE TABLE `article_tag` (
    `article_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    PRIMARY KEY (`article_id`, `tag_id`),
    FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID，支持嵌套回复',
    `likes` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-隐藏 1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_article` (`article_id`),
    INDEX `idx_user` (`user_id`),
    FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`parent_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 访问统计表
CREATE TABLE `visit_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `article_id` BIGINT,
    `ip` VARCHAR(50),
    `user_agent` VARCHAR(500),
    `referer` VARCHAR(500),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_article` (`article_id`),
    INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统操作日志表
CREATE TABLE `sys_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT,
    `action` VARCHAR(100) NOT NULL,
    `detail` TEXT,
    `ip` VARCHAR(50),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user` (`user_id`),
    INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 留言/通知表
CREATE TABLE `message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL,
    `article_id` BIGINT DEFAULT NULL,
    `type` TINYINT NOT NULL DEFAULT 0 COMMENT '0-留言 1-文章审核通知',
    `title` VARCHAR(200) DEFAULT NULL,
    `nickname` VARCHAR(50) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `content` TEXT NOT NULL,
    `reply` TEXT,
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-未读/未回复 1-已读/已回复',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_message_user` (`user_id`),
    INDEX `idx_message_type` (`type`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================
-- 用户数据
-- 普通用户密码: 123456
-- 管理员密码: admin123
-- =====================
INSERT INTO `user` (`id`, `username`, `password`, `email`, `nickname`, `avatar`, `bio`, `role`, `status`, `created_at`) VALUES
(1, 'admin', '$2a$10$IMPNNLkFauSe6QAcd/sVAucqsRK14X5dR9BTgVnkHMJ5WrBYCCu/K', 'admin@blog.com', '管理员', '/default-avatar.png', '系统管理员账号', 1, 1, '2025-09-01 09:00:00'),
(2, 'zhangsan', '$2a$10$EqKcp1WFKs6IYXE6MEstjOi0kkHMGKOTz2ALbTJHMZGOcYNG.ZxuS', 'zhangsan@gmail.com', '张三', '/default-avatar.png', '全栈工程师，热爱开源，专注于 Java 和 Vue 技术栈', 0, 1, '2025-09-15 10:30:00'),
(3, 'lisi', '$2a$10$EqKcp1WFKs6IYXE6MEstjOi0kkHMGKOTz2ALbTJHMZGOcYNG.ZxuS', 'lisi@outlook.com', '李四', '/default-avatar.png', '前端开发者，React/Vue 双修，偶尔写写技术博客', 0, 1, '2025-10-02 14:20:00'),
(4, 'wangwu', '$2a$10$EqKcp1WFKs6IYXE6MEstjOi0kkHMGKOTz2ALbTJHMZGOcYNG.ZxuS', 'wangwu@qq.com', '王五', '/default-avatar.png', '后端架构师，8年Java开发经验，擅长微服务和分布式系统', 0, 1, '2025-11-08 09:15:00'),
(5, 'xiaoming', '$2a$10$EqKcp1WFKs6IYXE6MEstjOi0kkHMGKOTz2ALbTJHMZGOcYNG.ZxuS', 'xiaoming@163.com', '小明同学', '/default-avatar.png', '计算机专业大三学生，正在学习 Spring Boot 和云原生技术', 0, 1, '2025-12-20 16:45:00'),
(6, 'lihua', '$2a$10$EqKcp1WFKs6IYXE6MEstjOi0kkHMGKOTz2ALbTJHMZGOcYNG.ZxuS', 'lihua@hotmail.com', '李华', '/default-avatar.png', '产品经理转开发，喜欢读书和写作，关注AI领域', 0, 1, '2026-01-10 11:00:00');
ALTER TABLE `user` AUTO_INCREMENT = 7;

-- =====================
-- 分类数据
-- =====================
INSERT INTO `category` (`id`, `name`, `description`, `sort_order`, `created_at`) VALUES
(1, '技术', '技术相关文章', 1, '2025-09-01 09:10:00'),
(2, '生活', '生活随笔', 2, '2025-09-01 09:11:00'),
(3, '读书', '读书笔记', 3, '2025-09-01 09:12:00'),
(4, '前端', '前端开发相关技术', 4, '2025-09-01 09:13:00'),
(5, '后端', '后端开发与架构', 5, '2025-09-01 09:14:00'),
(6, 'DevOps', '运维与持续集成', 6, '2025-09-01 09:15:00'),
(7, 'AI', '人工智能与机器学习', 7, '2025-09-01 09:16:00');
ALTER TABLE `category` AUTO_INCREMENT = 8;

-- =====================
-- 标签数据
-- =====================
INSERT INTO `tag` (`id`, `name`, `created_at`) VALUES
(1, 'Java', '2025-09-01 09:20:00'),
(2, 'Spring Boot', '2025-09-01 09:20:00'),
(3, 'Vue', '2025-09-01 09:20:00'),
(4, 'React', '2025-09-01 09:20:00'),
(5, 'MySQL', '2025-09-01 09:20:00'),
(6, 'Redis', '2025-09-01 09:20:00'),
(7, 'Docker', '2025-09-01 09:20:00'),
(8, 'Kubernetes', '2025-09-01 09:20:00'),
(9, 'TypeScript', '2025-09-01 09:20:00'),
(10, 'Python', '2025-09-01 09:20:00'),
(11, 'Git', '2025-09-01 09:20:00'),
(12, 'Linux', '2025-09-01 09:20:00'),
(13, '微服务', '2025-09-01 09:20:00'),
(14, '设计模式', '2025-09-01 09:20:00'),
(15, '算法', '2025-09-01 09:20:00'),
(16, 'ChatGPT', '2025-09-01 09:20:00'),
(17, '前端工程化', '2025-09-01 09:20:00'),
(18, '性能优化', '2025-09-01 09:20:00'),
(19, '安全', '2025-09-01 09:20:00'),
(20, '读书笔记', '2025-09-01 09:20:00');
ALTER TABLE `tag` AUTO_INCREMENT = 21;

-- =====================
-- 文章数据
-- 状态说明：status 0=草稿 1=已发布
-- review_status：0=待审核 1=审核通过 2=审核不通过
-- 覆盖：草稿 / 已发布待审核 / 审核通过 / 审核不通过
-- =====================
INSERT INTO `article` (`id`, `title`, `content`, `summary`, `author_id`, `category_id`, `views`, `likes`, `status`, `review_status`, `review_reason`, `reviewed_at`, `reviewed_by`, `is_top`, `created_at`) VALUES
(1, 'Spring Boot 3 + MyBatis-Plus 实战：从零搭建博客系统',
'## 前言\n\n最近花了两周时间，用 Spring Boot 3 和 MyBatis-Plus 搭建了一套完整的博客系统。这篇文章记录一下整个开发过程中的技术选型和踩坑经历。\n\n## 技术栈选择\n\n后端选择了 Spring Boot 3.2，主要考虑到它对 Java 17 的原生支持和 GraalVM 的兼容性。ORM 层用的 MyBatis-Plus，相比原生 MyBatis 省去了大量样板代码，内置的分页插件和条件构造器非常好用。\n\n## 项目结构\n\n```\nsrc/main/java/com/blog/\n├── config/          # 配置类\n├── controller/      # 控制器\n├── dto/             # 数据传输对象\n├── entity/          # 实体类\n├── mapper/          # MyBatis Mapper\n├── service/         # 业务逻辑\n├── security/        # 安全相关\n└── util/            # 工具类\n```\n\n## JWT 认证实现\n\n认证方案采用了 JWT + Spring Security 的组合。用户登录后签发 token，后续请求通过 Authorization 头携带 token，服务端通过自定义 Filter 解析并设置 SecurityContext。\n\n```java\n@Component\npublic class JwtAuthFilter extends OncePerRequestFilter {\n    @Override\n    protected void doFilterInternal(HttpServletRequest request,\n            HttpServletResponse response, FilterChain chain) {\n        String token = extractToken(request);\n        if (token != null && jwtUtil.validateToken(token)) {\n            // 设置认证信息\n        }\n        chain.doFilter(request, response);\n    }\n}\n```\n\n## 踩坑记录\n\n1. MyBatis-Plus 3.5.6 需要使用 `mybatis-plus-spring-boot3-starter` 而不是旧版的 starter\n2. Spring Security 6.x 的配置方式变化很大，`WebSecurityConfigurerAdapter` 已经废弃\n3. JWT 库推荐用 jjwt 0.12.x，API 和之前版本差异较大\n\n## 总结\n\n整体开发体验还是很流畅的，Spring Boot 3 的启动速度有明显提升。完整代码已开源，欢迎 Star。',
'用 Spring Boot 3 和 MyBatis-Plus 从零搭建博客系统的完整实战记录，包含 JWT 认证、分页查询、全局异常处理等核心功能的实现细节。',
2, 5, 2847, 186, 1, 1, NULL, '2026-02-10 18:00:00', 1, 1, '2026-02-10 09:00:00'),
(2, 'Vue 3 组合式 API 最佳实践总结',
'## 为什么选择组合式 API\n\n从 Vue 2 的选项式 API 迁移到 Vue 3 的组合式 API，最大的感受是代码组织方式的变化。以前按照 data、methods、computed 分类，现在可以按照功能逻辑聚合，可读性和复用性都好了很多。\n\n## 常用模式\n\n### 1. 使用 composables 封装业务逻辑\n\n```javascript\n// useArticles.js\nexport function useArticles() {\n  const articles = ref([])\n  const loading = ref(false)\n  const page = ref(1)\n\n  async function loadArticles(params) {\n    loading.value = true\n    try {\n      const res = await getArticles({ page: page.value, ...params })\n      articles.value = res.data.records\n    } finally {\n      loading.value = false\n    }\n  }\n\n  return { articles, loading, page, loadArticles }\n}\n```\n\n### 2. Pinia 状态管理\n\nPinia 比 Vuex 轻量很多，TypeScript 支持也更好。推荐用 setup store 的写法，和组合式 API 风格一致。\n\n### 3. 路由守卫配合 store\n\n```javascript\nrouter.beforeEach((to, from, next) => {\n  const userStore = useUserStore()\n  if (to.meta.requiresAuth && !userStore.isLoggedIn) {\n    next({ name: "Login" })\n  } else {\n    next()\n  }\n})\n```\n\n## 性能优化建议\n\n- 大列表使用虚拟滚动（vue-virtual-scroller）\n- 合理使用 `shallowRef` 和 `shallowReactive`\n- 图片懒加载配合 Intersection Observer\n- 路由组件全部使用动态 import 实现代码分割\n\n## 小结\n\n组合式 API 的学习曲线确实比选项式陡一些，但一旦习惯了，开发效率会有质的提升。',
'总结 Vue 3 组合式 API 的常用开发模式，包括 composables 封装、Pinia 状态管理、路由守卫等最佳实践。',
3, 4, 1923, 134, 1, 1, NULL, '2026-02-15 20:00:00', 1, 0, '2026-02-15 14:30:00'),
(3, 'MySQL 慢查询排查与索引优化实战',
'## 背景\n\n上周线上环境出现了几次接口超时告警，排查后发现是几条 SQL 查询耗时过长导致的。这里记录一下完整的排查和优化过程。\n\n## 开启慢查询日志\n\n```sql\nSET GLOBAL slow_query_log = ON;\nSET GLOBAL long_query_time = 1;\nSET GLOBAL slow_query_log_file = "/var/log/mysql/slow.log";\n```\n\n## 问题定位\n\n通过 `mysqldumpslow` 工具分析慢查询日志，发现有三条高频慢查询：\n\n1. 文章列表查询：全表扫描，缺少 `status` 和 `created_at` 的联合索引\n2. 标签关联查询：`article_tag` 表的 `tag_id` 没有单独索引\n3. 评论树查询：`parent_id` 字段缺少索引\n\n## EXPLAIN 分析\n\n```sql\nEXPLAIN SELECT a.*, u.nickname\nFROM article a\nLEFT JOIN user u ON a.author_id = u.id\nWHERE a.status = 1\nORDER BY a.created_at DESC\nLIMIT 10;\n```\n\n执行计划显示 type 为 ALL，rows 扫描了 12000 行，明显需要优化。\n\n## 优化方案\n\n```sql\n-- 文章表联合索引\nALTER TABLE article ADD INDEX idx_status_created (status, created_at DESC);\n\n-- 标签关联表补充索引\nALTER TABLE article_tag ADD INDEX idx_tag_id (tag_id);\n\n-- 评论表 parent_id 索引\nALTER TABLE comment ADD INDEX idx_parent (parent_id);\n```\n\n优化后，文章列表查询从 800ms 降到了 15ms，效果非常明显。\n\n## 经验总结\n\n- 联合索引要注意最左前缀原则\n- `ORDER BY` 的字段尽量包含在索引中\n- 定期用 `pt-query-digest` 分析慢查询\n- 线上环境建议 `long_query_time` 设为 0.5 秒',
'记录一次线上 MySQL 慢查询的完整排查过程，从开启慢查询日志、EXPLAIN 分析到索引优化，查询耗时从 800ms 降到 15ms。',
4, 5, 3156, 245, 1, 1, NULL, '2026-02-20 19:00:00', 1, 0, '2026-02-20 10:15:00'),
(4, 'Docker Compose 一键部署 Spring Boot + Vue + MySQL 项目',
'## 为什么用 Docker Compose\n\n每次给新同事搭环境都要折腾半天，数据库版本不对、Node 版本不兼容、端口冲突……用 Docker Compose 把整个项目容器化之后，一条命令就能跑起来。\n\n## 项目结构\n\n```\nproject/\n├── docker-compose.yml\n├── blog-backend/\n│   └── Dockerfile\n├── blog-frontend/\n│   └── Dockerfile\n└── mysql/\n    └── init.sql\n```\n\n## 后端 Dockerfile\n\n```dockerfile\nFROM eclipse-temurin:17-jre-alpine\nWORKDIR /app\nCOPY target/blog-backend-1.0.0.jar app.jar\nEXPOSE 8088\nENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]\n```\n\n## 前端 Dockerfile\n\n```dockerfile\nFROM node:18-alpine AS build\nWORKDIR /app\nCOPY package*.json ./\nRUN npm ci\nCOPY . .\nRUN npm run build\n\nFROM nginx:alpine\nCOPY --from=build /app/dist /usr/share/nginx/html\nCOPY nginx.conf /etc/nginx/conf.d/default.conf\n```\n\n## docker-compose.yml\n\n```yaml\nversion: "3.8"\nservices:\n  mysql:\n    image: mysql:8.0\n    environment:\n      MYSQL_ROOT_PASSWORD: root123\n      MYSQL_DATABASE: blog_db\n    volumes:\n      - mysql_data:/var/lib/mysql\n      - ./mysql/init.sql:/docker-entrypoint-initdb.d/init.sql\n    ports:\n      - "3306:3306"\n\n  backend:\n    build: ./blog-backend\n    depends_on:\n      - mysql\n    environment:\n      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/blog_db\n    ports:\n      - "8088:8088"\n\n  frontend:\n    build: ./blog-frontend\n    depends_on:\n      - backend\n    ports:\n      - "80:80"\n\nvolumes:\n  mysql_data:\n```\n\n## 启动\n\n```bash\ndocker-compose up -d --build\n```\n\n第一次构建大概需要 5 分钟，之后增量构建很快。访问 `http://localhost` 就能看到博客首页了。\n\n## 常见问题\n\n- 后端连不上 MySQL：加 `restart: on-failure` 或用 healthcheck 等待 MySQL 就绪\n- 前端 API 代理：nginx 配置中加 `proxy_pass http://backend:8088`\n- 数据持久化：一定要挂载 volume，否则容器重建数据就没了',
'手把手教你用 Docker Compose 将 Spring Boot + Vue + MySQL 项目容器化部署，一条命令启动整个开发环境。',
2, 6, 1567, 98, 1, 1, NULL, '2026-03-01 18:30:00', 1, 0, '2026-03-01 08:45:00'),
(5, '用 Python 调用 OpenAI API 搭建个人知识库助手',
'## 想法来源\n\n平时积累了大量的技术笔记和文档，但每次想找某个知识点的时候都要翻很久。于是想到可以用 LLM + 向量数据库搭一个个人知识库助手，用自然语言提问就能找到答案。\n\n## 技术方案\n\n- 文档解析：用 `langchain` 的 DocumentLoader 加载 Markdown 和 PDF\n- 文本切分：RecursiveCharacterTextSplitter，chunk_size=1000\n- 向量化：OpenAI text-embedding-3-small\n- 向量存储：ChromaDB（本地轻量，够用了）\n- 问答：GPT-4o-mini + RAG 检索增强\n\n## 核心代码\n\n```python\nfrom langchain_openai import OpenAIEmbeddings, ChatOpenAI\nfrom langchain_community.vectorstores import Chroma\nfrom langchain.chains import RetrievalQA\n\nembeddings = OpenAIEmbeddings(model="text-embedding-3-small")\nvectorstore = Chroma(persist_directory="./chroma_db", embedding_function=embeddings)\n\nllm = ChatOpenAI(model="gpt-4o-mini", temperature=0)\nqa_chain = RetrievalQA.from_chain_type(\n    llm=llm,\n    retriever=vectorstore.as_retriever(search_kwargs={"k": 5}),\n    return_source_documents=True\n)\n\nresult = qa_chain.invoke({"query": "Spring Boot 如何配置多数据源？"})\nprint(result["result"])\n```\n\n## 效果\n\n导入了大约 200 篇技术笔记后，检索准确率还不错。对于代码相关的问题，返回的答案基本都能直接用。不过对于跨文档的综合性问题，效果还有提升空间。\n\n## 成本\n\n- Embedding 费用：200 篇文档大约 $0.02\n- 每次查询（含 GPT-4o-mini）：约 $0.001\n- ChromaDB 本地运行，零成本\n\n总体来说非常划算，比订阅各种知识管理工具便宜多了。',
'使用 Python + LangChain + ChromaDB 搭建个人知识库助手，通过 RAG 技术实现对个人技术笔记的智能问答。',
5, 7, 4210, 312, 1, 1, NULL, '2026-03-06 10:00:00', 1, 0, '2026-03-05 16:20:00'),
(6, '《代码整洁之道》读书笔记：写出可维护的代码',
'## 为什么读这本书\n\n工作三年了，越来越觉得写代码容易，写好代码难。接手过不少"祖传代码"，深刻体会到代码可读性的重要性。Robert C. Martin 的这本《Clean Code》算是这个领域的经典了。\n\n## 核心观点摘录\n\n### 命名的艺术\n\n好的命名应该是自解释的。看到变量名就知道它是什么、做什么。\n\n坏的命名：`d`、`temp`、`data`、`info`\n好的命名：`elapsedTimeInDays`、`userLoginCount`、`articlePublishStatus`\n\n### 函数设计原则\n\n- 函数应该短小，20 行以内最佳\n- 只做一件事，做好这件事\n- 参数越少越好，超过 3 个考虑封装成对象\n- 避免副作用：函数名说做 A，内部偷偷做了 B\n\n### 注释的正确用法\n\n好的代码本身就是最好的注释。需要写注释的场景：\n- 解释"为什么"而不是"做了什么"\n- 法律信息和版权声明\n- TODO 标记\n- 对公共 API 的文档说明\n\n不需要的注释：\n- 重复代码含义的注释\n- 注释掉的代码（用 Git 管理历史）\n- 日志式注释（交给 Git log）\n\n## 我的实践体会\n\n读完这本书后，我在项目中做了几个改变：\n1. Code Review 时更关注命名和函数长度\n2. 提交前用 SonarQube 扫描代码异味\n3. 重构时遵循"童子军规则"——让代码比你发现它时更干净\n\n这些习惯坚持了半年，团队的代码质量确实有了明显提升。',
'《代码整洁之道》的读书笔记，总结了命名、函数设计、注释等方面的核心原则，以及在实际项目中的应用体会。',
6, 3, 1345, 89, 1, 1, NULL, '2026-03-11 09:00:00', 1, 0, '2026-03-10 20:00:00'),
(7, 'Redis 缓存实战：从缓存穿透到分布式锁',
'## 引言\n\n在高并发场景下，数据库往往是系统的瓶颈。引入 Redis 缓存可以大幅提升读取性能，但同时也带来了缓存一致性、穿透、雪崩等问题。这篇文章结合实际项目经验，聊聊 Redis 缓存的常见问题和解决方案。\n\n## 缓存穿透\n\n查询一个数据库中不存在的数据，缓存中也没有，每次请求都会打到数据库。\n\n解决方案：\n- 缓存空值，设置较短的过期时间（如 60 秒）\n- 布隆过滤器前置拦截\n\n```java\npublic Article getArticle(Long id) {\n    String key = "article:" + id;\n    String cached = redisTemplate.opsForValue().get(key);\n    if ("NULL".equals(cached)) return null;\n    if (cached != null) return JSON.parseObject(cached, Article.class);\n\n    Article article = articleMapper.selectById(id);\n    if (article == null) {\n        redisTemplate.opsForValue().set(key, "NULL", 60, TimeUnit.SECONDS);\n        return null;\n    }\n    redisTemplate.opsForValue().set(key, JSON.toJSONString(article), 30, TimeUnit.MINUTES);\n    return article;\n}\n```\n\n## 缓存雪崩\n\n大量缓存同时过期，请求全部涌向数据库。\n\n解决方案：\n- 过期时间加随机值，避免同时失效\n- 热点数据永不过期，后台异步更新\n- 多级缓存：本地缓存（Caffeine）+ Redis\n\n## 分布式锁\n\n用 Redis 实现分布式锁，推荐使用 Redisson，比手写 SETNX 可靠得多。\n\n```java\nRLock lock = redissonClient.getLock("lock:article:" + id);\ntry {\n    if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {\n        // 执行业务逻辑\n    }\n} finally {\n    lock.unlock();\n}\n```\n\n## 缓存与数据库一致性\n\n推荐"先更新数据库，再删除缓存"的策略，配合消息队列做补偿删除，基本能满足大多数业务场景的一致性要求。',
'Redis 缓存实战总结，涵盖缓存穿透、缓存雪崩、分布式锁、缓存一致性等高频面试题的实际解决方案。',
4, 5, 5023, 378, 1, 1, NULL, '2026-03-15 16:30:00', 1, 1, '2026-03-15 11:30:00'),
(8, 'TypeScript 5.x 新特性与项目迁移指南',
'## 为什么迁移到 TypeScript\n\n我们团队的前端项目之前一直用 JavaScript，随着项目规模增长到 200+ 个组件，类型相关的 bug 越来越多。上个季度决定逐步迁移到 TypeScript，这里分享一下迁移过程和 TS 5.x 的一些实用新特性。\n\n## TypeScript 5.x 亮点\n\n### Decorators（装饰器）正式支持\n\nTS 5.0 终于支持了 TC39 Stage 3 的装饰器提案，不再需要 `experimentalDecorators` 编译选项。\n\n### const 类型参数\n\n```typescript\nfunction routes<const T extends readonly string[]>(paths: T): T {\n  return paths\n}\nconst r = routes(["home", "about", "blog"])\n```\n\n### satisfies 操作符\n\n```typescript\ntype Colors = "red" | "green" | "blue"\nconst palette = {\n  red: [255, 0, 0],\n  green: "#00ff00",\n  blue: [0, 0, 255]\n} satisfies Record<Colors, string | number[]>\n```\n\n## 渐进式迁移策略\n\n1. 先配置 `tsconfig.json`，开启 `allowJs: true`\n2. 从工具函数和 API 层开始，这些文件依赖少、改动影响小\n3. 逐步迁移组件，先加 `.vue` 文件的 `<script setup lang="ts">`\n4. 最后处理全局类型定义和第三方库的类型声明\n\n整个迁移花了大约 6 周，期间没有影响正常迭代。迁移完成后，IDE 的智能提示和重构能力提升非常明显。',
'分享 TypeScript 5.x 的实用新特性，以及大型 Vue 项目从 JS 渐进式迁移到 TS 的实战经验。',
3, 4, 1876, 112, 1, 1, NULL, '2026-03-21 10:00:00', 1, 0, '2026-03-20 15:45:00'),
(9, '学习笔记：Kubernetes 入门到实践',
'## K8s 核心概念\n\n### Pod\nK8s 最小调度单元，一个 Pod 可以包含一个或多个容器。\n\n### Deployment\n管理 Pod 的副本数量，支持滚动更新和回滚。\n\n### Service\n为一组 Pod 提供稳定的网络访问入口。\n\n（待补充更多内容...）',
'Kubernetes 学习笔记，整理中...',
5, 1, 0, 0, 0, NULL, NULL, NULL, NULL, 0, '2026-04-01 22:00:00'),
(10, '程序员的周末：从代码到咖啡',
'## 工作日 vs 周末\n\n作为一个程序员，工作日的生活基本就是写代码、开会、Review 代码的循环。周末终于可以从 IDE 中解放出来，做点不一样的事情。\n\n## 我的周末日常\n\n### 上午：逛咖啡馆\n\n最近迷上了手冲咖啡，周末会去城里不同的独立咖啡馆坐坐。上周发现了一家藏在胡同里的小店，老板是个退休的大学教授，店里的埃塞俄比亚耶加雪菲冲得特别好。\n\n### 下午：读书或骑行\n\n天气好的时候会沿着河边骑车，单程大约 15 公里。骑行的时候脑子放空，反而经常能想通工作中卡住的技术问题。\n\n天气不好就窝在家里看书。最近在读《系统之美》，讲系统思维的，对理解复杂软件架构也有启发。\n\n### 晚上：折腾 Side Project\n\n虽然说好了周末不写代码，但晚上还是忍不住打开电脑折腾自己的小项目。和工作不同的是，Side Project 没有 deadline，想用什么技术就用什么技术，纯粹为了好玩。\n\n## 写在最后\n\n程序员也是普通人，需要在代码之外找到生活的乐趣。保持好奇心，保持对生活的热情，写出来的代码也会更有温度。',
'一个程序员的周末生活记录：咖啡、骑行、读书，以及忍不住打开电脑写 Side Project。',
2, 2, 892, 67, 1, 1, NULL, '2026-04-10 18:00:00', 1, 0, '2026-04-10 10:30:00'),
(11, '基于 Spring Security 的接口鉴权排错记录',
'## 问题现象\n\n项目接入 JWT 之后，部分接口在开发环境下表现正常，但切换到代理转发或跨域场景后会偶发 401。\n\n## 排查过程\n\n首先检查了前端是否正确携带 Authorization 请求头，然后又核对了 Spring Security 过滤器链和 CORS 配置。最后发现真正的问题集中在两个地方：一是某些公开接口的放行规则不完整，二是 WebSocket 握手请求与普通 HTTP 接口在认证链路上存在差异。\n\n## 结论\n\n后续需要把认证过滤器、公开路由白名单和调试日志统一梳理一遍，再补齐端到端验证用例。',
'记录一次 Spring Security + JWT 接口鉴权问题的排查过程，目前作为待审核稿件提交。',
5, 5, 146, 12, 1, 0, NULL, NULL, NULL, 0, '2026-04-18 09:40:00'),
(12, 'Vue 3 富文本编辑器选型与踩坑备忘',
'## 选型背景\n\n为了给博客系统加入更好的创作体验，我对比了几种常见的富文本编辑器方案，包括基于 Markdown 的方案和可视化编辑方案。\n\n## 当前结论\n\n编辑器能力和集成成本需要综合权衡，尤其要关注图片上传、代码块高亮、内容回显和样式兼容问题。\n\n## 待完善点\n\n后续会继续补充不同编辑器在 Vue 3 项目中的兼容性记录、二次封装方式以及实际性能表现。',
'一篇仍在补充中的富文本编辑器选型笔记，已发布并进入待审核状态。',
6, 4, 63, 5, 1, 0, NULL, NULL, NULL, 0, '2026-04-19 14:10:00'),
(13, '一次失败的 Redis 缓存重构复盘',
'## 背景\n\n上周尝试把文章详情缓存策略统一重构，但上线前复盘时发现方案对缓存删除时机考虑不足，可能导致脏数据窗口被放大。\n\n## 主要问题\n\n第一，更新数据库后删除缓存的顺序虽然正确，但补偿逻辑不完整。第二，热点文章在高并发下仍然可能出现短时间不一致。第三，监控指标没有覆盖缓存命中率与异常回源量。\n\n## 复盘结论\n\n这次方案暂不适合直接推广，需要补充更完整的回源监控、消息补偿和回归验证，再重新提交评审。',
'一篇围绕 Redis 缓存重构复盘的文章，因内容结论和论证不足被审核驳回。',
4, 5, 37, 2, 1, 2, '结论论证不充分，缺少可执行的监控方案与回归数据，请补充后重新提交。', '2026-04-21 10:25:00', 1, 0, '2026-04-20 16:30:00');
ALTER TABLE `article` AUTO_INCREMENT = 14;

-- =====================
-- 文章标签关联
-- =====================
INSERT INTO `article_tag` (`article_id`, `tag_id`) VALUES
(1, 1), (1, 2), (1, 5), (1, 19),
(2, 3), (2, 9), (2, 17),
(3, 5), (3, 18), (3, 12),
(4, 7), (4, 2), (4, 3), (4, 12),
(5, 10), (5, 16),
(6, 14), (6, 20),
(7, 6), (7, 1), (7, 18), (7, 13),
(8, 9), (8, 3), (8, 17),
(9, 8), (9, 7), (9, 12),
(10, 20),
(11, 2), (11, 19),
(12, 3), (12, 17),
(13, 6), (13, 18), (13, 13);

-- =====================
-- 评论数据
-- =====================
INSERT INTO `comment` (`id`, `article_id`, `user_id`, `content`, `parent_id`, `likes`, `status`, `created_at`) VALUES
(1, 1, 3, '写得很详细，正好在学 Spring Boot 3，收藏了！请问 MyBatis-Plus 的代码生成器你有用过吗？', NULL, 12, 1, '2026-02-11 10:20:00'),
(2, 1, 4, '代码生成器可以用，但生成的代码建议只作为起点，后续还是要根据业务调整。', 1, 5, 1, '2026-02-11 14:35:00'),
(3, 1, 5, '请问 JWT 的 refresh token 机制具体是怎么实现的？文章里好像没有展开讲。', NULL, 8, 1, '2026-02-12 09:10:00'),
(4, 1, 2, '好问题，refresh token 的逻辑是：access token 过期后，前端用 refresh token 请求新的 access token，避免用户频繁登录。后续我会单独写一篇。', 3, 15, 1, '2026-02-12 11:00:00'),
(5, 1, 6, '终于看到一篇用 Spring Boot 3 的实战文章了，之前找的教程都还是 2.x 的。', NULL, 6, 1, '2026-02-13 16:40:00'),
(6, 2, 2, '组合式 API 确实比选项式灵活很多，特别是 composables 的复用能力。我们项目迁移后代码量减少了大约 20%。', NULL, 9, 1, '2026-02-16 08:50:00'),
(7, 2, 5, '请问 Pinia 和 Vuex 5 有什么区别？现在新项目应该选哪个？', NULL, 4, 1, '2026-02-16 15:20:00'),
(8, 2, 3, 'Pinia 就是 Vuex 5，官方已经推荐用 Pinia 了。API 更简洁，TypeScript 支持也更好。', 7, 11, 1, '2026-02-16 17:30:00'),
(9, 3, 2, '800ms 到 15ms，这优化效果太明显了。请问你们线上用的是什么工具监控慢查询的？', NULL, 7, 1, '2026-02-21 09:30:00'),
(10, 3, 4, '我们用的 Percona Monitoring and Management (PMM)，开源免费，功能很全。也可以用阿里云的 DAS 服务。', 9, 3, 1, '2026-02-21 10:45:00'),
(11, 3, 6, '联合索引的最左前缀原则这块讲得很清楚，之前一直搞混。', NULL, 5, 1, '2026-02-22 14:00:00'),
(12, 5, 2, 'RAG 方案现在确实很火，不过 embedding 模型的选择对检索质量影响很大。你试过 BGE 或者 M3E 这些国产模型吗？', NULL, 10, 1, '2026-03-06 10:15:00'),
(13, 5, 5, '试过 BGE-large-zh，中文场景下效果确实比 OpenAI 的 embedding 好一些，而且可以本地部署，没有 API 调用费用。', 12, 8, 1, '2026-03-06 14:20:00'),
(14, 5, 4, '建议加一个 rerank 步骤，先用 embedding 粗筛，再用 cross-encoder 精排，检索准确率能提升不少。', NULL, 13, 1, '2026-03-07 09:00:00'),
(15, 5, 3, '这个成本也太低了吧，200 篇文档才 2 分钱。我也去搭一个试试。', NULL, 6, 1, '2026-03-08 20:30:00'),
(16, 7, 3, 'Redis 分布式锁那段，Redisson 确实比手写靠谱。之前我们用 SETNX 自己实现的，遇到过锁超时但业务没执行完的问题。', NULL, 14, 1, '2026-03-16 08:30:00'),
(17, 7, 5, '缓存和数据库一致性这块，延迟双删方案怎么样？', NULL, 3, 1, '2026-03-16 11:00:00'),
(18, 7, 4, '延迟双删也可以，但要注意延迟时间的设置。我更推荐用 Canal 监听 binlog 来异步删除缓存，更可靠。', 17, 9, 1, '2026-03-16 14:15:00'),
(19, 10, 6, '哈哈，说好的周末不写代码，结果晚上还是忍不住。太真实了。', NULL, 18, 1, '2026-04-10 12:00:00'),
(20, 10, 3, '耶加雪菲确实好喝，推荐试试肯尼亚 AA，果酸味很特别。', NULL, 7, 1, '2026-04-10 15:30:00'),
(21, 10, 4, '骑行的时候想通技术问题这个太有共鸣了，我一般是洗澡的时候。', NULL, 22, 1, '2026-04-11 09:20:00');
ALTER TABLE `comment` AUTO_INCREMENT = 22;

-- =====================
-- 访问统计数据
-- =====================
INSERT INTO `visit_log` (`article_id`, `ip`, `user_agent`, `referer`, `created_at`) VALUES
(1, '223.104.63.12', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0.0.0', 'https://www.google.com', '2026-02-10 09:15:00'),
(1, '116.25.88.201', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 Chrome/124.0.0.0', 'https://juejin.cn', '2026-02-10 10:30:00'),
(1, '39.144.12.78', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15', NULL, '2026-02-10 14:20:00'),
(3, '101.226.33.45', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0.0.0', 'https://www.baidu.com/s?wd=MySQL慢查询优化', '2026-02-20 11:00:00'),
(3, '180.169.45.67', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0', 'https://segmentfault.com', '2026-02-21 08:45:00'),
(5, '58.247.200.13', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 Safari/605.1.15', 'https://twitter.com', '2026-03-05 17:00:00'),
(5, '114.88.12.99', 'Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile', 'https://v2ex.com', '2026-03-06 09:30:00'),
(7, '202.96.134.88', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0.0.0', 'https://www.zhihu.com/question/123456', '2026-03-15 12:00:00'),
(7, '61.172.201.45', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4) AppleWebKit/537.36 Chrome/124.0.0.0', 'https://mp.weixin.qq.com', '2026-03-16 10:15:00'),
(10, '117.136.8.201', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148', NULL, '2026-04-10 11:00:00');

-- =====================
-- 留言板 + 审核通知数据
-- type: 0=普通留言 1=文章审核通知
-- status: 留言 0=未回复 1=已回复；通知 0=未读 1=已读
-- =====================
INSERT INTO `message` (`id`, `user_id`, `article_id`, `type`, `title`, `nickname`, `email`, `content`, `reply`, `status`, `created_at`) VALUES
(1, NULL, NULL, 0, '用户留言', '路过的前端er', 'visitor01@gmail.com', '博客做得很漂亮，请问前端用的什么 UI 框架？想参考一下。', '感谢关注！前端用的 Element Plus，搭配 Vue 3 组合式 API 开发的。', 1, '2026-03-01 14:30:00'),
(2, NULL, NULL, 0, '用户留言', 'Java新手', 'newbie@qq.com', '请问博主有没有推荐的 Spring Boot 入门教程？刚开始学，有点迷茫。', '推荐先看官方文档的 Getting Started，然后跟着做一个小项目练手。B站上也有很多不错的免费教程。', 1, '2026-03-10 09:20:00'),
(3, NULL, NULL, 0, '用户留言', '匿名用户', NULL, '文章质量很高，希望能多更新一些关于微服务架构的内容。', NULL, 0, '2026-03-20 18:45:00'),
(4, NULL, NULL, 0, '用户留言', '考研党小刘', 'liuxr@163.com', '博主的算法文章写得很好，请问有没有计划写一个算法专栏？', NULL, 0, '2026-04-05 21:30:00'),
(5, NULL, NULL, 0, '用户留言', '运维老王', 'wangops@company.com', 'Docker 那篇文章帮了大忙，我们团队按照你的方案成功容器化了三个项目。特地来留言感谢！', '太好了！很高兴能帮到你们。后续还会写 K8s 部署相关的文章，敬请期待。', 1, '2026-04-15 10:00:00'),
(6, 5, 11, 1, '文章审核通过', NULL, NULL, '《基于 Spring Security 的接口鉴权排错记录》审核结果：文章审核通过。', NULL, 0, '2026-04-18 18:30:00'),
(7, 6, 12, 1, '文章审核通过', NULL, NULL, '《Vue 3 富文本编辑器选型与踩坑备忘》审核结果：文章审核通过。', NULL, 1, '2026-04-19 19:10:00'),
(8, 4, 13, 1, '文章审核未通过', NULL, NULL, '《一次失败的 Redis 缓存重构复盘》审核结果：文章审核未通过。 原因：结论论证不充分，缺少可执行的监控方案与回归数据，请补充后重新提交。', NULL, 0, '2026-04-21 10:30:00');
ALTER TABLE `message` AUTO_INCREMENT = 9;

-- =====================
-- 系统日志数据
-- =====================
INSERT INTO `sys_log` (`user_id`, `action`, `detail`, `ip`, `created_at`) VALUES
(1, 'dashboard', '耗时: 45ms', '127.0.0.1', '2026-02-10 08:00:00'),
(1, 'listUsers', '耗时: 32ms', '127.0.0.1', '2026-02-10 08:01:00'),
(1, 'toggleUserStatus', '耗时: 18ms', '127.0.0.1', '2026-02-15 09:30:00'),
(1, 'createCategory', '耗时: 12ms', '127.0.0.1', '2026-03-01 10:00:00'),
(1, 'createTag', '耗时: 8ms', '127.0.0.1', '2026-03-01 10:05:00'),
(1, 'deleteComment', '耗时: 15ms', '127.0.0.1', '2026-03-10 14:20:00'),
(1, 'replyMessage', '耗时: 22ms', '127.0.0.1', '2026-03-15 11:00:00'),
(1, 'listArticles', '耗时: 28ms', '127.0.0.1', '2026-04-01 09:00:00'),
(1, 'reviewArticle', 'articleId=11,status=1', '192.168.1.100', '2026-04-18 18:30:00'),
(1, 'reviewArticle', 'articleId=12,status=1', '192.168.1.100', '2026-04-19 19:10:00'),
(1, 'reviewArticle', 'articleId=13,status=2', '192.168.1.100', '2026-04-21 10:30:00'),
(1, 'dashboard', '耗时: 38ms', '192.168.1.100', '2026-04-20 08:30:00'),
(1, 'listLogs', '耗时: 20ms', '192.168.1.100', '2026-04-20 08:31:00');
