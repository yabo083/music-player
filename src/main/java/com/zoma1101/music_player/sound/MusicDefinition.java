package com.zoma1101.music_player.sound;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

public class MusicDefinition {

    public enum PlaybackMode {
        RANDOM,
        SEQUENTIAL,
        SPECIFIED
    }

    // --- JSONから直接ロードされるフィールド ---
    @SerializedName("priority")
    public int priority = 0;

    @SerializedName("music")
    @JsonAdapter(MusicPathListTypeAdapter.class)
    public List<String> musicFilesInPack = new ArrayList<>();

    @SerializedName(value = "play_mode", alternate = {"playback_mode", "playlist_mode", "mode"})
    @Nullable
    public String playMode = null;

    // 値は index 文字列 ("0") でも music パス ("music/foo.ogg") でも可
    @SerializedName(value = "play_order", alternate = {"specified_order", "playlist_order"})
    @Nullable
    public List<String> playOrder = null;

    @SerializedName("biomes")
    @Nullable
    public List<String> biomes = null;

    @SerializedName("is_night")
    @Nullable
    public Boolean isNight = null;

    @SerializedName("is_combat")
    @Nullable
    public Boolean isCombat = null;

    @SerializedName("is_village")
    @Nullable
    public Boolean isVillage = null;

    @SerializedName("min_y")
    @Nullable
    public Integer minY = null;

    @SerializedName("max_y")
    @Nullable
    public Integer maxY = null;

    @SerializedName("gui_screen")
    @Nullable
    public String guiScreen = null;

    @SerializedName("weather")
    @Nullable
    public List<String> weather = null;

    @SerializedName("dimensions")
    @Nullable
    public List<String> dimensions = null;

    // --- エンティティ条件用のトップレベルフィールド ---
    @SerializedName("entity_conditions")
    @Nullable
    public List<String> entityConditions = null; // エンティティIDまたはタグのリスト

    @SerializedName("radius")
    @Nullable
    public Double radius = null;

    @SerializedName("min_count")
    @Nullable
    public Integer minCount = null;

    @SerializedName("max_count")
    @Nullable
    public Integer maxCount = null; // nullの場合は上限なし

    // --- ロード処理中に設定される内部フィールド ---
    private transient String soundPackId; // この定義が属するSoundPackのID
    private transient List<Path> absoluteOggPaths = new ArrayList<>(); // OGGファイルの絶対パス
    // sounds.json のトップレベルキー (例: "cool_pack/music/battle1")
    // Minecraft内部ではこのキーでサウンドイベントが認識される
    private transient List<String> soundEventKeys = new ArrayList<>();
    // MODがOGGファイルを提供するためのResourceLocation
    private transient List<ResourceLocation> oggResourceLocations = new ArrayList<>();

    // Gsonのためのデフォルトコンストラクタ
    public MusicDefinition() {}

    // --- Getter ---
    public int getPriority() { return priority; }

    @Nullable
    public String getMusicFileInPack() {
        return musicFilesInPack == null || musicFilesInPack.isEmpty() ? null : musicFilesInPack.get(0);
    }

    public List<String> getMusicFilesInPack() {
        return musicFilesInPack == null ? Collections.emptyList() : Collections.unmodifiableList(musicFilesInPack);
    }

