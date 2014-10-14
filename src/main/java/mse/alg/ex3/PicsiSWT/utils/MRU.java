package mse.alg.ex3.PicsiSWT.utils;


import mse.alg.ex3.PicsiSWT.gui.MainWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.util.prefs.Preferences;

public class MRU {
	private static int MaxMRUitems = 4;
	
	MainWindow m_mainWindow;
	private Preferences m_prefs;

	public MRU(MainWindow mw) {
		m_mainWindow = mw;
		m_prefs = Preferences.userRoot();
		//m_prefs = Preferences.userNodeForPackage(PicsiSWT.class);
	}
	
	public void addFileName(String fileName) {
		assert fileName != null : "filename is null";
		
		if (m_prefs != null) {
			// add filename to prefs
			Preferences p = m_prefs.node("/MRU");
			if (p == null) return;

			int top = p.getInt("Top", 0);
			top++;
			if (top > MaxMRUitems) top = 1;
			p.putInt("Top", top);
			
			p.put(String.valueOf(top), fileName);
		}
	}

	public void addRecentFiles(Menu recent) {
		assert recent != null : "recent menu is null";
		
		if (m_prefs != null) {
			// read filenames from prefs
			Preferences p = m_prefs.node("/MRU");
			if (p == null) return;
			
			int top = p.getInt("Top", 1);
			
			for (int i=0; i < MaxMRUitems; i++) {
				MenuItem item = new MenuItem(recent, SWT.PUSH);
				final String fileName = p.get(String.valueOf(top), "");
				if (!fileName.isEmpty()) {
					item.setText(fileName);
					item.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							if (m_mainWindow.updateFile(fileName)) {
								// move fileName to top position in MRU
								moveFileNameToTop(fileName);
							} else {
								// remove fileName from MRU
								removeFileName(fileName);
							}
						}
					});
				}
				top--;
				if (top == 0) top = MaxMRUitems;
			}
		}
	}
	
	private void moveFileNameToTop(String fileName) {
		assert fileName != null : "filename is null";
		
		if (m_prefs != null) {
			Preferences p = m_prefs.node("/MRU");
			if (p == null) return;
			
			int top = p.getInt("Top", 1);
			
			// find position of fileName
			for (int i=1; i <= MaxMRUitems; i++) {
				String key = String.valueOf(i);
				String fn = p.get(key, "");
				if (i != top && fileName.equals(fn)) {
					// swap i with top + 1
					top++;
					if (top > MaxMRUitems) top = 1;
					String key2 = String.valueOf(top);
					String fn2 = p.get(key2, "");
					
					p.put(key, fn2);
					p.put(key2, fn);
					p.putInt("Top", top);
				}
			}
		}
	}
	
	private void removeFileName(String fileName) {
		assert fileName != null : "filename is null";
		
		if (m_prefs != null) {
			Preferences p = m_prefs.node("/MRU");
			if (p == null) return;
			
			for (int i=1; i <= MaxMRUitems; i++) {
				String key = String.valueOf(i);
				String fn = p.get(key, "");
				if (fileName.equals(fn)) {
					p.put(key, "");
				}
			}
		}
	}
	
}
