package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.res.Resources;

import java.util.Locale;

import static ua.millfreedom.rom2.model.enums.MessageCodes.ROLE_DIALOG_ADVANCE_PART;
import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_TIP_BY_ID;

/**
 * Native class: RoleKeyDialogVisualObject.
 * Purpose: centered role-dialog keyed by script marker, with per-part text, speech, and optional NPC portrait state.
 */
public class RoleKeyDialogVisualObject extends CenteredDialogVisualObject {
    public static final int NATIVE_SIZE = 0x88; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int ROLE_TEXT_CHILD_ID = 10;
    private static final int ROLE_PORTRAIT_CHILD_ID = 0x0C;
    private static final int PLAYER_NPC_ID = 0x15;
    private static final int UNIT_FLAG_MAGIC_CLASS = 0x02;
    private static final int UNIT_FLAG_FEMALE = 0x04;
    private static final byte SPEECH_PRIORITY = (byte) 0x80;
    private static final String PART_DIRECTIVE = "part=";
    private static final String NPC_DIRECTIVE = "npc=";
    private static final String NPC_ALIVE_DIRECTIVE = "npcalive=";
    private static final String NPC_DEAD_DIRECTIVE = "npcdead=";
    private static final String SOUND_DIRECTIVE = "sound=";
    private static final String TUNE_DIRECTIVE = "tune=";
    private static final String TIPS_DIRECTIVE = "tips=";
    private static final String TALK_KEY = "talk";
    private static final String ABOUT_KEY = "about";
    private static final String ACCEPT_KEY = "accept";
    private static final String REJECT_KEY = "reject";
    private static final String KEEPER_KEY = "keeper";
    private static final String MAN_KEY = "man";
    private static final String GUARD_KEY = "guard";

    //0x68
    public int currentPartIndex;
    //0x6c
    public String key;
    //0x70
    public Sound activeSpeechSound;
    //0x74
    public String roleScriptBuffer;
    //0x78
    public int roleScriptBufferSize;
    //0x7c
    public int hasNpcDirective;
    //0x80
    public int pendingTipId;
    //0x84
    public int heroClassVoiceGate;

