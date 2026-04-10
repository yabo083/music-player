# PROJECT ARCHITECTURE

## 1. 系统目标

`music-player` 通过读取外部 `soundpacks` 目录中的条件 JSON，在客户端运行时按环境条件选择并播放 BGM。  
核心目标是将“条件匹配”与“音频资源提供”解耦，允许用户无需改模组代码即可扩展音乐规则。

## 2. 核心模块拓扑

- `SoundPackManager`
  - 负责发现/加载 soundpack（目录与 zip）
  - 解析 `conditions/*.json` 为 `MusicDefinition`
  - 生成动态 `sounds.json` 内容并维护 OGG 资源映射
- `MusicDefinition`
  - 单条条件规则的数据模型
  - 包含匹配条件、音乐列表与播放模式
- `ClientMusicManager`
  - Tick 驱动的客户端播放控制器
  - 计算当前最佳匹配规则、拦截冲突音乐源、触发播放/停播
- `PlaylistNavigator`
  - 负责在同一条 `MusicDefinition` 的多首音乐之间计算“下一首”
  - 支持顺序、随机、指定顺序三种策略
- `MusicConditionEvaluator`
  - 基于玩家上下文（生物群系、时间、天气、维度、实体等）评估规则命中

## 3. 条件 JSON 协议（架构级）

### 3.1 `music` 字段

- 向后兼容：支持字符串与数组两种输入
  - 字符串：`"music": "music/lifeline.ogg"`
  - 数组：`"music": ["music/a.ogg", "music/b.ogg"]`

### 3.2 播放策略字段

- `play_mode`
  - `sequential`: 按 `music` 列表顺序循环
  - `random`: 随机选曲（避免与上一首立即重复）
  - `specified`: 按 `play_order` 指定顺序播放
- `play_order`
  - 仅在 `play_mode = "specified"` 时使用
  - 允许混合两种元素：
    - 索引字符串（如 `"2"`）
    - 音乐路径（如 `"music/winter.ogg"`）
  - 未覆盖到的曲目会自动按原列表顺序补齐到队尾

## 4. 播放决策与数据流

1. `SoundPackManager` 读取并校验每条 `MusicDefinition`，将每个 `music` 项映射为独立 `soundEventKey`。
2. `ClientMusicManager` 周期性计算当前最佳匹配规则（按优先级与条件命中）。
3. 当规则切换时：
   - 停止当前音乐
   - 重置并初始化 `PlaylistNavigator`
   - 选择该规则下第一首应播曲目
4. 当当前曲目自然结束时：
   - 由 `PlaylistNavigator` 根据 `play_mode` 计算下一首
   - 播放下一首并更新 `currentMusicSoundEventKey`

## 5. 兼容性约束

- 旧配置（单 `music` 字符串）保持可用。
- 多曲配置不影响已有条件评估逻辑，仅扩展“命中后的选曲行为”。
- `onPlaySound` 基于 `currentMusicSoundEventKey` 校验当前允许播放的事件键，避免错误拦截同规则内非首曲目。
