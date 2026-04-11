package com.zoma1101.music_player;

import com.mojang.logging.LogUtils;
import com.zoma1101.music_player.client.ClientPlaybackSettings;
import com.zoma1101.music_player.client.overlay.MusicPlaybackObserver;
import com.zoma1101.music_player.client.overlay.NowPlayingOverlayController;
import com.zoma1101.music_player.sound.FadingMusicSoundInstance;
import com.zoma1101.music_player.sound.MusicDefinition;
import com.zoma1101.music_player.sound.PlaybackHealthTracker;
import com.zoma1101.music_player.sound.PlaybackStartNotifier;
import com.zoma1101.music_player.sound.PlaylistNavigator;
import com.zoma1101.music_player.util.GameContextHelper;
import com.zoma1101.music_player.util.MusicConditionEvaluator;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Music_Player.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientMusicManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHECK_INTERVAL_TICKS = 20;

    @Nullable
    private static FadingMusicSoundInstance currentMusicInstance = null;
    @Nullable
    private static String currentMusicSoundEventKey = null;
    @Nullable
    private static MusicDefinition currentMusicDefinition = null;

    private static final List<FadingMusicSoundInstance> fadingOutInstances = new ArrayList<>();
    private static final PlaylistNavigator playlistNavigator = new PlaylistNavigator();
    private static final PlaybackHealthTracker playbackHealthTracker = new PlaybackHealthTracker();
    private static final PlaybackStartNotifier playbackStartNotifier = new PlaybackStartNotifier();
    private static final NowPlayingOverlayController nowPlayingOverlayController = new NowPlayingOverlayController();
    private static final List<MusicPlaybackObserver> playbackObservers = new ArrayList<>();

    private static boolean isStopping = false;
    private static boolean isRecordPlaying = false;
    @Nullable
    private static Boolean lastObservedCombatState = null;
    @Nullable
    private static SoundInstance lastPlayedRecordInstance = null;

    static {
        playbackObservers.add(nowPlayingOverlayController);
        nowPlayingOverlayController.setStyle(ClientPlaybackSettings.getOverlayStyle());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        nowPlayingOverlayController.onClientTick();
        cleanupFadingInstances(soundManager);

        LocalPlayer player = mc.player;
        if (player != null && mc.level != null && player.tickCount % CHECK_INTERVAL_TICKS == 0) {
            if (isRecordPlaying) {
                if (lastPlayedRecordInstance != null && !soundManager.isActive(lastPlayedRecordInstance)) {
                    LOGGER.info("Record music [{}] seems to have stopped. Resuming MOD music checks.", lastPlayedRecordInstance.getLocation());
                    isRecordPlaying = false;
                    lastPlayedRecordInstance = null;
                    clearCurrentTrackKey();
                    stopMusic(false);
                    updateMusic();
                } else if (lastPlayedRecordInstance == null) {
                    LOGGER.warn("isRecordPlaying is true, but lastPlayedRecordInstance is null. Resetting record state.");
                    isRecordPlaying = false;
                    updateMusic();
                } else {
                    if (currentMusicInstance != null) {
                        LOGGER.debug("Record is still playing. Ensuring MOD music is stopped.");
                        stopMusic(true);
                        clearCurrentTrackKey();
                    }
                }
            } else {
                updateMusic();
            }
        }

        if (isStopping) {
            isStopping = false;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        LOGGER.info("Player logged in. Resetting music state.");
        resetPlaybackState(true);
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        LOGGER.info("Player logged out. Stopping music.");
        resetPlaybackState(true);
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance soundBeingPlayed = event.getSound();
        if (soundBeingPlayed == null) {
            return;
        }

        ResourceLocation playingSoundEventLocation = soundBeingPlayed.getLocation();
        SoundSource soundSource = soundBeingPlayed.getSource();
        String playingNamespace = playingSoundEventLocation.getNamespace();

        if (SoundSource.RECORDS.equals(soundSource)) {
            if (!isRecordPlaying || (lastPlayedRecordInstance != null && !lastPlayedRecordInstance.getLocation().equals(playingSoundEventLocation))) {
                LOGGER.info("Record-source music [{}] started.", playingSoundEventLocation);
                isRecordPlaying = true;
                lastPlayedRecordInstance = soundBeingPlayed;
                if (currentMusicInstance != null) {
                    LOGGER.info("Stopping MOD music because a record-source sound started.");
                    stopMusic(true);
                    clearCurrentTrackKey();
                }
            }
            return;
        }

        if (SoundSource.MUSIC.equals(soundSource) && Music_Player.MOD_ID.equals(playingNamespace)) {
            if (isRecordPlaying) {
                LOGGER.debug("[onPlaySound] MOD music [{}] tried to play while a record-source sound is active. Cancelling MOD music.", playingSoundEventLocation);
                event.setSound(null);
                return;
            }

            MusicDefinition def = Music_Player.soundPackManager.getMusicDefinitionByEventKey(playingSoundEventLocation.getPath());
            boolean isTheCorrectModMusic = def != null
                    && currentMusicSoundEventKey != null
                    && currentMusicSoundEventKey.equals(playingSoundEventLocation.getPath());

            if (!isTheCorrectModMusic) {
                event.setSound(null);
            }
            return;
        }

        if (SoundSource.MUSIC.equals(soundSource)) {
            if (isRecordPlaying) {
                return;
            }

            boolean modMusicShouldBePlaying = currentMusicSoundEventKey != null;
            if (modMusicShouldBePlaying) {
                LOGGER.info("[onPlaySound] Override enabled. MOD music should be playing (Key: {}). Cancelling other MUSIC-source sound: {}", currentMusicSoundEventKey, playingSoundEventLocation);
                event.setSound(null);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        nowPlayingOverlayController.render(event);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null || event.getEntity().getId() != localPlayer.getId()) {
            return;
        }

        Entity target = event.getTarget();
        if (target instanceof Enemy || target instanceof Mob) {
            GameContextHelper.registerClientCombatPulse("player_attack");
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null) {
            return;
        }

        Entity sourceEntity = event.getSource().getEntity();
        if (event.getEntity().getId() == localPlayer.getId()) {
            if (sourceEntity instanceof Enemy || sourceEntity instanceof Mob) {
                GameContextHelper.registerClientCombatPulse("player_hurt");
            }
            return;
        }

        if (sourceEntity != null
                && sourceEntity.getId() == localPlayer.getId()
                && (event.getEntity() instanceof Enemy || event.getEntity() instanceof Mob)) {
            GameContextHelper.registerClientCombatPulse("enemy_hurt_by_player");
        }
    }

    public static void onPlaybackSettingsChanged() {
        nowPlayingOverlayController.setStyle(ClientPlaybackSettings.getOverlayStyle());
        if (currentMusicInstance != null) {
            currentMusicInstance.setTargetVolume(resolveTargetVolume(currentMusicDefinition));
        }
    }

    private static void updateMusic() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            if (currentMusicInstance != null || !fadingOutInstances.isEmpty()) {
                LOGGER.warn("Player or Level became null, stopping music.");
                stopMusic(false, true);
            }
            clearCurrentTrackKey();
            currentMusicDefinition = null;
            playlistNavigator.reset();
            playbackHealthTracker.reset();
            return;
        }

        if (isStopping) {
            LOGGER.trace("Music stopping is in progress, skipping music update check.");
            return;
        }

        if (isRecordPlaying) {
            if (currentMusicInstance != null) {
                LOGGER.debug("Record is playing, ensuring MOD music is stopped during updateMusic.");
                stopMusic(true);
                clearCurrentTrackKey();
            }
            return;
        }

        MusicConditionEvaluator.CurrentContext context = MusicConditionEvaluator.getCurrentContext(player, mc.level, mc.screen);
        logCombatStateTransition(player, context);
        List<MusicDefinition> definitions = Music_Player.soundPackManager.getActiveMusicDefinitionsSorted();
        MusicDefinition bestMatch = findBestMatch(definitions, context);
        boolean hasValidDefinition = bestMatch != null && bestMatch.isValid();
        MusicDefinition nextDefinition = hasValidDefinition ? bestMatch : null;
        boolean definitionChanged = nextDefinition != currentMusicDefinition;

        if (definitionChanged) {
            String previousKey = currentMusicSoundEventKey;
            stopMusic(false);
            currentMusicDefinition = nextDefinition;
            clearCurrentTrackKey();
            playbackHealthTracker.reset();

            if (currentMusicDefinition != null) {
                String nextTrackKey = playlistNavigator.nextTrackKey(currentMusicDefinition, true);
                if (nextTrackKey != null) {
                    playMusicByKey(nextTrackKey);
                }
                LOGGER.info("Music definition changed. Previous Key: [{}], New Definition: [{}], Next Track Key: [{}].",
                        previousKey, describeDefinition(currentMusicDefinition), nextTrackKey);
            } else {
                playlistNavigator.reset();
                LOGGER.info("Music definition changed. Previous Key: [{}], New Definition: [none].", previousKey);
            }
            return;
        }

        SoundManager soundManager = mc.getSoundManager();
        boolean shouldBePlaying = currentMusicDefinition != null;
        boolean isActuallyPlaying = currentMusicInstance != null && soundManager.isActive(currentMusicInstance);

        if (!shouldBePlaying) {
            playbackHealthTracker.reset();
            if (isActuallyPlaying) {
                LOGGER.warn("Music should NOT be playing, but instance for key [{}] is active. Stopping.", currentMusicSoundEventKey);
                stopMusic(true);
            }
            clearCurrentTrackKey();
            return;
        }

        mc.getMusicManager().stopPlaying();

        if (currentMusicSoundEventKey == null) {
            String nextTrackKey = playlistNavigator.nextTrackKey(currentMusicDefinition, false);
            if (nextTrackKey != null) {
                playMusicByKey(nextTrackKey);
            }
            return;
        }

        announceTrackStartIfActive(isActuallyPlaying);
        PlaybackHealthTracker.Decision decision = playbackHealthTracker.onHeartbeat(isActuallyPlaying);
        if (isActuallyPlaying || decision == PlaybackHealthTracker.Decision.NONE) {
            return;
        }

        if (decision == PlaybackHealthTracker.Decision.RETRY_CURRENT_TRACK) {
            LOGGER.warn("Track [{}] did not become active. Retrying playback.", currentMusicSoundEventKey);
            playMusicByKey(currentMusicSoundEventKey);
            return;
        }

        String nextTrackKey = playlistNavigator.nextTrackKey(currentMusicDefinition, false);
        if (nextTrackKey != null) {
            playMusicByKey(nextTrackKey);
        } else {
            clearCurrentTrackKey();
        }
    }

    private static void playMusicByKey(String soundEventKey) {
        if (isStopping) {
            LOGGER.debug("Skipping playMusicByKey for key [{}] because isStopping is true.", soundEventKey);
            return;
        }
        if (isRecordPlaying) {
            LOGGER.debug("Skipping playMusicByKey for key [{}] because a record is playing.", soundEventKey);
            return;
        }
        if (soundEventKey == null) {
            LOGGER.warn("playMusicByKey called with null soundEventKey.");
            return;
        }

        try {
            Minecraft mc = Minecraft.getInstance();
            SoundManager soundManager = mc.getSoundManager();
            mc.getMusicManager().stopPlaying();
            ResourceLocation soundEventRl = ResourceLocation.fromNamespaceAndPath(Music_Player.MOD_ID, soundEventKey);

            if (currentMusicInstance != null) {
                if (soundManager.isActive(currentMusicInstance)) {
                    startFadeOut(currentMusicInstance);
                } else {
                    soundManager.stop(currentMusicInstance);
                }
                currentMusicInstance = null;
            }

            FadingMusicSoundInstance nextInstance = new FadingMusicSoundInstance(
                    soundEventRl,
                    resolveTargetVolume(currentMusicDefinition),
                    resolveFadeInTicks(currentMusicDefinition),
                    resolveFadeOutTicks(currentMusicDefinition)
            );
            soundManager.play(nextInstance);
            currentMusicInstance = nextInstance;
            currentMusicSoundEventKey = soundEventKey;
            playbackStartNotifier.onPlaybackRequested(soundEventKey);
            playbackHealthTracker.onTrackRequested(soundEventKey);
            LOGGER.info("Playing music with key: [{}], resolved to RL: [{}]", soundEventKey, soundEventRl);
        } catch (ResourceLocationException e) {
            LOGGER.error("Invalid ResourceLocation format for sound event key [{}] with namespace [{}]: {}",
                    soundEventKey, Music_Player.MOD_ID, e.getMessage());
            currentMusicInstance = null;
        } catch (Exception e) {
            LOGGER.error("Exception occurred trying to play music with key [{}], resolved RL [{}]: {}",
                    soundEventKey, Music_Player.MOD_ID + ":" + soundEventKey, e.getMessage(), e);
            currentMusicInstance = null;
        }
    }

    private static void stopMusic(boolean setStoppingFlag) {
        stopMusic(setStoppingFlag, false);
    }

    private static void stopMusic(boolean setStoppingFlag, boolean immediate) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        boolean hadTrackedMusic = currentMusicSoundEventKey != null;
        if (currentMusicInstance != null) {
            LOGGER.debug("Stopping music instance for key: {}", currentMusicSoundEventKey);
            if (immediate) {
                soundManager.stop(currentMusicInstance);
            } else {
                startFadeOut(currentMusicInstance);
            }
            currentMusicInstance = null;
        }

        if (immediate && !fadingOutInstances.isEmpty()) {
            for (FadingMusicSoundInstance instance : fadingOutInstances) {
                soundManager.stop(instance);
            }
            fadingOutInstances.clear();
        }

        if (setStoppingFlag) {
            isStopping = true;
        }
        playbackHealthTracker.reset();
        playbackStartNotifier.reset();
        if (hadTrackedMusic) {
            notifyTrackStopped();
        }
    }

    private static void startFadeOut(FadingMusicSoundInstance instance) {
        if (instance.isFadingOut()) {
            return;
        }

        instance.startFadeOut();
        if (!instance.isStopped()) {
            fadingOutInstances.add(instance);
        }
    }

    private static void cleanupFadingInstances(SoundManager soundManager) {
        if (fadingOutInstances.isEmpty()) {
            return;
        }
        fadingOutInstances.removeIf(instance -> instance == null || instance.isStopped() || !soundManager.isActive(instance));
    }

    private static void resetPlaybackState(boolean immediateStop) {
        stopMusic(false, immediateStop);
        clearCurrentTrackKey();
        currentMusicDefinition = null;
        playlistNavigator.reset();
        playbackHealthTracker.reset();
        isRecordPlaying = false;
        lastObservedCombatState = null;
        lastPlayedRecordInstance = null;
    }

    @Nullable
    private static MusicDefinition findBestMatch(List<MusicDefinition> definitions, MusicConditionEvaluator.CurrentContext context) {
        return pickBestMatchingDefinition(definitions, context);
    }

    @Nullable
    static MusicDefinition pickBestMatchingDefinition(List<MusicDefinition> definitions, MusicConditionEvaluator.CurrentContext context) {
        List<MusicDefinition> validDefinitions = new ArrayList<>();
        for (MusicDefinition definition : definitions) {
            if (definition.isValid()) {
                validDefinitions.add(definition);
            } else {
                LOGGER.warn("Skipping invalid music definition during match finding: {}", definition);
            }
        }

        if (context.isInCombat) {
            MusicDefinition combatMatch = findFirstMatching(validDefinitions, context, true);
            if (combatMatch != null) {
                LOGGER.debug("Combat context active. Selected combat definition: {}", describeDefinition(combatMatch));
                return combatMatch;
            }
            LOGGER.debug("Combat context active but no combat-only definition matched. Falling back to ambient definitions.");
        }

        return findFirstMatching(validDefinitions, context, false);
    }

    @Nullable
    private static MusicDefinition findFirstMatching(
            List<MusicDefinition> definitions,
            MusicConditionEvaluator.CurrentContext context,
            boolean combatOnly
    ) {
        for (MusicDefinition definition : definitions) {
            if (combatOnly && !Boolean.TRUE.equals(definition.isCombat())) {
                continue;
            }
            if (MusicConditionEvaluator.doesDefinitionMatch(definition, context)) {
                return definition;
            }
        }
        return null;
    }

    private static float resolveTargetVolume(@Nullable MusicDefinition definition) {
        float globalVolume = ClientPlaybackSettings.getMusicVolume();
        float definitionVolume = definition == null ? 1.0f : definition.resolveVolumeMultiplier(1.0f);
        return Mth.clamp(globalVolume * definitionVolume, 0.0f, 1.0f);
    }

    private static int resolveFadeInTicks(@Nullable MusicDefinition definition) {
        int fallback = ClientPlaybackSettings.getFadeInTicks();
        return definition == null ? fallback : definition.resolveFadeInTicks(fallback);
    }

    private static int resolveFadeOutTicks(@Nullable MusicDefinition definition) {
        int fallback = ClientPlaybackSettings.getFadeOutTicks();
        return definition == null ? fallback : definition.resolveFadeOutTicks(fallback);
    }

    private static String formatTrackDisplayName(String soundEventKey) {
        if (soundEventKey == null || soundEventKey.isBlank()) {
            return "";
        }
        int lastSlash = soundEventKey.lastIndexOf('/');
        String trackName = lastSlash >= 0 ? soundEventKey.substring(lastSlash + 1) : soundEventKey;
        return trackName.replace('_', ' ');
    }

    private static void announceTrackStartIfActive(boolean isActuallyPlaying) {
        String startedTrackKey = playbackStartNotifier.onHeartbeat(isActuallyPlaying);
        if (startedTrackKey == null) {
            return;
        }
        notifyTrackStarted(formatTrackDisplayName(startedTrackKey));
    }

    private static void clearCurrentTrackKey() {
        currentMusicSoundEventKey = null;
        playbackStartNotifier.reset();
    }

    private static void notifyTrackStarted(String trackDisplayName) {
        if (trackDisplayName == null || trackDisplayName.isBlank()) {
            return;
        }
        for (MusicPlaybackObserver observer : playbackObservers) {
            observer.onTrackStarted(trackDisplayName);
        }
    }

    private static void notifyTrackStopped() {
        for (MusicPlaybackObserver observer : playbackObservers) {
            observer.onTrackStopped();
        }
    }

    private static String describeDefinition(MusicDefinition definition) {
        return "pack=" + definition.getSoundPackId()
                + ", mode=" + definition.getPlaybackMode()
                + ", tracks=" + definition.getMusicFilesInPack()
                + ", priority=" + definition.getPriority();
    }

    private static void logCombatStateTransition(LocalPlayer player, MusicConditionEvaluator.CurrentContext context) {
        if (lastObservedCombatState == null || lastObservedCombatState.booleanValue() != context.isInCombat) {
            String attacker = player.getLastHurtByMob() == null ? "none" : player.getLastHurtByMob().getType().toString();
            String defender = player.getLastHurtMob() == null ? "none" : player.getLastHurtMob().getType().toString();
            LOGGER.info(
                    "Combat context switched: {} -> {} (health={}, pos={}, lastHurtBy={}, lastHurtMob={})",
                    lastObservedCombatState,
                    context.isInCombat,
                    Mth.floor(player.getHealth() * 10.0F) / 10.0F,
                    player.blockPosition(),
                    attacker,
                    defender
            );
            lastObservedCombatState = context.isInCombat;
        }
    }
}
