package com.pfp.desktop.foundation.audio;

import com.pfp.desktop.foundation.settings.DesktopSettings;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public final class DesktopAudio {
    private static final Map<SoundEffect, AudioClip> CLIPS = new EnumMap<>(SoundEffect.class);

    private static DesktopSettings settings = DesktopSettings.defaults();
    private static MediaPlayer musicPlayer;
    private static AudioTheme activeTheme = AudioTheme.NONE;

    private DesktopAudio() {
    }

    public static void applySettings(DesktopSettings nextSettings) {
        settings = nextSettings == null ? DesktopSettings.defaults() : nextSettings;
        syncMusic();
    }

    public static void play(SoundEffect effect) {
        if (effect == null || !settings.isSoundEnabled() || settings.getSoundVolume() <= 0) {
            return;
        }
        AudioClip clip = CLIPS.computeIfAbsent(effect, DesktopAudio::loadClip);
        if (clip == null) {
            return;
        }
        clip.setVolume(volume(settings.getSoundVolume()));
        clip.play();
    }

    private static void syncMusic() {
        AudioTheme theme = AudioTheme.fromDisplayName(settings.getMusicTheme());
        if (!settings.isMusicEnabled() || settings.getMusicVolume() <= 0 || !theme.hasTrack()) {
            stopMusic();
            activeTheme = AudioTheme.NONE;
            return;
        }
        if (musicPlayer == null || activeTheme != theme) {
            stopMusic();
            Media media = loadMedia(theme.resourcePath());
            if (media == null) {
                activeTheme = AudioTheme.NONE;
                return;
            }
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            activeTheme = theme;
        }
        musicPlayer.setVolume(volume(settings.getMusicVolume()));
        if (musicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            musicPlayer.play();
        }
    }

    private static void stopMusic() {
        if (musicPlayer == null) {
            return;
        }
        musicPlayer.stop();
        musicPlayer.dispose();
        musicPlayer = null;
    }

    private static AudioClip loadClip(SoundEffect effect) {
        URL resource = DesktopAudio.class.getResource(effect.resourcePath());
        return resource == null ? null : new AudioClip(resource.toExternalForm());
    }

    private static Media loadMedia(String resourcePath) {
        URL resource = DesktopAudio.class.getResource(resourcePath);
        return resource == null ? null : new Media(resource.toExternalForm());
    }

    private static double volume(int percent) {
        return Math.max(0, Math.min(100, percent)) / 100.0;
    }
}
