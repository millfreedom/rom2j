package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.sound.MusicPlayer;
import ua.millfreedom.rom2.res.Resources;

import java.util.ArrayList;
import java.util.List;

/**
 * Native class: CBmp64kFrameSequence.
 */
public final class CBmp64kFrameSequence {
    public static final int NATIVE_SIZE = 0x30; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x00
    public final List<CBmp64k> frames = new ArrayList<>();
    //0x14
    public CBmp64k currentFrame;
    //0x18
    public int currentFrameIndex = -1;
    //0x1c
    public final List<String> frameNames = new ArrayList<>();

    /**
     * Native: CBmp64kFrameSequence::CBmp64kFrameSequence @00401000.
     * Fully ported.
     */
    public CBmp64kFrameSequence() {
        initializeEmpty();
    }

    /**
     * Native: CBmp64kFrameSequence::InitializeEmpty @004010BC.
     * Fully ported.
     */
    public void initializeEmpty() {
        frameNames.clear();
        frames.clear();
        currentFrameIndex = -1;
        currentFrame = null;
    }

    /**
     * Native: CBmp64kFrameSequence::LoadFrames @004010EE.
     * Fully ported. Java routes bitmap allocation through the modeled resource manager and mirrors the native per-frame mouse/music update hook.
     */
    public void loadFrames(List<String> frameNames) {
        frames.clear();
        for (int frameIndex = 0; frameIndex < frameNames.size(); frameIndex++) {
            frames.add(new CBmp64k(Resources.path(frameNames.get(frameIndex))));
            updateLoadProgress();
        }
        currentFrameIndex = 0;
        currentFrame = frames.get(currentFrameIndex);
    }

    /**
     * Native: CBmp64kFrameSequence::LoadFramesFromStoredNames @0040123A.
     * Fully ported. Java routes bitmap allocation through the modeled resource manager.
     */
    public void loadFramesFromStoredNames() {
        frames.clear();
        for (int frameIndex = 0; frameIndex < frameNames.size(); frameIndex++) {
            frames.add(new CBmp64k(Resources.path(frameNames.get(frameIndex))));
        }
        currentFrameIndex = 0;
        currentFrame = frames.get(currentFrameIndex);
    }

    /**
     * Native: CBmp64kFrameSequence::AdvanceLoadingCurrentFrame @0040131E.
     * Fully ported. Java clears retained references instead of reproducing native delete/free calls.
     */
    public CBmp64k advanceLoadingCurrentFrame() {
        if (currentFrameIndex == getUpperBound()) {
            currentFrame = null;
            return null;
        }
        currentFrameIndex += 1;
        currentFrame = new CBmp64k(Resources.path(frameNames.get(currentFrameIndex)));
        return currentFrame;
    }

    /**
     * Native: CBmp64kFrameSequence::AdvanceLoopedLoadingCurrentFrame @0040140D.
     * Fully ported. Java clears retained references instead of reproducing native delete/free calls.
     */
    public CBmp64k advanceLoopedLoadingCurrentFrame() {
        currentFrameIndex = (currentFrameIndex + 1) % getUpperBound();
        currentFrame = null;
        currentFrame = new CBmp64k(Resources.path(frameNames.get(currentFrameIndex)));
        return currentFrame;
    }

    /**
     * Native: CBmp64kFrameSequence::ReleaseFrames @004014F1.
     * Fully ported. Java clears retained references instead of reproducing native delete/free calls.
     */
    public void releaseFrames() {
        currentFrame = null;
        frames.clear();
        currentFrameIndex = -1;
    }

    /**
     * Native: CBmp64kFrameSequence::Advance @004015C5.
     * Fully ported.
     */
    public CBmp64k advance() {
        if (currentFrameIndex == getUpperBound()) {
            return null;
        }
        currentFrameIndex += 1;
        currentFrame = frames.get(currentFrameIndex);
        return getCurrentFrame();
    }

    /**
     * Native: CBmp64kFrameSequence::Rewind @00401612.
     * Fully ported.
     */
    public CBmp64k rewind() {
        if (currentFrameIndex == 0) {
            return null;
        }
        currentFrameIndex -= 1;
        currentFrame = frames.get(currentFrameIndex);
        return getCurrentFrame();
    }

    /**
     * Native: CBmp64kFrameSequence::AdvanceLooped @00401658.
     * Fully ported.
     */
    public void advanceLooped() {
        currentFrameIndex = (currentFrameIndex + 1) % getUpperBound();
        currentFrame = frames.get(currentFrameIndex);
        getCurrentFrame();
    }

    /**
     * Native: CBmp64kFrameSequence::DrawCurrentFrame @004016EE.
     * Fully ported.
     */
    public void drawCurrentFrame(int x, int y) {
        currentFrame.draw(x, y, 0, 0, false);
    }

    /**
     * Native: CBmp64kFrameSequence::DrawFrameAt @004016B5.
     * Fully ported.
     */
    public void drawFrameAt(int x, int y, int frameIndex) {
        frames.get(frameIndex).draw(x, y, 0, 0, false);
    }

    /**
     * Native: CBmp64kFrameSequence::CopyFrom @0040171A.
     * Fully ported.
     */
    public void copyFrom(CBmp64kFrameSequence source) {
        copyFramePointersFrom(source);
        currentFrameIndex = source.currentFrameIndex;
        frameNames.clear();
        frameNames.addAll(source.frameNames);
        currentFrame = source.currentFrame;
    }

    /**
     * Native: CBmp64kFrameSequence::GetUpperBound @004019C0.
     * Fully ported.
     */
    public int getUpperBound() {
        return frames.size() - 1;
    }

    /**
     * Native: CBmp64kFrameSequence::CopyFramePointersFrom @00401C20.
     * Fully ported.
     */
    public void copyFramePointersFrom(CBmp64kFrameSequence source) {
        frames.clear();
        frames.addAll(source.frames);
    }

    /**
     * Native: CBmp64kFrameSequence::GetCurrentFrame @00401F30.
     * Fully ported.
     */
    public CBmp64k getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Native: CBmp64kFrameSequence::SetCurrentFrameIndex @004A0F80.
     * Fully ported.
     */
    public void setCurrentFrameIndex(int frameIndex) {
        currentFrameIndex = frameIndex;
    }

    /**
     * Native: CBmp64kFrameSequence::GetCurrentFrameIndex @004A0FA0.
     * Fully ported.
     */
    public int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    /**
     * Native support extracted from CBmp64kFrameSequence::LoadFrames @004010EE.
     */
    private static void updateLoadProgress() {
        MusicPlayer musicPlayer = Globals.mainWindow.musicPlayer;
        if (musicPlayer != null) {
            musicPlayer.updateStreamingPlayback((int) System.currentTimeMillis());
        }
        Globals.mousePointer.update();
    }
}
