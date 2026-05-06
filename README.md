# Kiblog

Kiblog 是一个基于 Spring Boot、Thymeleaf 和 SQLite 的轻量级个人博客系统。它支持文章发布与管理、Markdown 渲染、品牌文案外置配置，以及适合 `jar + config` 目录一起分发的部署方式。

## 特性

- 首页、文章页、后台管理页统一支持深浅主题切换
- 文章内容支持 Markdown 渲染
- 支持脚注、任务列表、表格、KaTeX 数学公式
- 支持 Markdown 中的安全 HTML 扩展渲染
- 支持文章标签展示、搜索、分页
- 支持后台发布、编辑、删除文章
- 支持站点品牌内容通过外部配置文件自定义
- 支持用户将头像等资源放在 `config/assets/` 中单独管理

## 技术栈

- Java 25
- Spring Boot 4.0.6
- Spring MVC
- Spring Data JPA
- Thymeleaf
- SQLite

## 目录结构

```text
Kiblog/
├─ config/
│  ├─ admin.json
│  ├─ assets/
│  └─ branding.yml
├─ src/
├─ kiblog.db
├─ pom.xml
└─ README.md
```

## 运行要求

- JDK 25
- Maven 3.9+，或者直接使用仓库内置的 `mvnw` / `mvnw.cmd`

## 本地启动

### 1. 克隆并进入项目

```bash
git clone <your-repo-url>
cd Kiblog
```

### 2. 启动项目

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
./mvnw spring-boot:run
```

默认启动后访问：

- 前台首页：`http://localhost:8080/`
- 后台登录：`http://localhost:8080/admin/login`

## 打包运行

### 1. 构建 Jar

Windows:

```powershell
.\mvnw.cmd clean package
```

macOS / Linux:

```bash
./mvnw clean package
```

构建完成后，Jar 文件通常位于：

```text
target/Kiblog-0.0.1-SNAPSHOT.jar
```

### 2. 使用 `jar + config` 方式部署

推荐将以下内容放在同一目录下：

```text
deploy/
├─ Kiblog-0.0.1-SNAPSHOT.jar
└─ config/
   ├─ admin.json
   ├─ assets/
   └─ branding.yml
```

运行方式：

```bash
java -jar Kiblog-0.0.1-SNAPSHOT.jar
```

项目会自动从当前目录下的 `config/branding.yml` 读取品牌配置。

## 配置说明

### `config/branding.yml`

该文件用于定义首页、站点名和个人简介等品牌内容。默认示例：

```yml
kiblog:
  brand:
    site-name: Kiblog
    home-title: 写给长期主义者的技术与生活记录
    home-subtitle: 从代码、产品和日常观察中挑出值得留存的部分，做成一份可反复翻阅的数字笔记。
    home-articles-title: 最新文章
    home-articles-description: 从最近写下的内容开始浏览，看看这段时间我在关注什么。
    about-name: Kiblog
    about-subtitle: 记录技术、产品与长期学习过程中的思考。
    about-description: 这里主要用来整理编程实践、产品体验和日常学习中的零散想法，希望把它们慢慢沉淀成更清晰、也更值得回看的内容。
    about-avatar-path: avatar.jpg
```

### 头像路径说明

`about-avatar-path` 按相对于 `config/assets/` 目录的路径填写。

例如：

- `avatar.jpg`
- `portraits/me.png`

如果你的文件结构是：

```text
config/
├─ assets/
│  └─ portraits/
│     └─ me.png
└─ branding.yml
```

那么应填写：

```yml
about-avatar-path: portraits/me.png
```

头像资源最终会通过 `/assets/**` 对外提供访问，`config` 目录中的其他文件不会被公开暴露。

### `config/admin.json`

该文件用于保存管理员用户名和密码。系统兼容两种写法：

推荐写法，使用加密后的密码：

```json
{
  "username": "admin",
  "encodedPassword": "..."
}
```

兼容写法，也可以直接写明文密码：

```json
{
  "username": "admin",
  "password": "123456"
}
```

首次运行后如果文件不存在，系统会自动初始化管理员配置。

如果系统启动时检测到 `password` 是明文：

- 会自动将其转换为 `encodedPassword`
- 会立即回写到 `config/admin.json`
- 不再继续保留明文字段

## 数据库

项目默认使用 SQLite，本地数据库文件为：

```text
kiblog.db
```

数据库连接配置位于 [src/main/resources/application.properties](src/main/resources/application.properties)：

```properties
spring.datasource.url=jdbc:sqlite:kiblog.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.hibernate.ddl-auto=update
```

## 后台说明

后台页面入口：

```text
/admin/login
```

登录成功后可以进入后台管理页，进行以下操作：

- 发布文章
- 编辑文章
- 删除文章
- 实时预览 Markdown 渲染效果

当前后台采用浏览器本地 token 的方式保护管理接口。

## Markdown 能力

Kiblog 当前支持以下内容渲染：

- 标题、段落、引用、代码块
- 标签页风格的任务列表
- 脚注
- 表格与横向滚动优化
- KaTeX 数学公式
- `details/summary` 折叠面板
- 音视频与 iframe 嵌入
- 安全的 HTML 扩展内容渲染

说明：

- 当净化器可用时，Markdown 中的 HTML 扩展内容会经过安全清洗后渲染
- 当净化器不可用时，系统会退回到更保守的安全模式，不直接渲染原始 HTML

## 安全说明

- `config/` 目录不会被整体暴露
- 仅 `config/assets/` 会通过 `/assets/**` 暴露给前台，用于头像等静态资源
- Markdown 渲染链已加入 HTML 安全清洗
- 管理接口受后台认证保护

## 开发说明

编译检查：

Windows:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

macOS / Linux:

```bash
./mvnw -q -DskipTests compile
```

## 可自定义内容

推荐开放给使用者自行修改的内容包括：

- `config/branding.yml` 中的站点名与文案
- `config/assets/` 中的头像文件
- `kiblog.db` 中的文章数据
- `config/admin.json` 中的后台账号

## License

该项目使用MIT LICENSE，详情请参阅 [LICENSE](LICENSE) 文件。
