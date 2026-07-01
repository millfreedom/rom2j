package ua.millfreedom.rom2;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.res.Resources;
import ua.millfreedom.rom2.text.TextTableId;

import java.nio.charset.Charset;
import java.util.*;

import static ua.millfreedom.rom2.Utils.readAllLines;
import static ua.millfreedom.rom2.Utils.readText;

public class CTextFile implements MfcSerializable {
    public final String name;
    public final List<String> lines;

    //IMPORTANT: SHOULD STAY PRIVATE TO AVOID WRONG USAGE
    // Native g_StringTable @00622600 is represented by TextTableId/StringTableIndex plus this table-local storage.
    // Static CArray<CString> lifecycle thunks @00473E62/@00473E71/@00473E80/@00473E92 are represented by Java object lifecycle.
    // Native g_patch @00621878 is represented by TextTableId.PATCH/PatchText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473DE2/@00473DF1/@00473E00/@00473E12 are represented by Java object lifecycle.
    // Native g_tunes @006227C8 is represented by TextTableId.TUNES/TunesText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473DA3/@00473DB2/@00473DC1/@00473DD3 are represented by Java object lifecycle.
    // Native g_cutpaths @006226B0 is represented by TextTableId.CUTPATHS/CutPathsText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473D64/@00473D73/@00473D82/@00473D94 are represented by Java object lifecycle.
    // Native g_cutscene @00622660 is represented by TextTableId.CUTSCENE/CutSceneText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473D25/@00473D34/@00473D43/@00473D55 are represented by Java object lifecycle.
    // Native g_npcnames @00621868 is represented by TextTableId.NPCNAMES/NpcNamesText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473CE6/@00473CF5/@00473D04/@00473D16 are represented by Java object lifecycle.
    // Native g_itemname @006225E0 is represented by TextTableId.ITEMNAME/ItemNameText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473C68/@00473C77/@00473C86/@00473C98 are represented by Java object lifecycle.
    // Native g_building @00622670 is represented by TextTableId.BUILDING/BuildingText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473C29/@00473C38/@00473C47/@00473C59 are represented by Java object lifecycle.
    // Native g_unitname @006226F0 is represented by TextTableId.UNITNAME/UnitNameText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473BEA/@00473BF9/@00473C08/@00473C1A are represented by Java object lifecycle.
    // Native g_dialogs @00621888 is represented by TextTableId.DIALOGS/DialogsText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473BAB/@00473BBA/@00473BC9/@00473BDB are represented by Java object lifecycle.
    // Native g_spell @006226E0 is represented by TextTableId.SPELL/SpellText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473B6C/@00473B7B/@00473B8A/@00473B9C are represented by Java object lifecycle.
    // Native g_spells @006225F0 is represented by TextTableId.SPELLS/SpellsText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473B2D/@00473B3C/@00473B4B/@00473B5D are represented by Java object lifecycle.
    // Native g_stats @00621440 is represented by TextTableId.STATS/StatsText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473AEE/@00473AFD/@00473B0C/@00473B1E are represented by Java object lifecycle.
    // Native g_heropicture @00621430 is represented by TextTableId.HEROPICTURE/HeroPictureText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473AAF/@00473ABE/@00473ACD/@00473ADF are represented by Java object lifecycle.
    // Native g_main @00622730 is represented by TextTableId.MAIN/MainText plus this table-local storage.
    // Static CTextFile lifecycle thunks @00473A70/@00473A7F/@00473A8E/@00473AA0 are represented by Java object lifecycle.
    private static final Map<String, List<String>> all = new HashMap<>();

    // not ported.
    private CTextFile(String name, List<String> lines) {
        this.name = name;
        this.lines = lines;
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native support extracted from CTextFile::New @00474548.
     * Fully ported for Java-managed text table instances.
     */
    public static CTextFile createEmpty(String name) {
        return new CTextFile(name, new ArrayList<>());
    }


    /**
     * Java convenience helper around the instance-native path using a predefined TextTableId.
     * not ported.
     * IMPORTANT: should NOT be used to load any file, except those 15, listed in TextTableId!
     */
    public static CTextFile LoadAndParse(TextTableId textTableId) {
        Objects.requireNonNull(textTableId, "textTableId");
        CTextFile textFile = createEmpty(textTableId.arrayName());
        textFile.loadAndParse(textTableId.resourcePath());
        return textFile;
    }

    /**
     * Native support extracted from the CTextFile::Delete global-table calls in CMainApp::ExitInstance @00481B0C.
     */
    public static void delete(TextTableId textTableId) {
        all.remove(textTableId.arrayName());
    }

    /**
     * Native: CTextFile::LoadAndParse @00474585.
     * Fully ported through Java-managed table-local lines plus CTextFile.all registration for g_StringTable lookups.
     * <p>
     * INTENTIONALLY PRIVATE TO AVOID ERRORS DURING PORTING.
     */
    private void loadAndParse(String fileName) {
        Objects.requireNonNull(fileName, "fileName");
        List<String> allLines = readAllLines(Resources.open(fileName), Charset.forName("Cp1251"));
        lines.clear();
        lines.addAll(allLines);
        all.put(name, new ArrayList<>(lines));
    }

    /**
     * Native: Global::LoadTextFileToOEM @004741AD.
     * Fully ported for Java text-domain strings. Native reads the complete CGameFile payload, NUL-terminates it,
     * applies CharToOemA in place, and assigns the result to a CString; Java decodes the same CP1251 resources
     * directly to Unicode for renderer/dialog consumers.
     */
    public static String loadTextFileToOemString(String fileName) {
        return readText(Resources.open(fileName), Charset.forName("Cp1251"));
    }

    /**
     * Native support extracted from Global::LoadTextFileToOEM @004741AD for line-oriented g_strScriptData consumers.
     */
    public static CTextFile loadTextFileToOem(String fileName) {
        return new CTextFile(fileName, loadTextFileToOemString(fileName).lines().toList());
    }

    /**
     * Native: CTextFile::Delete @004746C5.
     * Java port status: fully ported through local table cleanup.
     */
    public void delete() {
        lines.clear();
        all.remove(name);
    }

    /**
     * Native: CTextFile::GetLineCount @0043C3D0.
     * Fully ported.
     */
    public int getLineCount() {
        return lines.size();
    }

    /**
     * Native: CTextFile::GetAt @00474733.
     * Java port status: fully ported through the table-local line list.
     */
    public String getAt(int index) {
        return lines.get(index);
    }

    // not ported.
    public static String GetValue(String key, int index) {
        return all.get(key).get(index);
    }

    // not ported.
    public static void AddValue(String key, String value) {
        all.computeIfAbsent(key, _ -> new ArrayList<>()).add(value);
    }
}
