package com.zoma1101.music_player.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class FadingMusicSoundInstance extends AbstractTickableSoundInstance {
    private final int fadeInTicks;
    private final int fadeOutTicks;
    private int ageTicks = 0;
    private int fadeOutProgressTicks = -1;
    private float targetVolume;

    public FadingMusicSoundInstance(ResourceLocation location, float targetVolume, int fadeInTicks, int fadeOutTicks) {
        super(SoundEvent.createVariableRangeEvent(location), SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.targetVolume = clampVolume(targetVolume);
        this.fadeInTicks = Math.max(0, fadeInTicks);
        this.fadeOutTicks = Math.max(0, fadeOutTicks);
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.relative = true;
        this.looping = false;
        this.delay = 0;
        this.volume = shouldAllowSilentStart(this.fadeInTicks, this.targetVolume) ? 0.0f : this.targetVolume;
    }

    @Override
    public void tick() {
        if (isStopped()) {
            return;
        }

        ageTicks++;

        if (isFadingOut()) {
            fadeOutProgressTicks++;
            if (fadeOutTicks <= 0 || fadeOutProgressTicks >= fadeOutTicks) {
                this.volume = 0.0f;
                stop();
                return;
            }

            float progress = fadeOutProgressTicks / (float) fadeOutTicks;
            this.volume = targetVolume * (1.0f - progress);
            return;
        }

        if (fadeInTicks > 0 && ageTicks <= fadeInTicks) {
            float progress = ageTicks / (float) fadeInTicks;
            this.volume = targetVolume * progress;
            return;
        }

        this.volume = targetVolume;
    }

    public void startFadeOut() {
        if (isStopped() || isFadingOut()) {
            return;
        }

        if (fadeOutTicks <= 0) {
            this.volume = 0.0f;
            stop();
            return;
        }

        fadeOutProgressTicks = 0;
    }

    public boolean isFadingOut() {
        return fadeOutProgressTicks >= 0;
    }

    @Override
    public boolean canStartSilent() {
        return shouldAllowSilentStart(fadeInTicks, targetVolume);
    }

    public void setTargetVolume(float targetVolume) {
        this.targetVolume = clampVolume(targetVolume);
        if (!isFadingOut() && (fadeInTicks <= 0 || ageTicks >= fadeInTicks)) {
            this.volume = this.targetVolume;
        }
    }

    private static float clampVolume(float value) {
        return Mth.clamp(value, 0.0f, 1.0f);
    }

    static boolean shouldAllowSilentStart(int fadeInTicks, float targetVolume) {
        return fadeInTicks > 0 || clampVolume(targetVolume) <= 0.0f;
    }
}
