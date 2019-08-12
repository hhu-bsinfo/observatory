package de.hhu.bsinfo.observatory.plot;

import java.awt.Component;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

/**
 * Based on http://blog.mynotiz.de/programmieren/java-checkbox-in-jlist-1061/
 */
class JCheckBoxList extends JList<JCheckBox> {

    JCheckBoxList(String[] objects) {
        JCheckBox[] checkBoxes = new JCheckBox[objects.length];

        for(int i = 0; i < objects.length; i++) {
            checkBoxes[i] = new JCheckBox(objects[i]);
        }

        DefaultListModel<JCheckBox> model = new DefaultListModel<>();
        model.addAll(Arrays.asList(checkBoxes));

        setModel(model);

        setCellRenderer(new CellRenderer<>());

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    JCheckBox checkbox = getModel().getElementAt(index);
                    checkbox.setSelected(!checkbox.isSelected());
                    repaint();
                }
            }
        });

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private static class CellRenderer<T> implements ListCellRenderer<T> {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JCheckBox checkbox = (JCheckBox) value;

            checkbox.setBackground(UIManager.getColor("List.background"));

            return checkbox;
        }
    }

    void addItemListener(ItemListener listener) {
        for(int i = 0; i < getModel().getSize(); i++) {
            getModel().getElementAt(i).addItemListener(listener);
        }
    }

    void selectAll() {
        int size = this.getModel().getSize();
        for (int i = 0; i < size; i++) {
            JCheckBox checkbox = this.getModel().getElementAt(i);
            checkbox.setSelected(true);
        }
        this.repaint();
    }

    void deselectAll() {
        int size = this.getModel().getSize();
        for (int i = 0; i < size; i++) {
            JCheckBox checkbox = this.getModel().getElementAt(i);
            checkbox.setSelected(false);
        }
        this.repaint();
    }
}