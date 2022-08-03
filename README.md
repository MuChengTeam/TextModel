# MuCodeEditor

### 最后更新于：2022/5/30 - 15:10

### 开源组 QQ 群：1032012046

## 一款流畅的大文本编辑器

免费且开源，你还在等什么！

## 协议

我们使用 MPL 2.0 作为开源协议，其中明确规定你可以拿来商用与修改，但版权永远属于本人（SuMuCheng）

除非我允许的情况下你不得做出侵权行为

## 基础功能：

基础文本编辑、文本选中、打开文件（InputStream)、保存文件 （通过 OutputStream）

setText 设置内容的支持

getText 获取内容的支持

## 常用功能：

代码高亮、代码自动补全、Undo & Redo（撤销 & 恢复）

## 光标动画:

光标可见性动画、光标移动动画（目前三种：平移动画、缩放动画、淡入淡出动画）

## 组件：

Cursor -> 光标，控制光标显示位置

Renderer -> 渲染器，用于绘制编辑器

Painters -> 画笔集合，用于存放绘制所用的 Paint

TextSelectHandle -> 手柄（选中时的角标）的绘制器

## 主题:

通过继承 AbstractTheme 并设置你可以实现自定义主题颜色

## 数据

数据存储使用 TextModel（文本模型），
开源地址：https://github.com/CaiMuCheng/TextModel

## 后续：

后续将向支持 Language Server Protocol 的方向发展，

准备支持 Parser 解析文本，添加至 AutoCompletionPanel 中