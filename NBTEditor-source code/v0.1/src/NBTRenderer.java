import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

import net.querz.nbt.tag.*;

public class NBTRenderer extends DefaultTreeCellRenderer {

    private Icon load(String name) {
        return new ImageIcon(
            NBTRenderer.class.getResource("/icons/" + name)
        );
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof javax.swing.tree.DefaultMutableTreeNode node) {
            Object user = node.getUserObject();

            if (user instanceof Tag<?> tag) {

                if (tag instanceof CompoundTag) {
                    setIcon(load("TAG_Compound.png"));

                } else if (tag instanceof ListTag) {
                    setIcon(load("TAG_List.png"));

                } else if (tag instanceof StringTag) {
                    setIcon(load("TAG_String.png"));

                } else if (tag instanceof IntTag) {
                    setIcon(load("TAG_Int.png"));

                } else if (tag instanceof ByteTag) {
                    setIcon(load("TAG_Byte.png"));

                } else if (tag instanceof ShortTag) {
                    setIcon(load("TAG_Short.png"));

                } else if (tag instanceof LongTag) {
                    setIcon(load("TAG_Long.png"));

                } else if (tag instanceof FloatTag) {
                    setIcon(load("TAG_Float.png"));

                } else if (tag instanceof DoubleTag) {
                    setIcon(load("TAG_Double.png"));

                } else if (tag instanceof ByteArrayTag) {
                    setIcon(load("TAG_Byte_Array.png"));

                } else if (tag instanceof IntArrayTag) {
                    setIcon(load("TAG_Int_Array.png"));

                } else if (tag instanceof LongArrayTag) {
                    setIcon(load("TAG_Long_Array.png"));
                }
            }
        }

        return this;
    }
}