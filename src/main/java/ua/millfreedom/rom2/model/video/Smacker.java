package ua.millfreedom.rom2.model.video;

import ua.millfreedom.rom2.model.color.RGB32;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 * Java port of the public libsmacker decoder surface from {@code smacker.h}/{@code smacker.c}.
 */
public final class Smacker implements AutoCloseable {
    public static final int DONE = 0x00;
    public static final int MORE = 0x01;
    public static final int LAST = 0x02;
    public static final int ERROR = -1;

    public static final int MODE_DISK = 0x00;
    public static final int MODE_MEMORY = 0x01;

    public static final int FLAG_Y_NONE = 0x00;
    public static final int FLAG_Y_INTERLACE = 0x01;
    public static final int FLAG_Y_DOUBLE = 0x02;

    public static final int AUDIO_TRACK_0 = 0x01;
    public static final int AUDIO_TRACK_1 = 0x02;
    public static final int AUDIO_TRACK_2 = 0x04;
    public static final int AUDIO_TRACK_3 = 0x08;
    public static final int AUDIO_TRACK_4 = 0x10;
    public static final int AUDIO_TRACK_5 = 0x20;
    public static final int AUDIO_TRACK_6 = 0x40;
    public static final int VIDEO_TRACK = 0x80;

    private static final int TREE_MMAP = 0;
    private static final int TREE_MCLR = 1;
    private static final int TREE_FULL = 2;
    private static final int TREE_TYPE = 3;

    private static final int[] BLOCK_SIZE_TABLE = {
            1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16,
            17, 18, 19, 20, 21, 22, 23, 24,
            25, 26, 27, 28, 29, 30, 31, 32,
            33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 44, 45, 46, 47, 48,
            49, 50, 51, 52, 53, 54, 55, 56,
            57, 58, 59, 128, 256, 512, 1024, 2048
    };

    private static final int[] PALETTE_MAP = {
            0x00, 0x04, 0x08, 0x0C, 0x10, 0x14, 0x18, 0x1C,
            0x20, 0x24, 0x28, 0x2C, 0x30, 0x34, 0x38, 0x3C,
            0x41, 0x45, 0x49, 0x4D, 0x51, 0x55, 0x59, 0x5D,
            0x61, 0x65, 0x69, 0x6D, 0x71, 0x75, 0x79, 0x7D,
            0x82, 0x86, 0x8A, 0x8E, 0x92, 0x96, 0x9A, 0x9E,
            0xA2, 0xA6, 0xAA, 0xAE, 0xB2, 0xB6, 0xBA, 0xBE,
            0xC3, 0xC7, 0xCB, 0xCF, 0xD3, 0xD7, 0xDB, 0xDF,
            0xE3, 0xE7, 0xEB, 0xEF, 0xF3, 0xF7, 0xFB, 0xFF
    };

    private final int mode;
    private final double microsecondsPerFrame;
    private final int frameCount;
    private final boolean ringFrame;
    private final VideoState video;
    private final AudioState[] audio;

    private int currentFrame;
    private boolean closed;
    private byte[] diskBuffer;
    private int[] chunkOffsets;
    private byte[][] chunkData;
    private byte[] keyframes;
    private byte[] frameTypes;
    private int[] chunkSizes;

    /**
     * Port of the shared libsmacker state constructor.
     * not ported.
     */
    private Smacker(int mode,
                    double microsecondsPerFrame,
                    int frameCount,
                    boolean ringFrame,
                    VideoState video,
                    AudioState[] audio,
                    byte[] diskBuffer,
                    int[] chunkOffsets,
                    byte[][] chunkData,
                    byte[] keyframes,
                    byte[] frameTypes,
                    int[] chunkSizes) {
        this.mode = mode;
        this.microsecondsPerFrame = microsecondsPerFrame;
        this.frameCount = frameCount;
        this.ringFrame = ringFrame;
        this.video = video;
        this.audio = audio;
        this.diskBuffer = diskBuffer;
        this.chunkOffsets = chunkOffsets;
        this.chunkData = chunkData;
        this.keyframes = keyframes;
        this.frameTypes = frameTypes;
        this.chunkSizes = chunkSizes;
    }

    /**
     * Port of {@code smk_open_file()} for Java paths.
     * not ported.
     */
    public static Smacker openFile(Path path, int mode) throws IOException {
        Objects.requireNonNull(path, "path");
        validateMode(mode);
        return openGeneric(Files.readAllBytes(path), mode);
    }

    /**
     * Port of {@code smk_open_filepointer()} using a seekable channel as the Java equivalent.
     * not ported.
     */
    public static Smacker openChannel(SeekableByteChannel channel, int mode) throws IOException {
        Objects.requireNonNull(channel, "channel");
        validateMode(mode);
        long remaining = channel.size() - channel.position();
        byte[] data = new byte[(int) remaining];
        int offset = 0;
        while (offset < data.length) {
            int read = channel.read(ByteBuffer.wrap(data, offset, data.length - offset));
            if (read < 0) {
                throw new EOFException("Short read while loading Smacker stream");
            }
            offset += read;
        }
        return openGeneric(data, mode);
    }

