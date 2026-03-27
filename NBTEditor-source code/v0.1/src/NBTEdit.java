import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class NBTEdit {

    public static void main(String[] args) {
        JFrame frame = new JFrame("NBT 编辑器");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(null);

        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            File file = chooser.getSelectedFile();

            Tag<?> rootTagRaw = NBTUtil.read(file).getTag();
            if (!(rootTagRaw instanceof CompoundTag rootTag)) {
                JOptionPane.showMessageDialog(null, "选择的不是 CompoundTag 根文件！");
                return;
            }

            DefaultMutableTreeNode rootNode = buildTree("Root", rootTag);
            JTree tree = new JTree(rootNode);
            tree.setCellRenderer(new NBTRenderer());

            // 双击修改叶子节点
            tree.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                        if (path == null) return;
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        Object userObj = node.getUserObject();
                        if (userObj instanceof TagWrapper wrapper) {
                            if (wrapper.tag instanceof CompoundTag || wrapper.tag instanceof ListTag) return;
                            String oldValue = getTagValueAsString(wrapper.tag);
                            String newValue = JOptionPane.showInputDialog(frame, "修改值:", oldValue);
                            if (newValue != null) {
                                try {
                                    updateTagValue(wrapper.tag, newValue);
                                    ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(frame, "修改失败: " + ex.getMessage());
                                }
                            }
                        }
                    }
                }
            });

            // 保存逻辑
            Runnable saveAction = () -> {
                try {
                    NBTUtil.write(new NamedTag("", rootTag), file);
                    JOptionPane.showMessageDialog(frame, "保存成功！");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "保存失败: " + ex.getMessage());
                }
            };

            // 按钮
            JButton saveButton = new JButton("保存文件");
            saveButton.addActionListener(e -> saveAction.run());

            JButton addButton = new JButton("添加节点");
            addButton.addActionListener(e -> {
                TreePath path = tree.getSelectionPath();
                if (path == null) {
                    JOptionPane.showMessageDialog(frame, "请先选择一个节点！");
                    return;
                }
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObj = selectedNode.getUserObject();
                if (!(userObj instanceof TagWrapper wrapper)) {
                    JOptionPane.showMessageDialog(frame, "只能在 Compound 或 List 节点下添加子节点！");
                    return;
                }

                Tag<?> parentTag = wrapper.tag;
                if (parentTag instanceof CompoundTag compound) {
                    String newName = JOptionPane.showInputDialog(frame, "请输入新节点名字:");
                    if (newName == null || newName.isEmpty()) return;
                    String newValue = JOptionPane.showInputDialog(frame, "请输入新节点值（字符串类型）:");
                    if (newValue == null) return;

                    StringTag newTag = new StringTag(newValue);
                    compound.put(newName, newTag);

                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new TagWrapper(newName, newTag));
                    selectedNode.add(newNode);
                    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(selectedNode);
                    tree.expandPath(path);

                } else if (parentTag instanceof ListTag<?> genericList) {
                    String newValue = JOptionPane.showInputDialog(frame, "请输入新列表元素值（字符串类型）:");
                    if (newValue == null) return;
                    StringTag newTag = new StringTag(newValue);

                    @SuppressWarnings("unchecked")
                    ListTag<Tag<?>> list = (ListTag<Tag<?>>) genericList;
                    list.add(newTag);

                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
                            new TagWrapper("[" + (list.size() - 1) + "]", newTag)
                    );
                    selectedNode.add(newNode);
                    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(selectedNode);
                    tree.expandPath(path);
                } else {
                    JOptionPane.showMessageDialog(frame, "无法在叶子节点下添加子节点！");
                }
            });

            JButton deleteButton = new JButton("删除节点");
            deleteButton.addActionListener(e -> {
                TreePath path = tree.getSelectionPath();
                if (path == null) {
                    JOptionPane.showMessageDialog(frame, "请先选择一个节点！");
                    return;
                }
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (selectedNode.isRoot()) {
                    JOptionPane.showMessageDialog(frame, "根节点不能删除！");
                    return;
                }

                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
                Object userObj = selectedNode.getUserObject();
                Object parentObj = parentNode.getUserObject();

                int confirm = JOptionPane.showConfirmDialog(frame, "确定删除选中节点吗？", "确认删除", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;

                try {
                    if (userObj instanceof TagWrapper wrapper) {
                        Tag<?> parentTag = ((TagWrapper) parentObj).tag;
                        if (parentTag instanceof CompoundTag compound) {
                            compound.remove(wrapper.name);
                        } else if (parentTag instanceof ListTag<?> list) {
                            int index = parentNode.getIndex(selectedNode);
                            list.remove(index);
                        }
                    }
                    parentNode.remove(selectedNode);
                    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(parentNode);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "删除失败: " + ex.getMessage());
                }
            });

            // 快捷键 Ctrl+S / ⌘+S
            InputMap im = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = frame.getRootPane().getActionMap();
            KeyStroke ks = KeyStroke.getKeyStroke("control S");
            if (System.getProperty("os.name").toLowerCase().contains("mac")) ks = KeyStroke.getKeyStroke("meta S");
            im.put(ks, "save");
            am.put("save", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveAction.run();
                }
            });

            // 底部按钮面板
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(saveButton);
            buttonPanel.add(addButton);
            buttonPanel.add(deleteButton);

            frame.add(new JScrollPane(tree), BorderLayout.CENTER);
            frame.add(buttonPanel, BorderLayout.SOUTH);
            frame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static DefaultMutableTreeNode buildTree(String name, Tag<?> tag) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TagWrapper(name, tag));

        if (tag instanceof CompoundTag compound) {
            for (String key : compound.keySet()) {
                node.add(buildTree(key, compound.get(key)));
            }
        } else if (tag instanceof ListTag<?> list) {
            for (int i = 0; i < list.size(); i++) {
                node.add(buildTree("[" + i + "]", list.get(i)));
            }
        }

        return node;
    }

    private static String getTagValueAsString(Tag<?> tag) {
        if (tag instanceof ByteTag) return String.valueOf(((ByteTag) tag).asByte());
        if (tag instanceof ShortTag) return String.valueOf(((ShortTag) tag).asShort());
        if (tag instanceof IntTag) return String.valueOf(((IntTag) tag).asInt());
        if (tag instanceof LongTag) return String.valueOf(((LongTag) tag).asLong());
        if (tag instanceof FloatTag) return String.valueOf(((FloatTag) tag).asFloat());
        if (tag instanceof DoubleTag) return String.valueOf(((DoubleTag) tag).asDouble());
        if (tag instanceof StringTag) return ((StringTag) tag).getValue();
        return "";
    }

    private static void updateTagValue(Tag<?> tag, String value) {
        if (tag instanceof ByteTag) ((ByteTag) tag).setValue(Byte.parseByte(value));
        if (tag instanceof ShortTag) ((ShortTag) tag).setValue(Short.parseShort(value));
        if (tag instanceof IntTag) ((IntTag) tag).setValue(Integer.parseInt(value));
        if (tag instanceof LongTag) ((LongTag) tag).setValue(Long.parseLong(value));
        if (tag instanceof FloatTag) ((FloatTag) tag).setValue(Float.parseFloat(value));
        if (tag instanceof DoubleTag) ((DoubleTag) tag).setValue(Double.parseDouble(value));
        if (tag instanceof StringTag) ((StringTag) tag).setValue(value);
    }

    private static class TagWrapper {
        String name;
        Tag<?> tag;

        TagWrapper(String name, Tag<?> tag) {
            this.name = name;
            this.tag = tag;
        }

        @Override
        public String toString() {
            if (tag instanceof CompoundTag || tag instanceof ListTag) return name;
            return name + ": " + getTagValueAsString(tag);
        }
    }
}