    public PlaybackMode getPlaybackMode() {
        if (playMode == null || playMode.isBlank()) {
            return PlaybackMode.SEQUENTIAL;
        }

        String normalized = playMode.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "random", "shuffle", "rand", "随机" -> PlaybackMode.RANDOM;
            case "specified", "specified_order", "custom", "manual", "指定顺序", "指定" -> PlaybackMode.SPECIFIED;
            case "sequential", "sequence", "ordered", "order", "顺序" -> PlaybackMode.SEQUENTIAL;
            default -> PlaybackMode.SEQUENTIAL;
        };
    }

    public List<Integer> resolvePlaybackOrderIndices() {
        int trackCount = musicFilesInPack == null ? 0 : musicFilesInPack.size();
        if (trackCount <= 0) {
            return Collections.emptyList();
        }

        List<Integer> defaultOrder = IntStream.range(0, trackCount).boxed().toList();
        if (getPlaybackMode() != PlaybackMode.SPECIFIED || playOrder == null || playOrder.isEmpty()) {
            return defaultOrder;
        }

        List<Integer> resolvedOrder = new ArrayList<>();
        Set<Integer> usedIndices = new HashSet<>();

        for (String token : playOrder) {
            Integer resolvedIndex = resolvePlayOrderToken(token);
            if (resolvedIndex == null || resolvedIndex < 0 || resolvedIndex >= trackCount || !usedIndices.add(resolvedIndex)) {
                continue;
            }
            resolvedOrder.add(resolvedIndex);
        }

        if (resolvedOrder.isEmpty()) {
            return defaultOrder;
        }

        for (int i = 0; i < trackCount; i++) {
            if (!usedIndices.contains(i)) {
                resolvedOrder.add(i);
            }
        }
        return resolvedOrder;
    }

    @Nullable
    private Integer resolvePlayOrderToken(@Nullable String token) {
        if (token == null || token.isBlank() || musicFilesInPack == null) {
            return null;
        }

        String trimmed = token.trim();
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
            // numeric index ではないので music パスとして解決
        }

        int directMatch = musicFilesInPack.indexOf(trimmed);
        if (directMatch >= 0) {
            return directMatch;
        }

        for (int i = 0; i < musicFilesInPack.size(); i++) {
            if (trimmed.equalsIgnoreCase(musicFilesInPack.get(i))) {
                return i;
            }
        }
        return null;
    }

    @Nullable
    public List<String> getBiomes() { return biomes; }
    @Nullable
    public Boolean isNight() { return isNight; }
    @Nullable
    public Boolean isCombat() { return isCombat; }
    @Nullable
    public Boolean isVillage() { return isVillage; }
    @Nullable
    public Integer getMinY() { return minY; }
    @Nullable
    public Integer getMaxY() { return maxY; }
    @Nullable
    public String getGuiScreen() { return guiScreen; }
    @Nullable
    public List<String> getWeather() { return weather; }
    @Nullable
    public List<String> getDimensions() { return dimensions; }
    @Nullable
    public List<String> getEntityConditions() { return entityConditions; }
    @Nullable
    public Double getRadius() { return radius; }
    @Nullable
    public Integer getMinCount() { return minCount; }
    @Nullable
    public Integer getMaxCount() { return maxCount; }

    public String getSoundPackId() { return soundPackId; }

    @Nullable
    public String getSoundEventKey() {
        return soundEventKeys.isEmpty() ? null : soundEventKeys.get(0);
    }

    public List<String> getSoundEventKeys() {
        return Collections.unmodifiableList(soundEventKeys);
    }

    @Nullable
    public ResourceLocation getOggResourceLocation() {
        return oggResourceLocations.isEmpty() ? null : oggResourceLocations.get(0);
    }

    public List<ResourceLocation> getOggResourceLocations() {
        return Collections.unmodifiableList(oggResourceLocations);
    }

    public List<Path> getAbsoluteOggPaths() {
        return Collections.unmodifiableList(absoluteOggPaths);
    }

    // --- Setter (ロード処理中に使用) ---
    public void setSoundPackId(String soundPackId) { this.soundPackId = soundPackId; }

    public void setAbsoluteOggPath(Path absoluteOggPath) {
        this.absoluteOggPaths = new ArrayList<>();
        if (absoluteOggPath != null) {
            this.absoluteOggPaths.add(absoluteOggPath);
        }
    }

    public void setAbsoluteOggPaths(List<Path> absoluteOggPaths) {
        this.absoluteOggPaths = absoluteOggPaths == null ? new ArrayList<>() : new ArrayList<>(absoluteOggPaths);
    }

    public void setSoundEventKey(String soundEventKey) {
        this.soundEventKeys = new ArrayList<>();
        if (soundEventKey != null && !soundEventKey.isBlank()) {
            this.soundEventKeys.add(soundEventKey);
        }
    }

    public void setSoundEventKeys(List<String> soundEventKeys) {
        this.soundEventKeys = soundEventKeys == null ? new ArrayList<>() : new ArrayList<>(soundEventKeys);
    }

    public void setOggResourceLocation(ResourceLocation oggResourceLocation) {
        this.oggResourceLocations = new ArrayList<>();
        if (oggResourceLocation != null) {
            this.oggResourceLocations.add(oggResourceLocation);
        }
    }

    public void setOggResourceLocations(List<ResourceLocation> oggResourceLocations) {
        this.oggResourceLocations = oggResourceLocations == null ? new ArrayList<>() : new ArrayList<>(oggResourceLocations);
    }

    public boolean isValid() {
        // 基本的なフィールドのチェック
        if (priority < 0
                || musicFilesInPack == null
                || musicFilesInPack.isEmpty()
                || musicFilesInPack.stream().anyMatch(path -> path == null || path.isBlank())
                || soundPackId == null || soundPackId.isBlank()
                || absoluteOggPaths == null
                || absoluteOggPaths.size() != musicFilesInPack.size()
                || absoluteOggPaths.stream().anyMatch(Objects::isNull)
                || soundEventKeys == null
                || soundEventKeys.size() != musicFilesInPack.size()
                || soundEventKeys.stream().anyMatch(path -> path == null || path.isBlank())
                || oggResourceLocations == null
                || oggResourceLocations.size() != musicFilesInPack.size()
                || oggResourceLocations.stream().anyMatch(Objects::isNull)) {
            return false;
        }

        // エンティティ条件の妥当性チェック
        if (entityConditions != null && !entityConditions.isEmpty()) {
            // entityConditions が指定されている場合、radius は必須
            if (radius == null || radius <= 0) return false;
            // minCount, maxCount の基本的なバリデーション
            if (minCount != null && minCount < 0) return false;
            if (maxCount != null && maxCount < 0) return false;
            if (minCount != null && maxCount != null && minCount > maxCount) return false;

            // entityConditions リスト内の各要素の形式チェック
            for (String entityIdOrTag : entityConditions) {
                if (entityIdOrTag == null || entityIdOrTag.isBlank()) return false; // 空の要素は無効
                String checkString = entityIdOrTag;
                if (checkString.startsWith("!")) {
                    if (checkString.length() == 1) return false; // "!" だけは無効
                    checkString = checkString.substring(1);
                }
                if (checkString.startsWith("#")) {
                    if (checkString.length() == 1) return false; // "#" だけは無効
                }
                try {
                    ResourceLocation.parse(checkString.startsWith("#") ? checkString.substring(1) : checkString);
                } catch (ResourceLocationException e) {
                    return false;
                }
            }
        } else return radius == null && minCount == null && maxCount == null;
        return true;
    }

    @Override
    public String toString() {
        return "MusicDefinition{" +
                "soundPackId='" + soundPackId + '\'' +
                ", musicFilesInPack=" + musicFilesInPack +
                ", playMode='" + playMode + '\'' +
                ", playOrder=" + playOrder +
                ", priority=" + priority +
                ", soundEventKeys=" + soundEventKeys +
                ", oggResourceLocations=" + oggResourceLocations +
                ", biomes=" + biomes +
                ", isNight=" + isNight +
                ", isCombat=" + isCombat +
                ", isVillage=" + isVillage +
                ", minY=" + minY +
                ", maxY=" + maxY +
                ", guiScreen='" + guiScreen + '\'' +
                ", weather=" + weather +
                ", dimensions=" + dimensions +
                ", entityConditions=" + entityConditions +
                ", radius=" + radius +
                ", minCount=" + minCount +
                ", maxCount=" + maxCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicDefinition that = (MusicDefinition) o;
        return Objects.equals(soundPackId, that.soundPackId)
                && Objects.equals(soundEventKeys, that.soundEventKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(soundPackId, soundEventKeys);
    }
}
