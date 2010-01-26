package hu.pagavcs.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * PagaVCS is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.<br>
 * <br>
 * PagaVCS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * PagaVCS; If not, see http://www.gnu.org/licenses/.
 */
public abstract class AbstractCellRendererColorizator<L extends ListItem> implements TableCellRenderer {

	private final TableCellRenderer delegate;
	private static final Color      alternateColor = new Color(240, 240, 240);

	public AbstractCellRendererColorizator(Table table) {
		delegate = table.getDefaultRenderer(Object.class);
		table.setDefaultRenderer(Object.class, this);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {

		Component delegeteComponent = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
		if (!isSelected) {
			delegeteComponent.setForeground(getForegroundColor((L) ((TableModel) (table.getModel())).getRow(table.convertRowIndexToModel(rowIndex))));
			delegeteComponent.setBackground(rowIndex % 2 == 0 ? alternateColor : null);
		} else {
			delegeteComponent.setForeground(null);
		}

		return delegeteComponent;
	}

	public abstract Color getForegroundColor(L li);

}