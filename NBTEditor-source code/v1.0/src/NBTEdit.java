/* 这是NBTEdit.java,是程序的入口点 它负责初始化界面和处理用户交互 */

// 由huangzuan制作,请遵守MIT协议,保留原作者信息
// 项目网址: https://github.com/huangzuan/NBTEditor/

/* NBTEdit.java */

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;

import javax.swing.*;
import javax.swing.tree.*;




import java.awt.*;
import java.awt.event.*;
import java.io.File;



public class NBTEdit {

    public static void main(String[] args) {

        AppInitializer.init();

        JFrame frame = new JFrame("NBT 编辑器");
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        JLabel status = new JLabel("Ready");
        frame.add(status, BorderLayout.NORTH);

        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        try {
            File file = chooser.getSelectedFile();
            final File[] currentFile = new File[]{file};

            Tag<?> rootRaw = NBTUtil.read(file).getTag();
            if (!(rootRaw instanceof CompoundTag root)) {
                JOptionPane.showMessageDialog(frame, "Root must be CompoundTag");
                return;
            }

            JTree tree = new JTree(buildTree("Root", root));

            // ================= 图标渲染 =================
            tree.setCellRenderer(new DefaultTreeCellRenderer() {
                @Override
                public Component getTreeCellRendererComponent(
                        JTree tree, Object value,
                        boolean sel, boolean expanded,
                        boolean leaf, int row, boolean hasFocus) {

                    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

                    if (node.getUserObject() instanceof TagWrapper w) {
                        Tag<?> t = w.tag;
                    
                        setIcon(null);
                    
                        if (t instanceof CompoundTag ct) {
                            setIcon(icon("TAG_Compound.png"));
                            setText(w.name + " (" + ct.size() + ")");
                    
                        } else if (t instanceof ListTag<?> lt) {
                            setIcon(icon("TAG_List.png"));
                            setText(w.name + " (" + lt.size() + ")");
                    
                        } else if (t instanceof StringTag) {
                            setIcon(icon("TAG_String.png"));
                            setText(w.name + ": " + t.toString());
                    
                        } else if (t instanceof IntTag) {
                            setIcon(icon("TAG_Int.png"));
                            setText(w.name + ": " + t.toString());
                    
                        } else if (t instanceof ByteTag) {
                            setIcon(icon("TAG_Byte.png"));
                            setText(w.name + ": " + t.toString());
                    
                        } else {
                            setText(w.name);
                        }
                    }
                    return this;
                }
            });

            // ================= 双击编辑 =================
            tree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {

                        TreePath path = tree.getSelectionPath();
                        if (path == null) return;

                        DefaultMutableTreeNode node =
                                (DefaultMutableTreeNode) path.getLastPathComponent();

                        if (!(node.getUserObject() instanceof TagWrapper w)) return;

                        Tag<?> tag = w.tag;

       if (tag instanceof StringTag st) {
    String val = JOptionPane.showInputDialog("Edit:", st.toString());
    if (val != null) st.setValue(val);

} else if (tag instanceof IntTag it) {
    String val = JOptionPane.showInputDialog("Edit:", it.toString());
    if (val != null) {
        try {
            it.setValue(Integer.parseInt(val));
        } catch (Exception ignored) {}
    }

} else if (tag instanceof ByteTag bt) {
    String val = JOptionPane.showInputDialog("Edit:", bt.toString());
    if (val != null) {
        try {
            bt.setValue(Byte.parseByte(val));
        } catch (Exception ignored) {}
    }
}

                        ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                    }
                }
            });

            // ================= 保存 =================
            Runnable save = () -> {
                try {
                    if (currentFile[0] == null) return;
                    NBTUtil.write(new NamedTag("", root), currentFile[0]);
                    status.setText("Saved ✔");
                    frame.setTitle("NBT 编辑器 - " + currentFile[0].getName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            };

            // ================= Save As =================
            Runnable saveAs = () -> {
                JFileChooser c = new JFileChooser();
                if (c.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;

                try {
                    File f = c.getSelectedFile();
                    NBTUtil.write(new NamedTag("", root), f);
                    currentFile[0] = f;
                    frame.setTitle("NBT 编辑器 - " + f.getName());
                    status.setText("Saved As ✔");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            };

            // ================= 打开 =================
            Runnable open = () -> {
                JFileChooser c = new JFileChooser();
                if (c.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

                try {
                    File f = c.getSelectedFile();
                    Tag<?> newRoot = NBTUtil.read(f).getTag();
                    if (!(newRoot instanceof CompoundTag comp)) return;

                    root.clear();
                    for (String k : comp.keySet()) {
                        root.put(k, comp.get(k));
                    }

                    currentFile[0] = f;
                    tree.setModel(new DefaultTreeModel(buildTree("Root", root)));

                    frame.setTitle("NBT 编辑器 - " + f.getName());
                    status.setText("Loaded " + f.getName());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            };

            // ================= 菜单栏 =================
            JMenuBar menuBar = new JMenuBar();

            JMenu fileMenu = new JMenu("File");
            JMenu editMenu = new JMenu("Edit");

            JMenuItem openItem = new JMenuItem("Open");
            JMenuItem saveItem = new JMenuItem("Save");
            JMenuItem saveAsItem = new JMenuItem("Save As");
            JMenuItem exitItem = new JMenuItem("Exit");

            JMenuItem addItem = new JMenuItem("Add");
            JMenuItem deleteItem = new JMenuItem("Delete");

            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

            openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
            saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
            saveAsItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_S,
                    shortcut | InputEvent.SHIFT_DOWN_MASK
            ));
            deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

            openItem.addActionListener(e -> open.run());
            saveItem.addActionListener(e -> save.run());
            saveAsItem.addActionListener(e -> saveAs.run());
            exitItem.addActionListener(e -> System.exit(0));

            addItem.addActionListener(e -> addNode(frame, tree, root));
            deleteItem.addActionListener(e -> deleteNode(tree, root));

            fileMenu.add(openItem);
            fileMenu.add(saveItem);
            fileMenu.add(saveAsItem);
            fileMenu.addSeparator();
            fileMenu.add(exitItem);

            editMenu.add(addItem);
            editMenu.add(deleteItem);

            menuBar.add(fileMenu);
            menuBar.add(editMenu);

            frame.setJMenuBar(menuBar);

            // ================= 底部按钮 =================
            JPanel panel = new JPanel();

            JButton openBtn = new JButton("Open");
            JButton saveBtn = new JButton("Save");
            JButton addBtn = new JButton("Add");
            JButton deleteBtn = new JButton("Delete");

            openBtn.addActionListener(e -> open.run());
            saveBtn.addActionListener(e -> save.run());
            addBtn.addActionListener(e -> addNode(frame, tree, root));
            deleteBtn.addActionListener(e -> deleteNode(tree, root));

            panel.add(openBtn);
            panel.add(saveBtn);
            panel.add(addBtn);
            panel.add(deleteBtn);

            frame.add(new JScrollPane(tree), BorderLayout.CENTER);
            frame.add(panel, BorderLayout.SOUTH);

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 图标缩放 =================
    private static ImageIcon icon(String name) {
        try {
            java.net.URL url = NBTEdit.class.getResource("/icons/" + name);
            if (url == null) return null;

            ImageIcon raw = new ImageIcon(url);
            Image img = raw.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            return new ImageIcon(img);

        } catch (Exception e) {
            return null;
        }
    }

    // ================= 构建树 =================
    private static DefaultMutableTreeNode buildTree(String name, Tag<?> tag) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TagWrapper(name, tag));

        if (tag instanceof CompoundTag c) {
            for (String k : c.keySet()) {
                node.add(buildTree(k, c.get(k)));
            }
        } else if (tag instanceof ListTag<?> l) {
            for (int i = 0; i < l.size(); i++) {
                node.add(buildTree("[" + i + "]", l.get(i)));
            }
        }

        return node;
    }

    // ================= 添加 =================
    private static void addNode(JFrame frame, JTree tree, CompoundTag root) {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) path.getLastPathComponent();

        if (!(node.getUserObject() instanceof TagWrapper w)) return;

        String name = JOptionPane.showInputDialog(frame, "Name:");
        if (name == null) return;

        String[] options = {
    "String",
    "Int",
    "Byte",
    "Long",
    "Double",
    "Float",
    "Compound",
    "List"
};

String type = (String) JOptionPane.showInputDialog(
        frame,
        "Select Tag Type:",
        "Add NBT Tag",
        JOptionPane.PLAIN_MESSAGE,
        null,
        options,
        "String"
);

if (type == null) return;

Tag<?> newTag;

switch (type) {
    case "Int":
        newTag = new IntTag(0);
        break;
    case "Byte":
        newTag = new ByteTag((byte) 0);
        break;
    case "Long":
        newTag = new LongTag(0L);
        break;
    case "Double":
        newTag = new DoubleTag(0.0);
        break;
    case "Float":
        newTag = new FloatTag(0f);
        break;
    case "Compound":
        newTag = new CompoundTag();
        break;
case "List":
    JOptionPane.showMessageDialog(frame, "ListTag 暂时不支持添加");
    return;
    default:
        newTag = new StringTag("");
}

        if (w.tag instanceof CompoundTag c) {
            c.put(name, newTag);
            node.add(new DefaultMutableTreeNode(new TagWrapper(name, newTag)));
        }

        ((DefaultTreeModel) tree.getModel()).reload(node);
    }

    // ================= 删除 =================
    private static void deleteNode(JTree tree, CompoundTag root) {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) path.getLastPathComponent();

        if (!(node.getUserObject() instanceof TagWrapper w)) return;

        DefaultMutableTreeNode parent =
                (DefaultMutableTreeNode) node.getParent();

        if (parent == null) return;

        if (parent.getUserObject() instanceof TagWrapper pw &&
                pw.tag instanceof CompoundTag comp) {

            comp.remove(w.name);
        }

        ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
    }

    // ================= 包装类 =================
    static class TagWrapper {
        String name;
        Tag<?> tag;

        TagWrapper(String name, Tag<?> tag) {
            this.name = name;
            this.tag = tag;
        }
    }
}