    /**
     * Native: RoleKeyDialogVisualObject::RoleKeyDialogVisualObject @004DC63F.
     * Fully ported.
     */
    public RoleKeyDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, String key) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.currentPartIndex = 0;
        this.key = key;
        this.activeSpeechSound = null;
        this.roleScriptBuffer = null;
        this.roleScriptBufferSize = 0;
        this.hasNpcDirective = 0;
        this.pendingTipId = 0;
        this.heroClassVoiceGate = 0;
        initializeRoleDialog();
    }

    /**
     * Native: RoleKeyDialogVisualObject::InitializeRoleDialog @004DD05B.
     * Fully ported. Java represents the native owned `malloc` char buffer as a `String` because recovered
     * consumers treat it as read-only text; `roleScriptBufferSize` preserves the native allocation size.
     */
    public void initializeRoleDialog() {
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        String marker = "#" + normalizedKey;
        String scriptData = getRoleDialogScriptData();
        int markerPos = scriptData.indexOf(marker);
        if (markerPos < 0) {
            roleScriptBuffer = "";
            roleScriptBufferSize = 1;
            hasNpcDirective = 0;
            return;
        }

        int start = markerPos + 2 + marker.length();
        if (start >= scriptData.length()) {
            roleScriptBuffer = "";
            roleScriptBufferSize = 1;
            hasNpcDirective = 0;
            return;
        }

        int end = scriptData.indexOf('#', start);
        String loadedRoleScriptBuffer = end >= 0 ? scriptData.substring(start, end) : scriptData.substring(start);
        roleScriptBuffer = loadedRoleScriptBuffer;
        roleScriptBufferSize = loadedRoleScriptBuffer.length() + 1;
        hasNpcDirective = loadedRoleScriptBuffer.toLowerCase(Locale.ROOT).contains("npc") ? 1 : 0;
    }

    /**
     * vtbl +0x48: RoleKeyDialogVisualObject::OnMessage @004DC754.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg != ROLE_DIALOG_ADVANCE_PART) {
            return super.onMessage(msg, wParam, lParam);
        }

        if (advanceRoleDialogPart()) {
            getChildById(ROLE_TEXT_CHILD_ID).draw();
            drawChildIfPresent(ROLE_PORTRAIT_CHILD_ID);
            return 1;
        }

        if (pendingTipId != 0) {
            Globals.mainWindow.postMessage(SHOW_TIP_BY_ID, pendingTipId, 0);
        }
        return super.onMessage(DIALOG_OK, 0, 0);
    }

    /**
     * vtbl +0x6C: RoleKeyDialogVisualObject::OnKeyDown @004DC70A.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == 0x0D || nChar == 0x1B) {
            onMessage(ROLE_DIALOG_ADVANCE_PART, 0, 0);
            return 1;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x80: RoleKeyDialogVisualObject::ShowDialog @004DC810.
     * Fully ported.
     */
    @Override
    public void showDialog() {
        advanceRoleDialogPart();
        super.showDialog();
    }

    /**
     * vtbl +0x84: RoleKeyDialogVisualObject::HideDialog @004DC82B.
     * Fully ported.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        releaseRoleDialogBuffer();
        return super.hideDialog(reason);
    }

    /**
     * Native helper: RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     * Fully ported.
     */
    private boolean advanceRoleDialogPart() {
        currentPartIndex += 1;
        releaseActiveSpeechSound();

        String loadedRoleScriptBuffer = roleScriptBuffer;
        int[] npcIdOut = {0};
        String[] soundOverrideOut = {""};
        int[] textStartOut = {-1};
        int[] textEndOut = {-1};
        int[] tuneIdOut = {-1};
        if (!parseRoleDialogPart(
                currentPartIndex,
                npcIdOut,
                soundOverrideOut,
                textStartOut,
                textEndOut,
                tuneIdOut
        )) {
            return false;
        }

        updateRoleDialogTextChild(loadedRoleScriptBuffer.substring(textStartOut[0], textEndOut[0]));
        playRoleDialogSpeech(soundOverrideOut[0], npcIdOut[0]);
        if (hasNpcDirective != 0) {
            syncRoleDialogPortraitChild(npcIdOut[0]);
        }
        if (tuneIdOut[0] >= 0) {
            playRoleDialogTune(tuneIdOut[0]);
        }
        return true;
    }

    /**
     * Native helper: RoleKeyDialogVisualObject::parseRoleDialogPart @004DD226.
     * Fully ported.
     */
    private boolean parseRoleDialogPart(
            int partIndex,
            int[] npcIdOut,
            String[] soundOverrideOut,
            int[] textStartOut,
            int[] textEndOut,
            int[] tuneIdOut
    ) {
        String loadedRoleScriptBuffer = roleScriptBuffer;
        int scanPosition = 0;
        while (scanPosition < loadedRoleScriptBuffer.length()) {
            int tagStart = loadedRoleScriptBuffer.indexOf('<', scanPosition);
            if (tagStart < 0) {
                return false;
            }
            int tagEnd = loadedRoleScriptBuffer.indexOf('>', tagStart + 1);
            if (tagEnd < 0) {
                return false;
            }

            String lowerTag = loadedRoleScriptBuffer.substring(tagStart + 1, tagEnd).toLowerCase(Locale.ROOT);
            if (!lowerTag.contains(PART_DIRECTIVE + partIndex)) {
                scanPosition = tagEnd + 1;
                continue;
            }

            String npcDirectiveValue = extractDirectiveValue(lowerTag, NPC_DIRECTIVE);
            hasNpcDirective = npcDirectiveValue == null ? 0 : 1;
            npcIdOut[0] = npcDirectiveValue == null ? 0 : parseNativeDecimal(npcDirectiveValue);
            heroClassVoiceGate = containsAnyToken(lowerTag, "iamfemale", "iammale", "iammage", "iamfighter") ? 1 : 0;

            if (!matchesCurrentHeroGate(lowerTag) || !matchesNpcStateGate(lowerTag)) {
                scanPosition = tagEnd + 1;
                continue;
            }

            soundOverrideOut[0] = extractSoundDirective(lowerTag);
            tuneIdOut[0] = parseOptionalFiveCharacterDirective(lowerTag, TUNE_DIRECTIVE, -1);
            pendingTipId = parseOptionalFiveCharacterDirective(lowerTag, TIPS_DIRECTIVE, pendingTipId);

            int textStart = findRoleDialogTextStart(loadedRoleScriptBuffer, tagEnd + 1);
            if (textStart < 0) {
                return false;
            }
            textStartOut[0] = textStart;
            textEndOut[0] = findRoleDialogTextEnd(loadedRoleScriptBuffer, textStart);
            return true;
        }
        return false;
    }

    /**
     * Native support boundary for the shared `g_strScriptData` role-script blob used by
     * RoleKeyDialogVisualObject::InitializeRoleDialog @004DD05B. Mission bootstrap loads mission text into this buffer
     * in CMainWindow::runSessionBootstrap @0048C8A3; town/menu paths load town text through their own boundaries.
     */
    private static String getRoleDialogScriptData() {
        return ScriptDataSupport.getJoinedScriptText();
    }

    /**
     * Native support boundary for the wrapped text update helper `FUN_004D4FB6` used by
     * RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     * Fully ported.
     */
    private void updateRoleDialogTextChild(String dialogText) {
        ((WrappedTextSourceListVisualObject) getChildById(ROLE_TEXT_CHILD_ID)).setSourceText(dialogText);
    }

    /**
     * Native support boundary for the active `Sound *` release branch at the head of
     * RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     * Partial port. Java stops and releases the previous speech sound through the sound backend.
     */
    private void releaseActiveSpeechSound() {
        if (activeSpeechSound == null) {
            return;
        }
        SoundSystem.get().releaseSound(activeSpeechSound);
        activeSpeechSound = null;
    }

    /**
     * Native support extracted from the role-dialog portrait child update through
     * MapVisualObject::CreateRoleDialogPortrait @0041D48D in RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     * Fully ported.
     */
    private void syncRoleDialogPortraitChild(int npcId) {
        LinkedPaletteVisualObject portraitChild = (LinkedPaletteVisualObject) getChildById(ROLE_PORTRAIT_CHILD_ID);
        portraitChild.setDrawable(Globals.mainWindow.pMapVisualObject.createRoleDialogPortrait(npcId));
    }

    /**
     * Native support for the speech-name synthesis and `Sound::Play` branch in
     * RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     */
    private void playRoleDialogSpeech(String soundOverride, int npcId) {
        String speechName = soundOverride == null ? "" : soundOverride;
        if (speechName.isEmpty()) {
            speechName = synthesizeSpeechName(npcId);
        }
        if (speechName.isEmpty()) {
            return;
        }

        String speechResource = Resources.path("speech", speechName.replace('\\', '/') + ".wav");
        activeSpeechSound = new Sound(speechResource);
        activeSpeechSound.load();
        activeSpeechSound.play(Globals.soundPreferences.speechVolume, false, SPEECH_PRIORITY, 0);
    }

    /**
     * Native support boundary for the `FUN_00459FE1` tune trigger in
     * RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     */
    private void playRoleDialogTune(int tuneId) {
        Globals.mainWindow.musicPlayer.queueTrackAfterFade(tuneId);
    }

    /**
     * Native support boundary for the current-hero `iam*` gate checks in
     * RoleKeyDialogVisualObject::parseRoleDialogPart @004DD226.
     */
    private static boolean matchesCurrentHeroGate(String lowerTag) {
        boolean hasFemaleGate = lowerTag.contains("iamfemale");
        boolean hasMaleGate = lowerTag.contains("iammale");
        boolean hasMageGate = lowerTag.contains("iammage");
        boolean hasFighterGate = lowerTag.contains("iamfighter");
        if (!hasFemaleGate && !hasMaleGate && !hasMageGate && !hasFighterGate) {
            return true;
        }

        CUnit selectedUnit = Globals.mainWindow.pMapVisualObject.getSelectedCUnit();
        int flags = selectedUnit.unitFlags;
        if (hasFemaleGate && (flags & UNIT_FLAG_FEMALE) == 0) {
            return false;
        }
        if (hasMaleGate && (flags & UNIT_FLAG_FEMALE) != 0) {
            return false;
        }
        if (hasMageGate && (flags & UNIT_FLAG_MAGIC_CLASS) == 0) {
            return false;
        }
        return !hasFighterGate || (flags & UNIT_FLAG_MAGIC_CLASS) == 0;
    }

    /**
     * Native support boundary for the `npcalive=*` / `npcdead=*` gate checks in
     * RoleKeyDialogVisualObject::parseRoleDialogPart @004DD226.
     */
    private static boolean matchesNpcStateGate(String lowerTag) {
        int scanPosition = 0;
        while (true) {
            int directiveIndex = lowerTag.indexOf(NPC_ALIVE_DIRECTIVE, scanPosition);
            if (directiveIndex < 0) {
                break;
            }
            int npcId = parseNativeDecimal(extractDirectiveValueAt(lowerTag, directiveIndex, NPC_ALIVE_DIRECTIVE));
            if (Globals.mainWindow.pMapVisualObject.getCUnit(npcId) == null) {
                return false;
            }
            scanPosition = directiveIndex + NPC_ALIVE_DIRECTIVE.length();
        }

        scanPosition = 0;
        while (true) {
            int directiveIndex = lowerTag.indexOf(NPC_DEAD_DIRECTIVE, scanPosition);
            if (directiveIndex < 0) {
                break;
            }
            int npcId = parseNativeDecimal(extractDirectiveValueAt(lowerTag, directiveIndex, NPC_DEAD_DIRECTIVE));
            if (Globals.mainWindow.pMapVisualObject.getCUnit(npcId) != null) {
                return false;
            }
            scanPosition = directiveIndex + NPC_DEAD_DIRECTIVE.length();
        }
        return true;
    }

    /**
     * Native: RoleKeyDialogVisualObject::releaseRoleDialogBuffer @004DD203.
     * Fully ported. Java detaches the managed script buffer instead of native `free`.
     */
    private void releaseRoleDialogBuffer() {
        roleScriptBuffer = null;
        roleScriptBufferSize = 0;
    }

    /**
     * Native support boundary for the optional portrait redraw branch in
     * RoleKeyDialogVisualObject::OnMessage @004DC754.
     * not ported.
     */
    private void drawChildIfPresent(int childId) {
        CVisualObject child = getChildById(childId);
        if (child != null) {
            child.draw();
        }
    }

    /**
     * Native support extracted from speech-name synthesis in RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     */
    private String synthesizeSpeechName(int npcId) {
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        char voiceCode = resolveSpeechVoiceCode(npcId);
        if (normalizedKey.contains(TALK_KEY)) {
            return String.format(
                    Locale.ROOT,
                    "talk/%04d%c%sp%02d",
                    npcId,
                    voiceCode == 0 ? 't' : voiceCode,
                    extractAndPadKeySuffix(normalizedKey, TALK_KEY),
                    currentPartIndex
            );
        }
        if (normalizedKey.contains(ABOUT_KEY)) {
            return String.format(Locale.ROOT, "about/%04dt000p%02d", npcId, currentPartIndex);
        }
        if (normalizedKey.contains(ACCEPT_KEY)) {
            return String.format(
                    Locale.ROOT,
                    "accept/%04d%c%sp%02d",
                    npcId,
                    voiceCode == 0 ? 't' : voiceCode,
                    extractAndPadKeySuffix(normalizedKey, ACCEPT_KEY),
                    currentPartIndex
            );
        }
        if (normalizedKey.contains(REJECT_KEY)) {
            return String.format(
                    Locale.ROOT,
                    "reject/%04d%c%sp%02d",
                    npcId,
                    voiceCode == 0 ? 't' : voiceCode,
                    extractAndPadKeySuffix(normalizedKey, REJECT_KEY),
                    currentPartIndex
            );
        }
        if (normalizedKey.contains(KEEPER_KEY) || normalizedKey.contains(MAN_KEY) || normalizedKey.contains(GUARD_KEY)) {
            return String.format(
                    Locale.ROOT,
                    "town/%04d%c%sp%02d",
                    npcId,
                    voiceCode == 0 ? 't' : voiceCode,
                    extractAndPadTownKeySuffix(normalizedKey),
                    currentPartIndex
            );
        }
        return "";
    }

    /**
     * Native support extracted from current-hero voice-code selection in
     * RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     */
    private char resolveSpeechVoiceCode(int npcId) {
        if (heroClassVoiceGate == 0 && npcId != PLAYER_NPC_ID) {
            return 0;
        }
        CUnit heroUnit = Globals.mainWindow.pMapVisualObject.getOrCreateCUnit(PLAYER_NPC_ID);
        int flags = heroUnit.unitFlags;
        if ((flags & UNIT_FLAG_FEMALE) == 0) {
            return (flags & UNIT_FLAG_MAGIC_CLASS) == 0 ? 'f' : 'm';
        }
        return (flags & UNIT_FLAG_MAGIC_CLASS) == 0 ? 'a' : 's';
    }

    /**
     * Java helper for the `sound=...` directive branch in RoleKeyDialogVisualObject::parseRoleDialogPart @004DD226.
     * not ported.
     */
    private static String extractSoundDirective(String lowerTag) {
        int soundIndex = lowerTag.indexOf(SOUND_DIRECTIVE);
        if (soundIndex < 0) {
            return "";
        }

        int valueStart = soundIndex + SOUND_DIRECTIVE.length();
        if (valueStart >= lowerTag.length()) {
            return "";
        }
        if (lowerTag.charAt(valueStart) == '"') {
            int quotedStart = valueStart + 1;
            int quotedEnd = lowerTag.indexOf('"', quotedStart);
            if (quotedEnd < 0) {
                quotedEnd = lowerTag.length();
            }
            return lowerTag.substring(quotedStart, quotedEnd);
        }

        int valueEnd = lowerTag.indexOf(';', valueStart);
        if (valueEnd < 0) {
            valueEnd = lowerTag.length();
        }
        return lowerTag.substring(valueStart, valueEnd);
    }

    /**
     * Java helper for the repeated five-character directive extraction branches in
     * RoleKeyDialogVisualObject::parseRoleDialogPart @004DD226.
     * not ported.
     */
    private static int parseOptionalFiveCharacterDirective(String lowerTag, String prefix, int defaultValue) {
        String digits = extractDirectiveValue(lowerTag, prefix);
        return digits == null ? defaultValue : parseNativeDecimal(digits);
    }

    /**
     * Java helper for the repeated `CString::Mid(..., 5)` directive branches in
     * RoleKeyDialogVisualObject::parseRoleDialogPart @004DD226.
     * not ported.
     */
    private static String extractDirectiveValue(String lowerTag, String prefix) {
        int index = lowerTag.indexOf(prefix);
        if (index < 0) {
            return null;
        }
        return extractDirectiveValueAt(lowerTag, index, prefix);
    }

    /**
     * Java helper for native directive values already located by `CString::Find`.
     * not ported.
     */
    private static String extractDirectiveValueAt(String lowerTag, int index, String prefix) {
        int valueStart = index + prefix.length();
        if (valueStart >= lowerTag.length()) {
            return "";
        }
        int valueEnd = Math.min(valueStart + 5, lowerTag.length());
        return lowerTag.substring(valueStart, valueEnd);
    }

    /**
     * Java helper for the native integer parse thunks around `FUN_00584030` in RoleKeyDialogVisualObject support helpers.
     * not ported.
     */
    private static int parseNativeDecimal(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int index = 0;
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index += 1;
        }
        int sign = 1;
        if (index < text.length() && (text.charAt(index) == '-' || text.charAt(index) == '+')) {
            sign = text.charAt(index) == '-' ? -1 : 1;
            index += 1;
        }
        int value = 0;
        boolean hasDigit = false;
        while (index < text.length() && Character.isDigit(text.charAt(index))) {
            value = value * 10 + (text.charAt(index) - '0');
            hasDigit = true;
            index += 1;
        }
        return hasDigit ? value * sign : 0;
    }

    /**
     * Java helper for the post-tag newline advance in RoleKeyDialogVisualObject::parseRoleDialogPart @004DD226.
     * not ported.
     */
    private static int findRoleDialogTextStart(String loadedRoleScriptBuffer, int scanPosition) {
        int nextNewline = loadedRoleScriptBuffer.indexOf('\n', scanPosition);
        if (nextNewline < 0) {
            return -1;
        }
        return nextNewline + 1;
    }

    /**
     * Java helper for the next-tag / backtrack-to-CR branch in RoleKeyDialogVisualObject::parseRoleDialogPart @004DD226.
     * not ported.
     */
    private static int findRoleDialogTextEnd(String loadedRoleScriptBuffer, int textStart) {
        int nextTag = loadedRoleScriptBuffer.indexOf('<', textStart);
        if (nextTag < 0) {
            return loadedRoleScriptBuffer.length();
        }
        int textEnd = nextTag;
        while (textEnd > textStart && loadedRoleScriptBuffer.charAt(textEnd - 1) != '\r') {
            textEnd -= 1;
        }
        return textEnd > textStart ? textEnd - 1 : nextTag;
    }

    /**
     * Java helper for repeated `CString::Find` token checks in RoleKeyDialogVisualObject support helpers.
     * not ported.
     */
    private static boolean containsAnyToken(String text, String... tokens) {
        if (text == null || tokens == null) {
            return false;
        }
        for (String token : tokens) {
            if (token != null && text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from the key-suffix `CString::Mid` and zero-padding blocks in
     * RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     */
    private static String extractAndPadKeySuffix(String normalizedKey, String token) {
        int tokenIndex = normalizedKey.indexOf(token);
        int start = tokenIndex < 0 ? normalizedKey.length() : tokenIndex + token.length();
        int end = Math.min(start + 5, normalizedKey.length());
        return padAtLeastThree(normalizedKey.substring(start, end));
    }

    /**
     * Native support extracted from the town-key trailing digit scan in
     * RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     */
    private static String extractAndPadTownKeySuffix(String normalizedKey) {
        int suffixStart = normalizedKey.length();
        while (suffixStart > 0 && !Character.isAlphabetic(normalizedKey.charAt(suffixStart - 1))) {
            suffixStart -= 1;
        }
        int end = Math.min(suffixStart + 5, normalizedKey.length());
        return padAtLeastThree(normalizedKey.substring(suffixStart, end));
    }

    /**
     * Native support extracted from the repeated `while (length < 3) prefix '0'` blocks in
     * RoleKeyDialogVisualObject::advanceRoleDialogPart @004DC84C.
     * not ported.
     */
    private static String padAtLeastThree(String value) {
        String padded = value;
        while (padded.length() < 3) {
            padded = "0" + padded;
        }
        return padded;
    }
}
