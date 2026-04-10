# Music Player

Example soundpack: [Example-SoundPack](https://github.com/zoma1101/Example-SoundPack/tree/main)

本模组支持根据条件自动切换 BGM。  
将资源包放入 `.minecraft/soundpacks` 后，启用与 `pack_id` 同名的资源包即可播放。

## 文件结构

- 条件文件: `soundpack/pack_id/assets/music_player/conditions/predicate.json`
- 音频文件: `soundpack/pack_id/assets/pack_id/music/name.ogg`

## 条件 JSON 基础格式

```json
{
  "priority": 100,
  "music": "music/name.ogg"
}
```

`music` 现在同时支持以下两种写法：

```json
{
  "music": "music/lifeline.ogg"
}
```

```json
{
  "music": ["music/flower_dance.ogg", "music/winter.ogg"]
}
```

## 播放方式控制（新增）

通过额外字段控制多曲播放行为：

- `play_mode`: 播放模式，可选 `sequential` / `random` / `specified`
- `play_order`: 仅在 `play_mode = "specified"` 时生效。用于指定顺序，可写索引字符串（如 `"2"`）或音乐路径（如 `"music/winter.ogg"`）

### 1) 顺序播放（按 music 列表顺序循环）

```json
{
  "priority": 10,
  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
  "play_mode": "sequential"
}
```

### 2) 随机播放（避免连续重复同一首）

```json
{
  "priority": 10,
  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
  "play_mode": "random"
}
```

### 3) 按指定顺序播放

```json
{
  "priority": 10,
  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
  "play_mode": "specified",
  "play_order": ["music/c.ogg", "0"]
}
```

上例实际顺序为：`music/c.ogg -> music/a.ogg -> music/b.ogg`，然后循环。

## 可用条件字段

- `biomes`: 指定生物群系（支持 biome tag）
- `is_night`: 是否夜晚
- `is_combat`: 是否战斗状态
- `is_village`: 周围是否存在村民
- `min_y`: 最低高度（大于等于）
- `max_y`: 最高高度（小于等于）
- `weather`: 天气条件（`clear` / `rain` / `thunder`）
- `dimensions`: 维度列表
- `gui_screen`: 指定 GUI 打开时播放（`crafting` / `inventory` / `furnace` / `brewing_stand` / `chest` / `creative`）
- `entity_conditions`: 指定实体或实体标签列表
- `radius`: 实体检测半径
- `min_count`: 最小数量
- `max_count`: 最大数量
