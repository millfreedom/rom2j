package ua.millfreedom.rom2.model.sound;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

public final class SoundChannel implements MfcSerializable {
    //0x04
    public byte priority;
    //0x08 Java OpenAL source replacing native IDirectSoundBuffer pointer.
    public final int sourceId;
    //0x0c
    public Sound sound;

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: SoundChannel::New @0045B580.
     * Full port at the Java/OpenAL source allocation boundary.
     */
    SoundChannel(int sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * Native: SoundChannel::Allocate @0045AB9C.
     * Fully ported. SoundSystem owns the Java/OpenAL channel array that replaces the native global channel array.
     */
    public static SoundChannel allocate(byte priority) {
        return SoundSystem.get().allocateChannel(priority);
    }

    /**
     * Native: SoundChannel::Stop @0041F430.
     * Fully ported. SoundSystem owns the Java/OpenAL source state that replaces the native DirectSound buffer pointer.
     */
    public void stop() {
        SoundSystem.get().stopChannel(this);
    }

    /**
     * Native: SoundChannel::SetVolume @0041F4A0.
     * Fully ported. SoundSystem owns the Java/OpenAL source state that replaces the native DirectSound buffer pointer.
     */
    public void setVolume(int volume) {
        SoundSystem.get().setChannelVolume(this, volume);
    }

    /**
     * Native: SoundChannel::GetVolume @0045ADB4.
     * Fully ported. SoundSystem owns the Java/OpenAL source state that replaces the native DirectSound buffer pointer.
     */
    public int getVolume() {
        return SoundSystem.get().getChannelVolume(this);
    }

    /**
     * Native: SoundChannel::StopAndRewind @0041F610.
     * Fully ported. SoundSystem owns the Java/OpenAL source state that replaces the native DirectSound buffer pointer.
     */
    public void stopAndRewind() {
        SoundSystem.get().stopAndRewindChannel(this);
    }

    /**
     * Native: SoundChannel::Play @0045AD66.
     * Fully ported. SoundSystem owns the Java/OpenAL source state that replaces the native DirectSound buffer pointer.
     */
    public void play(boolean loop) {
        SoundSystem.get().playChannel(this, loop);
    }

}
