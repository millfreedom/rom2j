package ua.millfreedom.rom2.model.video;

/**
 * Native struct: Fading (size 0x10), allocated as `Fading[]` by SMKPlayer::OpenWithRegistry @004C3FF1.
 */
public class Fading {
    //0x0
    public int startframe;

    //0x4
    public int endframe;

    //0x8
    public float startfade;

    //0xc
    public float endfade;

    /**
     * Native: Fading::Fading @004C4760.
     */
    public Fading() {
        startframe = 0;
        endframe = 0;
        startfade = 0.0f;
        endfade = 0.0f;
    }
}
