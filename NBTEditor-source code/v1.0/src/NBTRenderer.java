/* 这是 NBTRenderer.java, 负责渲染 NBT 树形结构
   它继承自 DefaultTreeCellRenderer，用于为每个节点设置合适的图标和显示文本 */

// 由 huangzuan 制作, 如果你想使用或修改这个代码, 请遵守 MIT 协议, 并保留原作者信息
// 项目网址: https://github.com/huangzuan/NBTEditor/

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import net.querz.nbt.tag.*;

public class NBTRenderer extends DefaultTreeCellRenderer {

    // ===== 图标加载 =====
    private static Icon load(String n){
        try {
            Image original = javax.imageio.ImageIO.read(NBTRenderer.class.getResource("/icons/"+n));
            Image scaled = original.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    // ===== 图标 =====
    private static final Icon COM = load("TAG_Compound.png");
    private static final Icon LIS = load("TAG_List.png");
    private static final Icon STR = load("TAG_String.png");
    private static final Icon INT = load("TAG_Int.png");
    private static final Icon BYT = load("TAG_Byte.png");
    private static final Icon SHT = load("TAG_Short.png");
    private static final Icon LNG = load("TAG_Long.png");
    private static final Icon FLT = load("TAG_Float.png");
    private static final Icon DBL = load("TAG_Double.png");
    private static final Icon BA  = load("TAG_Byte_Array.png");
    private static final Icon IA  = load("TAG_Int_Array.png");
    private static final Icon LA  = load("TAG_Long_Array.png");
    private static final Icon UNK = load("TAG_Type_not_found.png");

    public NBTRenderer() {
        setLeafIcon(null);
        setClosedIcon(null);
        setOpenIcon(null);
        setIconTextGap(12);
        setBorder(new EmptyBorder(2, 0, 2, 0));
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean exp, boolean leaf, int row, boolean focus) {

        super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, focus);

        if (value instanceof javax.swing.tree.DefaultMutableTreeNode n &&
                n.getUserObject() instanceof NBTEdit.TagWrapper w) {

            Tag<?> t = w.tag;

            /////////////
           String text;

if (t instanceof CompoundTag) {
    text = w.name; // ❗ 不显示内容
} else if (t instanceof ListTag) {
    text = w.name; // ❗ 不显示内容
} else if (t instanceof StringTag s) {
    text = w.name + ": " + s.getValue();
} else {
    text = w.name + ": " + t.toString();
}

setText(text);

            // ===== 图标 =====
            if (t instanceof CompoundTag) setIcon(COM);
            else if (t instanceof ListTag) setIcon(LIS);
            else if (t instanceof StringTag) setIcon(STR);
            else if (t instanceof IntTag) setIcon(INT);
            else if (t instanceof ByteTag) setIcon(BYT);
            else if (t instanceof ShortTag) setIcon(SHT);
            else if (t instanceof LongTag) setIcon(LNG);
            else if (t instanceof FloatTag) setIcon(FLT);
            else if (t instanceof DoubleTag) setIcon(DBL);
            else if (t instanceof ByteArrayTag) setIcon(BA);
            else if (t instanceof IntArrayTag) setIcon(IA);
            else if (t instanceof LongArrayTag) setIcon(LA);
            else setIcon(UNK);
        }

        return this;
    }

    public static void setupTree(JTree tree) {
        tree.setCellRenderer(new NBTRenderer());
        tree.setRowHeight(36);
    }
}