package chat.gui;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTabbedPane;

public class SortedTabbedPane extends JTabbedPane
{
	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int unused)
	{
		int low = 0;
		int high = this.getTabCount() - 1;
		int index = low;
		while (low <= high) {
			index = (low + high) >>> 1;
			int cmp = this.getTabComponentAt(index).getName().compareTo(title);
			
			if (cmp < 0)
				low = index + 1;
			else if (cmp > 0)
				high = index - 1;
		}
		super.insertTab(title, icon, component, tip, index);
	}
}
