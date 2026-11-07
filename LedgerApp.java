import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class LedgerApp extends JFrame {
    // ---------- colours ----------
    static final Color BG1     = new Color(12, 22, 38);
    static final Color BG2     = new Color(22, 52, 70);
    static final Color TITLE   = new Color(228, 238, 248);
    static final Color CYAN    = new Color(120, 200, 215);
    static final Color MAGENTA = new Color(190, 115, 135);
    static final Color GREEN   = new Color(110, 195, 150);
    static final Color RED     = new Color(220, 115, 115);
    static final Color YELLOW  = new Color(225, 195, 115);
    static final Color BLUE    = new Color(95, 150, 210);
    static final Color PURPLE  = new Color(115, 130, 195);
    static final Color FIELD   = new Color(18, 34, 54);
    static final Color TABLE_A = new Color(14, 28, 44);
    static final Color TABLE_B = new Color(20, 38, 58);
    static final Color ACCENT  = new Color(45, 105, 130);

    LedgerList ledger = new LedgerList();

    JTextField idField = styledField();
    JTextField itemField = styledField();
    JTextField qtyField = styledField();
    JComboBox<String> typeBox = new JComboBox<>(new String[]{"IN", "OUT"});
    DefaultTableModel model;
    JTable table;
    JLabel statusLabel = new JLabel(" Welcome! Add a transaction to start.");
    JLabel blocksLabel = new JLabel("Blocks: 0");
    JLabel chainLabel = new JLabel("Chain: not checked");

    public LedgerApp() {
        super("Blockchain Inventory Ledger  -  Doubly Linked List");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(18, 20, 14, 20));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.WEST);
        root.add(buildTable(), BorderLayout.CENTER);
        root.add(buildStatus(), BorderLayout.SOUTH);
        refresh(ledger.toList());
    }

    // ---------- header ----------
    JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("BLOCKCHAIN INVENTORY LEDGER");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TITLE);
        JLabel sub = new JLabel("Secure stock tracking powered by a Doubly Linked List");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(CYAN);
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        for (JLabel l : new JLabel[]{blocksLabel, chainLabel}) {
            l.setFont(new Font("Segoe UI", Font.BOLD, 16));
            l.setForeground(Color.WHITE);
            l.setAlignmentX(Component.RIGHT_ALIGNMENT);
            right.add(l);
        }
        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ---------- left form panel ----------
    JComponent buildForm() {
        GlassPanel p = new GlassPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setPreferredSize(new Dimension(290, 0));

        p.add(sectionLabel("TRANSACTION DETAILS"));
        p.add(Box.createVerticalStrut(8));
        p.add(fieldLabel("Block ID (for update / delete / tamper)"));
        p.add(idField);
        p.add(Box.createVerticalStrut(6));
        p.add(fieldLabel("Item name"));
        p.add(itemField);
        p.add(Box.createVerticalStrut(6));
        p.add(fieldLabel("Quantity"));
        p.add(qtyField);
        p.add(Box.createVerticalStrut(6));
        p.add(fieldLabel("Type"));
        typeBox.setBackground(FIELD);
        typeBox.setForeground(Color.WHITE);
        typeBox.setFont(new Font("Segoe UI", Font.BOLD, 14));
        typeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        typeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(typeBox);
        p.add(Box.createVerticalStrut(16));

        p.add(sectionLabel("OPERATIONS"));
        p.add(Box.createVerticalStrut(8));
        p.add(buttonRow(button("Add (Create)", GREEN, e -> doAdd()),
                        button("Show All (Read)", CYAN, e -> doShowAll())));
        p.add(Box.createVerticalStrut(8));
        p.add(buttonRow(button("Update", YELLOW, e -> doUpdate()),
                        button("Delete", RED, e -> doDelete())));
        p.add(Box.createVerticalStrut(8));
        p.add(buttonRow(button("Search Item", BLUE, e -> doSearch()),
                        button("Stock Level", BLUE, e -> doStock())));
        p.add(Box.createVerticalStrut(16));

        p.add(sectionLabel("SECURITY"));
        p.add(Box.createVerticalStrut(8));
        p.add(buttonRow(button("Validate Chain", PURPLE, e -> doValidate()),
                        button("Tamper (demo)", MAGENTA, e -> doTamper())));
        p.add(Box.createVerticalStrut(8));
        p.add(buttonRow(button("Load Sample Data", new Color(75, 100, 120), e -> doSample()),
                        button("Clear Fields", new Color(75, 100, 120), e -> clearFields())));
        p.add(Box.createVerticalGlue());
        return p;
    }

    // ---------- table ----------
    JComponent buildTable() {
        model = new DefaultTableModel(
                new String[]{"ID", "Timestamp", "Item", "Type", "Qty", "Hash", "Previous Hash"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(TABLE_A);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                int id = (Integer) model.getValueAt(row, 0);
                String type = (String) model.getValueAt(row, 3);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(col == 4 || col == 0 ? CENTER : LEFT);
                setFont(col >= 5 ? new Font("Consolas", Font.PLAIN, 13) : new Font("Segoe UI", Font.PLAIN, 14));
                if (id == ledger.invalidId) {
                    setBackground(new Color(160, 60, 65));
                    setForeground(Color.WHITE);
                } else if (sel) {
                    setBackground(new Color(55, 115, 145));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? TABLE_A : TABLE_B);
                    setForeground(col >= 5 ? new Color(165, 190, 210) : (type.equals("IN") ? GREEN : RED));
                }
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(cell);
        int[] widths = {45, 150, 130, 55, 60, 170, 170};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 36));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s,
                                                           boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setBackground(ACCENT);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(c == 4 || c == 0 ? CENTER : LEFT);
                return this;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0) {
                idField.setText(String.valueOf(model.getValueAt(r, 0)));
                itemField.setText((String) model.getValueAt(r, 2));
                typeBox.setSelectedItem(model.getValueAt(r, 3));
                qtyField.setText(String.valueOf(model.getValueAt(r, 4)));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(new Color(70, 140, 165), 2, true));
        sp.getViewport().setBackground(TABLE_A);
        return sp;
    }

    JComponent buildStatus() {
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusLabel.setForeground(YELLOW);
        statusLabel.setBorder(new EmptyBorder(6, 4, 0, 4));
        return statusLabel;
    }

    // ---------- actions ----------
    void doAdd() {
        Integer q = parse(qtyField, "Quantity");
        if (q == null) return;
        show(ledger.add(itemField.getText().trim(), q, (String) typeBox.getSelectedItem()));
        refresh(ledger.toList());
    }

    void doShowAll() {
        refresh(ledger.toList());
        show("OK: Showing all " + ledger.toList().size() + " block(s).");
    }

    void doUpdate() {
        Integer id = parse(idField, "Block ID");
        Integer q = parse(qtyField, "Quantity");
        if (id == null || q == null) return;
        show(ledger.update(id, q, (String) typeBox.getSelectedItem()));
        refresh(ledger.toList());
    }

    void doDelete() {
        Integer id = parse(idField, "Block ID");
        if (id == null) return;
        show(ledger.delete(id));
        refresh(ledger.toList());
    }

    void doSearch() {
        String item = itemField.getText().trim();
        if (item.isEmpty()) { show("ERROR: Type an item name to search."); return; }
        List<Block> found = ledger.search(item);
        refresh(found);
        show(found.isEmpty() ? "ERROR: Item not found." : "OK: Found " + found.size() + " block(s) for " + item + ".");
    }

    void doStock() {
        String item = itemField.getText().trim();
        if (item.isEmpty()) { show("ERROR: Type an item name first."); return; }
        show("OK: Current stock of " + item + " = " + ledger.getStock(item));
    }

    void doValidate() {
        String msg = ledger.validate();
        show(msg);
        chainLabel.setText(msg.startsWith("OK") ? "Chain: VALID" : "Chain: TAMPERED (Block " + ledger.invalidId + ")");
        chainLabel.setForeground(msg.startsWith("OK") ? GREEN : RED);
        refresh(ledger.toList());
    }

    void doTamper() {
        Integer id = parse(idField, "Block ID");
        Integer q = parse(qtyField, "Fake quantity");
        if (id == null || q == null) return;
        show(ledger.tamper(id, q));
        refresh(ledger.toList());
    }

    void doSample() {
        ledger.add("Laptop", 10, "IN");
        ledger.add("Mouse", 25, "IN");
        ledger.add("Keyboard", 15, "IN");
        ledger.add("Laptop", 3, "OUT");
        show("OK: Sample data loaded (4 blocks).");
        refresh(ledger.toList());
    }

    void clearFields() {
        idField.setText(""); itemField.setText(""); qtyField.setText("");
        typeBox.setSelectedIndex(0);
        table.clearSelection();
    }

    // ---------- helpers ----------
    Integer parse(JTextField f, String name) {
        try {
            return Integer.parseInt(f.getText().trim());
        } catch (NumberFormatException e) {
            show("ERROR: " + name + " must be a whole number.");
            return null;
        }
    }

    void show(String msg) {
        boolean ok = msg.startsWith("OK:");
        boolean err = msg.startsWith("ERROR:");
        statusLabel.setForeground(ok ? GREEN : err ? RED : YELLOW);
        statusLabel.setText(" " + msg.replaceFirst("^(OK|ERROR): ", ""));
    }

    void refresh(List<Block> blocks) {
        model.setRowCount(0);
        for (Block b : blocks) {
            model.addRow(new Object[]{b.id, b.timestamp, b.item, b.type, b.quantity,
                    b.hash.substring(0, 20) + "...",
                    b.previousHash.equals("0") ? "0 (genesis)" : b.previousHash.substring(0, 20) + "..."});
        }
        blocksLabel.setText("Blocks: " + ledger.toList().size());
        if (ledger.invalidId == -1 && chainLabel.getText().contains("TAMPERED")) {
            chainLabel.setText("Chain: not checked");
            chainLabel.setForeground(Color.WHITE);
        }
        table.repaint();
    }

    static JTextField styledField() {
        JTextField f = new JTextField();
        f.setBackground(FIELD);
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(new CompoundBorder(new LineBorder(new Color(70, 130, 160), 1, true), new EmptyBorder(4, 8, 4, 8)));
        return f;
    }

    static JLabel sectionLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(CYAN);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    static JLabel fieldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(200, 210, 250));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 3, 0));
        return l;
    }

    static NeonButton button(String text, Color c, ActionListener a) {
        NeonButton b = new NeonButton(text, c);
        b.addActionListener(a);
        return b;
    }

    static JPanel buttonRow(JButton a, JButton b) {
        JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.setOpaque(false);
        row.add(a);
        row.add(b);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    // ---------- custom components ----------
    // Draws background.png (put any picture with that name in the project folder).
    // If the file is missing, a calm gradient is used instead.
    static class GradientPanel extends JPanel {
        java.awt.image.BufferedImage bg;
        GradientPanel() {
            try {
                java.io.File f = new java.io.File("background.png");
                if (!f.exists()) f = new java.io.File("background.jpg");
                if (f.exists()) bg = javax.imageio.ImageIO.read(f);
            } catch (Exception ignored) {}
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (bg != null) {
                // scale the picture to cover the whole window
                double scale = Math.max((double) getWidth() / bg.getWidth(), (double) getHeight() / bg.getHeight());
                int w = (int) (bg.getWidth() * scale), h = (int) (bg.getHeight() * scale);
                g2.drawImage(bg, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
                g2.setColor(new Color(0, 0, 0, 90));   // soft dark overlay for readable text
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2.setPaint(new GradientPaint(0, 0, BG1, getWidth(), getHeight(), BG2));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
        }
    }

    static class GlassPanel extends JPanel {
        GlassPanel() { setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(8, 18, 32, 165));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.setColor(new Color(110, 190, 210, 130));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.dispose();
        }
    }

    static class NeonButton extends JButton {
        Color base;
        boolean hover;
        NeonButton(String text, Color base) {
            super(text);
            this.base = base;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = getModel().isPressed() ? base.darker() : hover ? base.brighter() : base;
            g2.setPaint(new GradientPaint(0, 0, c, 0, getHeight(), c.darker()));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LedgerApp().setVisible(true));
    }
}