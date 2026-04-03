import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class NBTEdit {

    public static void main(String[] args) {


    // 先检查更新
    UpdateChecker.checkOnStart();

    // 原来的界面初始化代码……


        JFrame frame = new JFrame("NBT 编辑器");
        frame.setSize(700, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

        try {
            File file = chooser.getSelectedFile();

            Tag<?> rootRaw = NBTUtil.read(file).getTag();
            if (!(rootRaw instanceof CompoundTag rootTag)) {
                JOptionPane.showMessageDialog(null, "不是 Compound 根！");
                return;
            }

            DefaultMutableTreeNode rootNode = buildTree("Root", rootTag);
            JTree tree = new JTree(rootNode);
            tree.setCellRenderer(new NBTRenderer());
            tree.setRowHeight(50);  // 每行高度 50 像素

            // 双击编辑
            tree.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {

                        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                        if (path == null) return;

                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (!(node.getUserObject() instanceof TagWrapper w)) return;

                        if (w.tag instanceof CompoundTag || w.tag instanceof ListTag) return;

                        String oldVal = getValue(w.tag);
                        String newVal = JOptionPane.showInputDialog(frame, "修改值:", oldVal);

                        if (newVal != null) {
                            try {
                                setValue(w.tag, newVal);
                                ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(frame, "修改失败！");
                            }
                        }
                    }
                }
            });

            // 保存
            Runnable save = () -> {
                try {
                    NBTUtil.write(new NamedTag("", rootTag), file);
                    JOptionPane.showMessageDialog(frame, "保存成功");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "保存失败");
                }
            };

            JButton addBtn = new JButton("添加节点");
            addBtn.addActionListener(e -> addNode(frame, tree));

            JButton delBtn = new JButton("删除节点");
            delBtn.addActionListener(e -> deleteNode(frame, tree));

            JButton saveBtn = new JButton("保存");
            saveBtn.addActionListener(e -> save.run());

            // 快捷键
            InputMap im = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = frame.getRootPane().getActionMap();
            im.put(KeyStroke.getKeyStroke("meta S"), "save");
            im.put(KeyStroke.getKeyStroke("control S"), "save");
            am.put("save", new AbstractAction() {
                public void actionPerformed(ActionEvent e) { save.run(); }
            });

            JPanel panel = new JPanel();
            panel.add(saveBtn);
            panel.add(addBtn);
            panel.add(delBtn);

            frame.add(new JScrollPane(tree), BorderLayout.CENTER);
            frame.add(panel, BorderLayout.SOUTH);
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== 添加节点 =====
    private static void addNode(JFrame frame, JTree tree) {

        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (!(node.getUserObject() instanceof TagWrapper w)) return;

        Tag<?> parent = w.tag;

       String[] types = {
    "Byte","Short","Int","Long","Float","Double",
    "String","ByteArray","IntArray","LongArray",
    "Compound"
};

        String type = (String) JOptionPane.showInputDialog(
                frame,"选择类型","添加",
                JOptionPane.PLAIN_MESSAGE,null,types,types[0]);

        if (type == null) return;

        Tag<?> newTag = createTag(frame, type);
        if (newTag == null) return;

        try {
            if (parent instanceof CompoundTag comp) {

                String name = JOptionPane.showInputDialog(frame,"名称:");
                if (name == null || name.isEmpty()) return;

                comp.put(name, newTag);
                node.add(new DefaultMutableTreeNode(new TagWrapper(name,newTag)));

            } else if (parent instanceof ListTag<?> listRaw) {

                ListTag<Tag<?>> list = (ListTag<Tag<?>>) listRaw;

                if (list.size() == 0) {
                    list.add(newTag);
                } else if (list.get(0).getID() != newTag.getID()) {
                    JOptionPane.showMessageDialog(frame,"列表类型不匹配");
                    return;
                } else {
                    list.add(newTag);
                }

                node.add(new DefaultMutableTreeNode(
                        new TagWrapper("["+(list.size()-1)+"]", newTag)));

            } else return;

            ((DefaultTreeModel) tree.getModel()).reload(node);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,"添加失败");
        }
    }

    // ===== 删除 =====
    private static void deleteNode(JFrame frame, JTree tree) {

        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.isRoot()) return;

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();

        TagWrapper w = (TagWrapper) node.getUserObject();
        TagWrapper pw = (TagWrapper) parentNode.getUserObject();

        if (pw.tag instanceof CompoundTag comp) {
            comp.remove(w.name);
        } else if (pw.tag instanceof ListTag<?> listRaw) {

            ListTag<Tag<?>> list = (ListTag<Tag<?>>) listRaw;
            list.remove(parentNode.getIndex(node));

            parentNode.removeAllChildren();
            for (int i = 0; i < list.size(); i++) {
                parentNode.add(new DefaultMutableTreeNode(
                        new TagWrapper("["+i+"]", list.get(i))));
            }
        }

        ((DefaultTreeModel) tree.getModel()).reload(parentNode);
    }

    // ===== 创建 Tag =====
    private static Tag<?> createTag(Component c,String type) {
        try {
            switch (type) {
                case "Byte": return new ByteTag(Byte.parseByte(JOptionPane.showInputDialog(c)));
                case "Short": return new ShortTag(Short.parseShort(JOptionPane.showInputDialog(c)));
                case "Int": return new IntTag(Integer.parseInt(JOptionPane.showInputDialog(c)));
                case "Long": return new LongTag(Long.parseLong(JOptionPane.showInputDialog(c)));
                case "Float": return new FloatTag(Float.parseFloat(JOptionPane.showInputDialog(c)));
                case "Double": return new DoubleTag(Double.parseDouble(JOptionPane.showInputDialog(c)));
                case "String": return new StringTag(JOptionPane.showInputDialog(c));
                case "ByteArray": return new ByteArrayTag(new byte[0]);
                case "IntArray": return new IntArrayTag(new int[0]);
                case "LongArray": return new LongArrayTag(new long[0]);
                case "Compound": return new CompoundTag();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static DefaultMutableTreeNode buildTree(String name, Tag<?> tag) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TagWrapper(name, tag));

        if (tag instanceof CompoundTag c)
            for (String k : c.keySet())
                node.add(buildTree(k, c.get(k)));

        else if (tag instanceof ListTag<?> l)
            for (int i=0;i<l.size();i++)
                node.add(buildTree("["+i+"]", l.get(i)));

        return node;
    }

    private static String getValue(Tag<?> t) {
        if (t instanceof ByteTag b) return String.valueOf(b.asByte());
        if (t instanceof ShortTag s) return String.valueOf(s.asShort());
        if (t instanceof IntTag i) return String.valueOf(i.asInt());
        if (t instanceof LongTag l) return String.valueOf(l.asLong());
        if (t instanceof FloatTag f) return String.valueOf(f.asFloat());
        if (t instanceof DoubleTag d) return String.valueOf(d.asDouble());
        if (t instanceof StringTag s) return s.getValue();
        return "";
    }

    private static void setValue(Tag<?> t,String v) {
        if (t instanceof ByteTag b) b.setValue(Byte.parseByte(v));
        else if (t instanceof ShortTag s) s.setValue(Short.parseShort(v));
        else if (t instanceof IntTag i) i.setValue(Integer.parseInt(v));
        else if (t instanceof LongTag l) l.setValue(Long.parseLong(v));
        else if (t instanceof FloatTag f) f.setValue(Float.parseFloat(v));
        else if (t instanceof DoubleTag d) d.setValue(Double.parseDouble(v));
        else if (t instanceof StringTag s) s.setValue(v);
    }

    static class TagWrapper {
        String name;
        Tag<?> tag;

        TagWrapper(String n,Tag<?> t){name=n;tag=t;}

        public String toString(){
            if (tag instanceof CompoundTag || tag instanceof ListTag) return name;
            if (tag instanceof StringTag s) return name + ": \""+s.getValue()+"\"";
            return name + ": " + getValue(tag);
        }
    }
}