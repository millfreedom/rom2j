import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Building;
import ua.millfreedom.rom2.model.TargetHandle;
import ua.millfreedom.rom2.model.UnitList;
import ua.millfreedom.rom2.model.world.CWorldMap;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.scenario.BuildingDTO;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

public final class PassabilityMapPng {
    private static final int BORDER = 0xFF17191B;
    private static final int TERRAIN_BLOCKED = 0xFF2F3133;
    private static final int OBJECT_BLOCKED = 0xFF5D3347;
    private static final int BUILDING_BLOCKED = 0xFF3D2F22;
    private static final int EASY = 0xFF5FA65B;
    private static final int NORMAL = 0xFF89C36B;
    private static final int SLOW = 0xFFE6C85D;
    private static final int VERY_SLOW = 0xFFD9844A;

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("usage: PassabilityMapPng <map.alm> [output.png]");
        }

        String mapPath = args[0];
        File output = new File(args.length == 2 ? args[1] : defaultOutputPath(mapPath));
        Globals.staticDataMgr.loadOrRebuild(Path.of("."));

        ScenarioDescriptor scenario = new ScenarioDescriptor(mapPath);
        if (!scenario.loaded || scenario.sec2Heights == null) {
            throw new IllegalStateException("Failed to load " + mapPath + ": error " + scenario.error);
        }

        CWorldMap worldMap = new CWorldMap(scenario, new UnitList());
        Globals.worldMap = worldMap;
        int buildings = attachScenarioBuildings(scenario, worldMap);
        render(mapPath, scenario, worldMap, buildings, output);
        System.out.println(output.getPath());
    }

    private static String defaultOutputPath(String mapPath) {
        String name = new File(mapPath).getName();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            name = name.substring(0, dot);
        }
        return "docs/generated/" + name.toLowerCase() + "-passability-map.png";
    }

    private static int attachScenarioBuildings(ScenarioDescriptor scenario, CWorldMap worldMap) {
        int count = 0;
        for (BuildingDTO dto : scenario.sec4Buildings) {
            if (dto == null) {
                continue;
            }
            TargetHandle target = new TargetHandle();
            target.initFromBytes(dto.x, dto.y, worldMap);
            new Building(dto.typeID & 0xFF, target, dto.sizeX, dto.sizeY);
            count++;
        }
        return count;
    }

    private static void render(String mapPath, ScenarioDescriptor scenario, CWorldMap worldMap, int buildings,
                               File output) throws Exception {
        int width = scenario.mapWidth;
        int height = scenario.mapHeight;
        int cell = Math.max(4, Math.min(14, 768 / Math.max(width, height)));
        int left = 26;
        int top = 26;
        int imageWidth = left + width * cell + 224;
        int imageHeight = Math.max(top + height * cell + 38, 240);
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(246, 247, 241));
        g.fillRect(0, 0, imageWidth, imageHeight);
        g.setColor(new Color(36, 40, 43));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.drawString(new File(mapPath).getName() + " Passability", left, 18);

        int passable = 0;
        int blocked = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int packed = ((y & 0xFF) << 8) | (x & 0xFF);
                int cost = worldMap.layer0_0x00000[packed] & 0xFF;
                int layer = worldMap.layer1_0x10000[packed] & 0xFF;
                boolean isBlocked = cost == 0xFF || (layer & 0x05) != 0 || layer == 0x1F;
                if (isBlocked) {
                    blocked++;
                } else {
                    passable++;
                }
                g.setColor(new Color(colorFor(scenario, x, y, cost, layer, isBlocked), true));
                g.fillRect(left + x * cell, top + y * cell, cell, cell);
            }
        }

        g.setColor(new Color(0, 0, 0, 44));
        g.drawRect(left, top, width * cell, height * cell);
        if (cell >= 7) {
            g.setColor(new Color(0, 0, 0, 18));
            for (int x = 0; x <= width; x += 8) {
                g.drawLine(left + x * cell, top, left + x * cell, top + height * cell);
            }
            for (int y = 0; y <= height; y += 8) {
                g.drawLine(left, top + y * cell, left + width * cell, top + y * cell);
            }
        }

        drawLegend(g, left + width * cell + 24, top + 4, width, height, passable, blocked, buildings);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(new Color(74, 78, 82));
        g.drawString("Native-style CWorldMap cost/layer rules", left, imageHeight - 18);
        g.drawString("blocked = cost 0xFF or layer1 blocker", left, imageHeight - 6);
        g.dispose();

        File parent = output.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        ImageIO.write(image, "png", output);
    }

    private static int colorFor(ScenarioDescriptor scenario, int x, int y, int cost, int layer, boolean blocked) {
        if (blocked) {
            if (isBorder(scenario, x, y)) {
                return BORDER;
            }
            if (Byte.toUnsignedInt(scenario.sec3Objects[y * scenario.mapWidth + x]) != 0) {
                return OBJECT_BLOCKED;
            }
            return (layer & 0x20) != 0 ? BUILDING_BLOCKED : TERRAIN_BLOCKED;
        }
        if (cost <= 6) {
            return EASY;
        }
        if (cost <= 8) {
            return NORMAL;
        }
        if (cost <= 11) {
            return SLOW;
        }
        return VERY_SLOW;
    }

    private static boolean isBorder(ScenarioDescriptor scenario, int x, int y) {
        return x < 8 || y < 8 || x >= scenario.mapWidth - 8 || y >= scenario.mapHeight - 8;
    }

    private static void drawLegend(Graphics2D g, int x, int y, int width, int height, int passable, int blocked,
                                   int buildings) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(new Color(36, 40, 43));
        g.drawString("Legend", x, y);
        y += 16;

        int[] colors = {EASY, NORMAL, SLOW, VERY_SLOW, BORDER, TERRAIN_BLOCKED, OBJECT_BLOCKED, BUILDING_BLOCKED};
        String[] text = {
                "passable cost <= 6", "passable cost 7-8", "passable cost 9-11", "passable cost >= 12",
                "blocked border", "blocked terrain", "blocked object byte", "blocked building mask"
        };
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        for (int i = 0; i < colors.length; i++) {
            g.setColor(new Color(colors[i], true));
            g.fillRect(x, y - 9, 11, 11);
            g.setColor(new Color(36, 40, 43));
            g.drawString(text[i], x + 17, y);
            y += 16;
        }

        y += 8;
        g.drawString("Size: " + width + "x" + height, x, y);
        g.drawString("Passable: " + passable, x, y + 15);
        g.drawString("Blocked: " + blocked, x, y + 30);
        g.drawString("Buildings attached: " + buildings, x, y + 45);
        g.drawString("Static world-map layer", x, y + 60);
    }
}
