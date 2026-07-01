package ua.millfreedom.rom2.model.video;

/**
 * Native struct: Panaraming (size 0x10), allocated as `Panaraming[]` by SMKPlayer::OpenWithRegistry @004C3FF1.
 */
public class Panaraming {
    //0x0
    public int startframe;

    //0x4
    public int endframe;

    //0x8
    public int stepx;

    //0xc
    public int stepy;

    /**
     * Native: Panaraming::Panaraming @004C47A0.
     */
    public Panaraming() {
        startframe = 0;
        endframe = 0;
        stepx = 0;
        stepy = 0;
    }
}
