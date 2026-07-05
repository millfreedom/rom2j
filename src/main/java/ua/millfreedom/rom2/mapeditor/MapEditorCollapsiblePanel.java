package ua.millfreedom.rom2.mapeditor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.Serial;

/**
 * One-line spoiler wrapper for standalone MapEditor control groups.
 * not ported.
 */
final class MapEditorCollapsiblePanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COLLAPSED_PREFIX = "[+] ";
    private static final String EXPANDED_PREFIX = "[-] ";

    private final String title;
    private final JComponent content;
    private final JButton headerButton = new JButton();
    private boolean collapsed;

    /**
     * Java support constructor for one collapsible editor control group.
     * not ported.
     */
    private MapEditorCollapsiblePanel(String title, JComponent content, boolean initiallyCollapsed) {
        super(new BorderLayout(0, 2));
        this.title = title;
        this.content = content;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(82, 88, 94)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        headerButton.setHorizontalAlignment(SwingConstants.LEFT);
        headerButton.setFocusPainted(false);
        headerButton.addActionListener(event -> setCollapsed(!collapsed));
        add(headerButton, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        setCollapsed(initiallyCollapsed);
    }

    /**
     * Java support factory for default-collapsed editor control groups.
     * not ported.
     */
    static JPanel collapsed(String title, JComponent content) {
        return new MapEditorCollapsiblePanel(title, content, true);
    }

    /**
     * Java support spoiler toggle state for one editor control group.
     * not ported.
     */
    private void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        content.setVisible(!collapsed);
        headerButton.setText((collapsed ? COLLAPSED_PREFIX : EXPANDED_PREFIX) + title);
        revalidate();
        repaint();
    }
}