    /**
     * Port of {@code smk_open_filepointer()} using a seekable channel as the Java equivalent.
     * not ported.
     */
    public static Smacker fromByteBuffer(ByteBuffer bb, int mode) throws IOException {
        Objects.requireNonNull(bb, "ByteBuffer");
        validateMode(mode);
        byte[] data = new byte[bb.remaining()];
        bb.get(data);
        return openGeneric(data, mode);
    }

    /**
     * Port of {@code smk_open_memory()}.
     * not ported.
     */
    public static Smacker openMemory(byte[] buffer) throws IOException {
        Objects.requireNonNull(buffer, "buffer");
        return openGeneric(buffer, MODE_MEMORY);
    }

    /**
     * Port of {@code smk_open_generic()}.
     * not ported.
     */
    private static Smacker openGeneric(byte[] source, int mode) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(source).order(ByteOrder.LITTLE_ENDIAN);
        try {
            byte[] signature = new byte[3];
            buffer.get(signature);
            if (signature[0] != 'S' || signature[1] != 'M' || signature[2] != 'K') {
                throw new IOException("Invalid Smacker signature");
            }

            int version = clampVersion(buffer.get() & 0xFF);
            int width = buffer.getInt();
            int height = buffer.getInt();
            int frameCount = buffer.getInt();

            double microsecondsPerFrame = decodeMicrosecondsPerFrame(buffer.getInt());
            int videoFlags = buffer.getInt();

            boolean ringFrame = (videoFlags & 0x01) != 0;
            int yScaleMode = FLAG_Y_NONE;
            if ((videoFlags & 0x02) != 0) {
                yScaleMode = FLAG_Y_DOUBLE;
            }
            if ((videoFlags & 0x04) != 0) {
                yScaleMode = FLAG_Y_INTERLACE;
            }

            AudioState[] audio = new AudioState[7];
            int[] maxAudioBuffers = new int[7];
            for (int i = 0; i < audio.length; i++) {
                audio[i] = new AudioState();
                maxAudioBuffers[i] = buffer.getInt();
            }

            int huffTreeChunkSize = buffer.getInt();
            int[] videoTreeSizes = new int[4];
            for (int i = 0; i < videoTreeSizes.length; i++) {
                videoTreeSizes[i] = buffer.getInt();
            }

            for (int i = 0; i < audio.length; i++) {
                int trackInfo = buffer.getInt();
                if ((trackInfo & 0x4000_0000) == 0) {
                    continue;
                }

                audio[i].exists = true;
                audio[i].buffer = new byte[maxAudioBuffers[i]];
                if ((trackInfo & 0x8000_0000) != 0) {
                    audio[i].compression = AudioState.COMPRESSION_SMK_DPCM;
                }
                if ((trackInfo & 0x0C00_0000) != 0) {
                    audio[i].compression = AudioState.COMPRESSION_BINK;
                }
                audio[i].bitDepth = (trackInfo & 0x2000_0000) != 0 ? 16 : 8;
                audio[i].channels = (trackInfo & 0x1000_0000) != 0 ? 2 : 1;
                audio[i].sampleRate = trackInfo & 0x00FF_FFFF;
            }

            buffer.getInt();
            int totalChunks = frameCount + (ringFrame ? 1 : 0);
            byte[] keyframes = new byte[totalChunks];
            int[] chunkSizes = new int[totalChunks];
            for (int i = 0; i < totalChunks; i++) {
                int chunkSize = buffer.getInt();
                if ((chunkSize & 0x01) != 0) {
                    keyframes[i] = 1;
                }
                chunkSize &= 0xFFFF_FFFC;
                chunkSizes[i] = chunkSize;
            }

            byte[] frameTypes = new byte[totalChunks];
            buffer.get(frameTypes);

            byte[] huffTreeChunk = new byte[huffTreeChunkSize];
            buffer.get(huffTreeChunk);
            BitStream treeStream = new BitStream(huffTreeChunk, 0, huffTreeChunk.length);
            VideoState video = new VideoState(width, height, yScaleMode, version);
            for (int i = 0; i < video.trees.length; i++) {
                video.trees[i] = Huff16Tree.build(treeStream, videoTreeSizes[i]);
            }

            byte[] diskBuffer = null;
            int[] chunkOffsets = null;
            byte[][] chunkData = null;
            if (mode == MODE_MEMORY) {
                chunkData = new byte[totalChunks][];
                for (int i = 0; i < totalChunks; i++) {
                    chunkData[i] = new byte[chunkSizes[i]];
                    buffer.get(chunkData[i]);
                }
            } else {
                diskBuffer = source;
                chunkOffsets = new int[totalChunks];
                for (int i = 0; i < totalChunks; i++) {
                    chunkOffsets[i] = buffer.position();
                    buffer.position(buffer.position() + chunkSizes[i]);
                }
            }

            return new Smacker(
                    mode,
                    microsecondsPerFrame,
                    frameCount,
                    ringFrame,
                    video,
                    audio,
                    diskBuffer,
                    chunkOffsets,
                    chunkData,
                    keyframes,
                    frameTypes,
                    chunkSizes
            );
        } catch (BufferUnderflowException | IllegalArgumentException e) {
            throw new EOFException("Smacker stream truncated");
        }
    }

    /**
     * Port of {@code smk_close()}.
     * not ported.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        diskBuffer = null;
        chunkOffsets = null;
        chunkData = null;
        keyframes = null;
        frameTypes = null;
        chunkSizes = null;
        video.clear();
        for (AudioState track : audio) {
            track.clear();
        }
    }

    /**
     * Port of {@code smk_info_all()}.
     * not ported.
     */
    public SMKFileInfo infoAll() {
        requireOpen();
        return new SMKFileInfo(currentFrame % frameCount, frameCount, microsecondsPerFrame);
    }

    /**
     * Port of {@code smk_info_video()}.
     * not ported.
     */
    public SMKVideoInfo infoVideo() {
        requireOpen();
        return new SMKVideoInfo(video.width, video.height, video.yScaleMode);
    }

    /**
     * Port of {@code smk_info_audio()}.
     * not ported.
     */
    public SMKAudioInfo infoAudio() {
        requireOpen();
        int trackMask = 0;
        int[] channels = new int[7];
        int[] bitDepth = new int[7];
        long[] audioRate = new long[7];
        for (int i = 0; i < audio.length; i++) {
            if (audio[i].exists) {
                trackMask |= (1 << i);
            }
            channels[i] = audio[i].channels;
            bitDepth[i] = audio[i].bitDepth;
            audioRate[i] = Integer.toUnsignedLong(audio[i].sampleRate);
        }
        return new SMKAudioInfo(trackMask, channels, bitDepth, audioRate);
    }

    /**
     * Port of {@code smk_enable_all()}.
     * not ported.
     */
    public void enableAll(int mask) {
        requireOpen();
        video.enabled = (mask & VIDEO_TRACK) != 0;
        for (int i = 0; i < audio.length; i++) {
            if (audio[i].exists) {
                audio[i].enabled = (mask & (1 << i)) != 0;
            }
        }
    }

    /**
     * Port of {@code smk_enable_video()}.
     * not ported.
     */
    public void enableVideo(boolean enable) {
        requireOpen();
        video.enabled = enable;
    }

    /**
     * Port of {@code smk_enable_audio()}.
     * not ported.
     */
    public void enableAudio(int track, boolean enable) {
        requireOpen();
        audio[validateTrack(track)].enabled = enable;
    }

    /**
     * Port of {@code smk_get_palette()}.
     * not ported.
     */
    public int[] getPalette() {
        requireOpen();
        return video.palette;
    }

    /**
     * Port of {@code smk_get_video()}.
     * not ported.
     */
    public int[] getVideo() {
        requireOpen();
        return video.frame;
    }

    /**
     * Port of {@code smk_get_audio()}.
     * not ported.
     */
    public byte[] getAudio(int track) {
        requireOpen();
        return audio[validateTrack(track)].buffer;
    }

    /**
     * Port of {@code smk_get_audio_size()}.
     * not ported.
     */
    public int getAudioSize(int track) {
        requireOpen();
        return audio[validateTrack(track)].bufferSize;
    }

    /**
     * Port of {@code smk_first()}.
     * not ported.
     */
    public int first() throws IOException {
        requireOpen();
        currentFrame = 0;
        render();
        return frameCount == 1 ? LAST : MORE;
    }

    /**
     * not ported. Resets playback so the next {@link #next()} call renders from the first frame again.
     */
    public void rewind() {
        requireOpen();
        currentFrame = -1;
    }

    /**
     * Port of {@code smk_next()}.
     * not ported.
     */
    public int next() throws IOException {
        requireOpen();
        if (currentFrame + 1 < totalChunkCount()) {
            currentFrame++;
            render();
            return currentFrame + 1 == totalChunkCount() ? LAST : MORE;
        }
        if (ringFrame) {
            currentFrame = 1;
            render();
            return currentFrame + 1 == totalChunkCount() ? LAST : MORE;
        }
        return DONE;
    }

    /**
     * Port of {@code smk_seek_keyframe()}.
     * not ported.
     */
    public int seekKeyframe(int frame) throws IOException {
        requireOpen();
        if (frame < 0 || frame >= totalChunkCount()) {
            throw new IndexOutOfBoundsException("Frame out of bounds: " + frame);
        }
        currentFrame = frame;
        while (currentFrame > 0 && keyframes[currentFrame] == 0) {
            currentFrame--;
        }
        render();
        return 0;
    }

    /**
     * Port of {@code smk_render()}.
     * not ported.
     */
    private void render() throws IOException {
        int remaining = chunkSizes[currentFrame];
        if (remaining == 0) {
            throw new IOException("Chunk size is zero for frame " + currentFrame);
        }

        byte[] chunk;
        int offset;
        if (mode == MODE_MEMORY) {
            chunk = chunkData[currentFrame];
            if (chunk == null) {
                throw new IOException("Chunk data is missing for frame " + currentFrame);
            }
            offset = 0;
        } else {
            chunk = diskBuffer;
            offset = chunkOffsets[currentFrame];
            if (chunk == null) {
                throw new IOException("Disk buffer has been released");
            }
        }

        int frameType = frameTypes[currentFrame] & 0xFF;
        if ((frameType & 0x01) != 0) {
            int size = 4 * (chunk[offset] & 0xFF);
            if (size <= 0 || size > remaining) {
                throw new IOException("Invalid palette record size " + size + " for frame " + currentFrame);
            }
            if (video.enabled) {
                renderPalette(video, chunk, offset + 1, size - 1);
            }
            offset += size;
            remaining -= size;
        }

        for (int track = 0; track < audio.length; track++) {
            if ((frameType & (0x02 << track)) == 0) {
                audio[track].bufferSize = 0;
                continue;
            }
            if (remaining < 4) {
                throw new IOException("Insufficient audio header data for frame " + currentFrame + ", track " + track);
            }
            int size = readIntLE(chunk, offset);
            if (size < 4 || size > remaining) {
                throw new IOException("Invalid audio chunk size " + size + " for track " + track);
            }
            if (audio[track].enabled) {
                renderAudio(audio[track], chunk, offset + 4, size - 4);
            } else {
                audio[track].bufferSize = 0;
            }
            offset += size;
            remaining -= size;
        }

        if (video.enabled) {
            renderVideo(video, chunk, offset, remaining);
        }
    }

    /**
     * Port of {@code smk_render_palette()}.
     * not ported.
     */
    private static void renderPalette(VideoState state, byte[] source, int offset, int size) throws IOException {
        int index = 0;
        int[] oldPalette = state.palette.clone();
        while (index < 256 && size > 0) {
            int control = source[offset] & 0xFF;
            if ((control & 0x80) != 0) {
                int count = (control & 0x7F) + 1;
                offset++;
                size--;
                if (index + count > 256) {
                    throw new IOException("Palette skip overflow");
                }
                index += count;
            } else if ((control & 0x40) != 0) {
                if (size < 2) {
                    throw new IOException("Palette copy block truncated");
                }
                int count = (control & 0x3F) + 1;
                offset++;
                size--;
                int src = source[offset] & 0xFF;
                offset++;
                size--;
                if (index + count > 256 || src + count > 256 || (src < index && src + count > index)) {
                    throw new IOException("Palette copy overflow");
                }
                System.arraycopy(oldPalette, src, state.palette, index, count);
                index += count;
            } else {
                if (size < 3) {
                    throw new IOException("Palette literal block truncated");
                }
                int red = source[offset] & 0xFF;
                int green = source[offset + 1] & 0xFF;
                int blue = source[offset + 2] & 0xFF;
                if ((red | green | blue) > 0x3F) {
                    throw new IOException("Palette component exceeds 6-bit range");
                }
                state.palette[index] = RGB32.from(PALETTE_MAP[red], PALETTE_MAP[green], PALETTE_MAP[blue]);
                offset += 3;
                size -= 3;
                index++;
            }
        }
        if (index < 256) {
            throw new IOException("Palette did not fully decode");
        }
    }

    /**
     * Port of {@code smk_render_video()}.
     * not ported.
     */
    private static void renderVideo(VideoState state, byte[] source, int offset, int size) throws IOException {
        BitStream bitStream = new BitStream(source, offset, size);
        for (Huff16Tree tree : state.trees) {
            tree.resetCache();
        }

        int row = 0;
        int col = 0;
        while (row < state.height) {
            int unpack = state.trees[TREE_TYPE].lookup(bitStream);
            int type = unpack & 0x0003;
            int blockLength = (unpack & 0x00FC) >>> 2;
            int typeData = (unpack & 0xFF00) >>> 8;

            if (type == 1 && state.version == '4') {
                int bit = bitStream.read1();
                if (bit != 0) {
                    type = 4;
                } else if (bitStream.read1() != 0) {
                    type = 5;
                }
            }

            int iterations = BLOCK_SIZE_TABLE[blockLength];
            for (int j = 0; j < iterations && row < state.height; j++) {
                int skip = row * state.width + col;
                switch (type) {
                    case 0 -> renderMonoBlock(state, bitStream, skip);
                    case 1 -> renderFullBlock(state, bitStream, skip);
                    case 2 -> {
                    }
                    case 3 -> renderSolidBlock(state, skip, typeData);
                    case 4 -> renderDoubleBlock(state, bitStream, skip);
                    case 5 -> renderHalfBlock(state, bitStream, skip);
                    default -> throw new IOException("Unsupported Smacker block type: " + type);
                }

                col += 4;
                if (col >= state.width) {
                    col = 0;
                    row += 4;
                }
            }
        }
    }

    /**
     * Port of the TYPE-0 monochrome video block path in {@code smk_render_video()}.
     * not ported.
     */
    private static void renderMonoBlock(VideoState state, BitStream bitStream, int skip) throws IOException {
        int colors = state.trees[TREE_MCLR].lookup(bitStream);
        int color1 = (colors >>> 8) & 0xFF;
        int color2 = colors & 0xFF;
        int mask = state.trees[TREE_MMAP].lookup(bitStream);
        int temp = 0x01;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                state.frame[skip + x] = (mask & temp) != 0 ? color1 : color2;
                temp <<= 1;
            }
            skip += state.width;
        }
    }

    /**
     * Port of the TYPE-1 full video block path in {@code smk_render_video()}.
     * not ported.
     */
    private static void renderFullBlock(VideoState state, BitStream bitStream, int skip) throws IOException {
        for (int y = 0; y < 4; y++) {
            int unpack = state.trees[TREE_FULL].lookup(bitStream);
            state.frame[skip + 3] = (unpack >>> 8) & 0xFF;
            state.frame[skip + 2] = unpack & 0xFF;

            unpack = state.trees[TREE_FULL].lookup(bitStream);
            state.frame[skip + 1] = (unpack >>> 8) & 0xFF;
            state.frame[skip] = unpack & 0xFF;
            skip += state.width;
        }
    }

    /**
     * Port of the TYPE-3 solid video block path in {@code smk_render_video()}.
     * not ported.
     */
    private static void renderSolidBlock(VideoState state, int skip, int typeData) {
        Arrays.fill(state.frame, skip, skip + 4, typeData);
        skip += state.width;
        Arrays.fill(state.frame, skip, skip + 4, typeData);
        skip += state.width;
        Arrays.fill(state.frame, skip, skip + 4, typeData);
        skip += state.width;
        Arrays.fill(state.frame, skip, skip + 4, typeData);
    }

    /**
     * Port of the Smacker v4 TYPE-4 double video block path in {@code smk_render_video()}.
     * not ported.
     */
    private static void renderDoubleBlock(VideoState state, BitStream bitStream, int skip) throws IOException {
        for (int block = 0; block < 2; block++) {
            int unpack = state.trees[TREE_FULL].lookup(bitStream);
            for (int repeat = 0; repeat < 2; repeat++) {
                Arrays.fill(state.frame, skip, skip + 2, unpack & 0xFF);
                Arrays.fill(state.frame, skip + 2, skip + 4, (unpack >>> 8) & 0xFF);
                skip += state.width;
            }
        }
    }

    /**
     * Port of the Smacker v4 TYPE-5 half video block path in {@code smk_render_video()}.
     * not ported.
     */
    private static void renderHalfBlock(VideoState state, BitStream bitStream, int skip) throws IOException {
        for (int block = 0; block < 2; block++) {
            int unpack = state.trees[TREE_FULL].lookup(bitStream);
            state.frame[skip + 3] = (unpack >>> 8) & 0xFF;
            state.frame[skip + 2] = unpack & 0xFF;
            state.frame[skip + state.width + 3] = (unpack >>> 8) & 0xFF;
            state.frame[skip + state.width + 2] = unpack & 0xFF;

            unpack = state.trees[TREE_FULL].lookup(bitStream);
            state.frame[skip + 1] = (unpack >>> 8) & 0xFF;
            state.frame[skip] = unpack & 0xFF;
            state.frame[skip + state.width + 1] = (unpack >>> 8) & 0xFF;
            state.frame[skip + state.width] = unpack & 0xFF;
            skip += state.width << 1;
        }
    }

    /**
     * Port of {@code smk_render_audio()}.
     * not ported.
     */
    private static void renderAudio(AudioState state, byte[] source, int offset, int size) throws IOException {
        if (state.compression == AudioState.COMPRESSION_RAW_PCM) {
            if (state.buffer == null || size > state.buffer.length) {
                throw new IOException("Audio buffer too small for raw PCM chunk");
            }
            state.bufferSize = size;
            System.arraycopy(source, offset, state.buffer, 0, size);
            return;
        }
        if (state.compression == AudioState.COMPRESSION_BINK) {
            throw new IOException("Bink audio is unsupported by libsmacker and Smacker");
        }
        if (size < 4) {
            throw new IOException("Compressed Smacker audio chunk is missing its output-size header");
        }

        state.bufferSize = readIntLE(source, offset);
        if (state.buffer == null || state.bufferSize < 0 || state.bufferSize > state.buffer.length) {
            throw new IOException("Audio buffer too small for decoded Smacker DPCM");
        }
        offset += 4;
        size -= 4;

        BitStream bitStream = new BitStream(source, offset, size);
        if (bitStream.read1() == 0) {
            throw new IOException("Smacker audio tree stream is missing its initial set bit");
        }

        bitStream.read1();
        bitStream.read1();

        Huff8Tree[] audioTrees = new Huff8Tree[4];
        audioTrees[0] = Huff8Tree.build(bitStream);
        int sampleIndex = 1;
        int byteCount = 1;

        if (state.bitDepth == 16) {
            audioTrees[1] = Huff8Tree.build(bitStream);
            byteCount = 2;
        }
        if (state.channels == 2) {
            audioTrees[2] = Huff8Tree.build(bitStream);
            sampleIndex = 2;
            byteCount = 2;
            if (state.bitDepth == 16) {
                audioTrees[3] = Huff8Tree.build(bitStream);
                byteCount = 4;
            }
        }

        if (state.channels == 2) {
            int unpack = bitStream.read8();
            if (state.bitDepth == 16) {
                short sample = (short) (bitStream.read8() | (unpack << 8));
                putShortLE(state.buffer, 1, sample);
            } else {
                state.buffer[1] = (byte) unpack;
            }
        }

        int unpack = bitStream.read8();
        if (state.bitDepth == 16) {
            short sample = (short) (bitStream.read8() | (unpack << 8));
            putShortLE(state.buffer, 0, sample);
        } else {
            state.buffer[0] = (byte) unpack;
        }

        while (byteCount < state.bufferSize) {
            if (state.bitDepth == 8) {
                unpack = audioTrees[0].lookup(bitStream);
                int previous = state.buffer[sampleIndex - state.channels] & 0xFF;
                state.buffer[sampleIndex] = (byte) ((previous + (byte) unpack) & 0xFF);
                sampleIndex++;
                byteCount++;
            } else {
                int low = audioTrees[0].lookup(bitStream);
                int high = audioTrees[1].lookup(bitStream);
                short delta = (short) (low | (high << 8));
                short previous = getShortLE(state.buffer, sampleIndex - state.channels);
                putShortLE(state.buffer, sampleIndex, (short) (previous + delta));
                sampleIndex++;
                byteCount += 2;
            }

            if (state.channels == 2) {
                if (state.bitDepth == 8) {
                    unpack = audioTrees[2].lookup(bitStream);
                    int previous = state.buffer[sampleIndex - 2] & 0xFF;
                    state.buffer[sampleIndex] = (byte) ((previous + (byte) unpack) & 0xFF);
                    sampleIndex++;
                    byteCount++;
                } else {
                    int low = audioTrees[2].lookup(bitStream);
                    int high = audioTrees[3].lookup(bitStream);
                    short delta = (short) (low | (high << 8));
                    short previous = getShortLE(state.buffer, sampleIndex - 2);
                    putShortLE(state.buffer, sampleIndex, (short) (previous + delta));
                    sampleIndex++;
                    byteCount += 2;
                }
            }
        }
    }

    /**
     * Port of the libsmacker mode validation used by the open entry points.
     * not ported.
     */
    private static void validateMode(int mode) {
        if (mode != MODE_DISK && mode != MODE_MEMORY) {
            throw new IllegalArgumentException("Unsupported Smacker mode: " + mode);
        }
    }

    /**
     * Port of the libsmacker version fallback path in {@code smk_open_generic()}.
     * not ported.
     */
    private static int clampVersion(int version) {
        if (version == '2' || version == '4') {
            return version;
        }
        return version < '4' ? '2' : '4';
    }

    /**
     * Port of the FPS-to-microseconds-per-frame conversion in {@code smk_open_generic()}.
     * not ported.
     */
    private static double decodeMicrosecondsPerFrame(int value) {
        if (value > 0) {
            return value * 1000.0;
        }
        if (value < 0) {
            return value * -10.0;
        }
        return 100_000.0;
    }

    /**
     * Port of the repeated non-null state guards used across the libsmacker API.
     * not ported.
     */
    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("Smacker is closed");
        }
    }

    /**
     * Port of the track-index assumptions used by the audio getters and toggles.
     * not ported.
     */
    private static int validateTrack(int track) {
        if (track < 0 || track >= 7) {
            throw new IndexOutOfBoundsException("Track out of bounds: " + track);
        }
        return track;
    }

    /**
     * Port of the repeated frame-count-plus-ring-frame arithmetic in libsmacker.
     * not ported.
     */
    private int totalChunkCount() {
        return frameCount + (ringFrame ? 1 : 0);
    }

    /**
     * Port of the libsmacker little-endian 32-bit reads used for chunk-size headers.
     * not ported.
     */
    private static int readIntLE(byte[] buffer, int offset) {
        return (buffer[offset] & 0xFF)
                | ((buffer[offset + 1] & 0xFF) << 8)
                | ((buffer[offset + 2] & 0xFF) << 16)
                | ((buffer[offset + 3] & 0xFF) << 24);
    }

    /**
     * Java helper for storing the same little-endian 16-bit PCM layout that libsmacker emits on little-endian hosts.
     * not ported.
     */
    private static void putShortLE(byte[] buffer, int sampleIndex, short value) {
        int offset = sampleIndex * 2;
        buffer[offset] = (byte) value;
        buffer[offset + 1] = (byte) (value >>> 8);
    }

    /**
     * Java helper for loading the same little-endian 16-bit PCM layout that libsmacker emits on little-endian hosts.
     * not ported.
     */
    private static short getShortLE(byte[] buffer, int sampleIndex) {
        int offset = sampleIndex * 2;
        return (short) ((buffer[offset] & 0xFF) | ((buffer[offset + 1] & 0xFF) << 8));
    }

    /**
     * Port of {@code struct smk_bit_t} and its helpers.
     * not ported.
     */
    private static final class BitStream {
        private final byte[] buffer;
        private final int end;
        private int index;
        private int bitNumber;

        /**
         * Port of {@code smk_bs_init()}.
         * not ported.
         */
        private BitStream(byte[] buffer, int offset, int size) {
            this.buffer = buffer;
            this.index = offset;
            this.end = offset + size;
        }

        /**
         * Port of {@code smk_bs_read_1()}.
         * not ported.
         */
        private int read1() throws EOFException {
            if (index >= end) {
                throw new EOFException("Smacker bitstream exhausted");
            }
            int value = ((buffer[index] & 0xFF) >>> bitNumber) & 0x01;
            if (bitNumber >= 7) {
                index++;
                bitNumber = 0;
            } else {
                bitNumber++;
            }
            return value;
        }

        /**
         * Port of {@code smk_bs_read_8()}.
         * not ported.
         */
        private int read8() throws EOFException {
            if (index + (bitNumber > 0 ? 1 : 0) >= end) {
                throw new EOFException("Smacker bitstream exhausted");
            }
            if (bitNumber == 0) {
                return buffer[index++] & 0xFF;
            }
            int value = (buffer[index] & 0xFF) >>> bitNumber;
            index++;
            return value | (((buffer[index] & 0xFF) << (8 - bitNumber)) & 0xFF);
        }
    }

    /**
     * Port of {@code struct smk_huff8_t} plus {@code smk_huff8_build()} and {@code smk_huff8_lookup()}.
     * not ported.
     */
    private static final class Huff8Tree {
        private static final int BRANCH = 0x8000;
        private static final int LEAF_MASK = 0x7FFF;

        private final int[] tree = new int[511];
        private int size;

        /**
         * Port of {@code smk_huff8_build()}.
         * not ported.
         */
        private static Huff8Tree build(BitStream bitStream) throws IOException {
            Huff8Tree tree = new Huff8Tree();
            int bit = bitStream.read1();
            tree.size = 0;
            if (bit != 0) {
                tree.buildRec(bitStream);
            } else {
                tree.tree[0] = 0;
            }
            if (bitStream.read1() != 0) {
                throw new IOException("Smacker huff8 tree is missing its trailing zero bit");
            }
            return tree;
        }

        /**
         * Port of {@code _smk_huff8_build_rec()}.
         * not ported.
         */
        private void buildRec(BitStream bitStream) throws IOException {
            if (size >= tree.length) {
                throw new IOException("Smacker huff8 tree exceeded its fixed limit");
            }
            int bit = bitStream.read1();
            if (bit != 0) {
                int value = size++;
                buildRec(bitStream);
                tree[value] = BRANCH | size;
                buildRec(bitStream);
            } else {
                tree[size++] = bitStream.read8();
            }
        }

        /**
         * Port of {@code smk_huff8_lookup()}.
         * not ported.
         */
        private int lookup(BitStream bitStream) throws IOException {
            int index = 0;
            while ((tree[index] & BRANCH) != 0) {
                int bit = bitStream.read1();
                index = bit != 0 ? tree[index] & LEAF_MASK : index + 1;
            }
            return tree[index];
        }
    }

    /**
     * Port of {@code struct smk_huff16_t} plus {@code smk_huff16_build()} and {@code smk_huff16_lookup()}.
     * not ported.
     */
    private static final class Huff16Tree {
        private static final int BRANCH = 0x8000_0000;
        private static final int CACHE = 0x4000_0000;
        private static final int LEAF_MASK = 0x3FFF_FFFF;

        private int[] tree;
        private int size;
        private final int[] cache = new int[3];

        /**
         * Port of {@code smk_huff16_build()}.
         * not ported.
         */
        private static Huff16Tree build(BitStream bitStream, int allocationSize) throws IOException {
            Huff16Tree tree = new Huff16Tree();
            int bit = bitStream.read1();
            tree.size = 0;
            if (bit != 0) {
                Huff8Tree low8 = Huff8Tree.build(bitStream);
                Huff8Tree high8 = Huff8Tree.build(bitStream);
                for (int i = 0; i < tree.cache.length; i++) {
                    int low = bitStream.read8();
                    int high = bitStream.read8();
                    tree.cache[i] = low | (high << 8);
                }
                if (allocationSize < 12 || allocationSize % 4 != 0) {
                    throw new IOException("Illegal huff16 allocation size: " + allocationSize);
                }
                int limit = (allocationSize - 12) / 4;
                tree.tree = new int[limit];
                tree.buildRec(bitStream, low8, high8, limit);
                if (limit != tree.size) {
                    throw new IOException("Smacker huff16 tree did not fully decode");
                }
            } else {
                tree.tree = new int[]{0};
            }
            if (bitStream.read1() != 0) {
                throw new IOException("Smacker huff16 tree is missing its trailing zero bit");
            }
            return tree;
        }

        /**
         * Port of {@code _smk_huff16_build_rec()}.
         * not ported.
         */
        private void buildRec(BitStream bitStream, Huff8Tree low8, Huff8Tree high8, int limit) throws IOException {
            if (size >= limit) {
                throw new IOException("Smacker huff16 tree exceeded its declared size");
            }
            int bit = bitStream.read1();
            if (bit != 0) {
                int value = size++;
                buildRec(bitStream, low8, high8, limit);
                tree[value] = BRANCH | size;
                buildRec(bitStream, low8, high8, limit);
                return;
            }

            int low = low8.lookup(bitStream);
            int high = high8.lookup(bitStream);
            int value = low | (high << 8);
            if (value == cache[0]) {
                value = CACHE;
            } else if (value == cache[1]) {
                value = CACHE | 1;
            } else if (value == cache[2]) {
                value = CACHE | 2;
            }
            tree[size++] = value;
        }

        /**
         * Port of {@code smk_huff16_lookup()}.
         * not ported.
         */
        private int lookup(BitStream bitStream) throws IOException {
            int index = 0;
            while ((tree[index] & BRANCH) != 0) {
                int bit = bitStream.read1();
                index = bit != 0 ? tree[index] & LEAF_MASK : index + 1;
            }

            int value = tree[index];
            if ((value & CACHE) != 0) {
                value = cache[value & LEAF_MASK];
            }
            if (cache[0] != value) {
                cache[2] = cache[1];
                cache[1] = cache[0];
                cache[0] = value;
            }
            return value;
        }

        /**
         * Port of the per-frame video-cache reset in {@code smk_render_video()}.
         * not ported.
         */
        private void resetCache() {
            Arrays.fill(cache, 0);
        }
    }

    /**
     * Port of {@code struct smk_video_t}.
     * not ported.
     */
    private static final class VideoState {
        private boolean enabled;
        private final int width;
        private final int height;
        private final int yScaleMode;
        private final int version;
        private final Huff16Tree[] trees = new Huff16Tree[4];
        private int[] palette;
        private int[] frame;

        /**
         * Port of the video-state initialization done by {@code smk_open_generic()}.
         * not ported.
         */
        private VideoState(int width, int height, int yScaleMode, int version) {
            this.width = width;
            this.height = height;
            this.yScaleMode = yScaleMode;
            this.version = version;
            this.palette = new int[256];
            Arrays.fill(this.palette, RGB32.BLACK);
            this.frame = new int[width * height];
        }

        /**
         * Java resource-release helper for the ported video state.
         * not ported.
         */
        private void clear() {
            palette = new int[0];
            frame = new int[0];
            Arrays.fill(trees, null);
        }
    }

    /**
     * Port of {@code struct smk_audio_t}.
     * not ported.
     */
    private static final class AudioState {
        private static final int COMPRESSION_RAW_PCM = 0;
        private static final int COMPRESSION_SMK_DPCM = 1;
        private static final int COMPRESSION_BINK = 2;

        private boolean exists;
        private boolean enabled;
        private int channels;
        private int bitDepth;
        private int sampleRate;
        private int compression;
        private byte[] buffer;
        private int bufferSize;

        /**
         * Java resource-release helper for the ported audio state.
         * not ported.
         */
        private void clear() {
            buffer = null;
            bufferSize = 0;
            enabled = false;
            exists = false;
        }
    }

    public record SMKFileInfo(int frame, int frameCount, double microsecondsPerFrame) {
    }

    public record SMKVideoInfo(int width, int height, int yScaleMode) {
    }

    public record SMKAudioInfo(int trackMask, int[] channels, int[] bitDepth, long[] audioRate) {
    }
}
