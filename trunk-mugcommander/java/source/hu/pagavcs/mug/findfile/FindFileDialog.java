package hu.pagavcs.mug.findfile;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.mucommander.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * This dialog allows the user to add a bookmark and enter a name for it. User
 * can also choose to store login and password information in the bookmark's URL
 * if the bookmark contains login/password information.
 * 
 * @author Maxence Bernard
 */
public class FindFileDialog extends FocusDialog implements ActionListener {

	private JTextField             nameField;
	private JTextField             sfSearchText;
	private JTextField             sfSearchTextEncoding;
	private JCheckBox              cbCaseSensitive;
	private JCheckBox              cbSearchInArchive;
	private JCheckBox              cbIncludeExclude;
	private JCheckBox              cbIncludeSelected;
	private JCheckBox              cbExcludeSelected;
	private JTextArea              taIncludeExclude;

	private JButton                btnStartSearch;
	private JButton                btnCancel;

	// Dialog's width has to be at least 320
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320, 0);

	// Dialog's width has to be at most 400
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400, 10000);
	private final MainFrame        mainFrame;
	private String                 findInProgress;

	public FindFileDialog(MainFrame mainFrame) {
		super(mainFrame, ActionProperties.getActionLabel(FindFileAction.Descriptor.ACTION_ID), mainFrame);
		// super(mainFrame, "Find File", mainFrame);
		this.mainFrame = mainFrame;

		Container contentPane = getContentPane();
		YBoxPanel mainPanel = new YBoxPanel(5);

		final AbstractFile currentFolder = mainFrame.getActiveTable().getCurrentFolder();

		final XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

		nameField = new JTextField("*");
		cbSearchInArchive = new JCheckBox("Search in archive");
		sfSearchText = new JTextField("");
		cbCaseSensitive = new JCheckBox("Case sensitive");
		sfSearchTextEncoding = new JTextField("UTF-8");
		cbIncludeExclude = new JCheckBox("Include/Exclude");
		cbIncludeSelected = new JCheckBox("Search only in selected dirs");
		cbIncludeSelected.setVisible(false);
		cbExcludeSelected = new JCheckBox("Don't search in selected dirs");
		cbExcludeSelected.setVisible(false);
		taIncludeExclude = new JTextArea();
		taIncludeExclude.setToolTipText("example: -/home/moo/private/*");
		taIncludeExclude.setVisible(false);

		cbIncludeExclude.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				boolean select = e.getStateChange() == ItemEvent.SELECTED;
				// taIncludeExclude.setVisible(true);
				cbIncludeSelected.setVisible(select);
				cbExcludeSelected.setVisible(select);
				compPanel.revalidate();
				compPanel.repaint();
				pack();
			}
		});

		cbIncludeSelected.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					cbExcludeSelected.setSelected(false);
				}
			}
		});

		cbExcludeSelected.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					cbIncludeSelected.setSelected(false);
				}
			}
		});

		compPanel.addRow("Search in:", new JLabel(currentFolder.getPath()), 10);

		compPanel.addRow("Search for:", nameField, 10);
		compPanel.addRow(cbSearchInArchive, 10);
		compPanel.addRow(cbIncludeExclude, 10);
		compPanel.addRow(cbIncludeSelected, 10);
		compPanel.addRow(cbExcludeSelected, 10);
		compPanel.addRow(taIncludeExclude, 10);
		compPanel.addRow("Find text:", sfSearchText, 10);
		compPanel.addRow(cbCaseSensitive, 10);
		compPanel.addRow("Text encoding:", sfSearchTextEncoding, 10);

		mainPanel.add(compPanel);

		contentPane.add(mainPanel, BorderLayout.NORTH);

		btnStartSearch = new JButton("Start search");
		btnStartSearch.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				startSearch(currentFolder);
			}

		});
		btnCancel = new JButton(Translator.get("cancel"));
		btnCancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				cancelSearch();
			}
		});
		contentPane.add(DialogToolkit.createOKCancelPanel(btnStartSearch, btnCancel, getRootPane(), this), BorderLayout.SOUTH);

		// Select text in name field and transfer focus to it for immediate user
		// change
		nameField.selectAll();
		setInitialFocusComponent(nameField);

		// Packs dialog
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

		showDialog();
	}


	private void cancelSearch() {
		if (findInProgress == null) {
			dispose();
		} else {
			FindManager.getInstance().cancelSearch(findInProgress);
		}

	}

	private void startSearch(final AbstractFile currentFolder) {
		if (findInProgress != null) {
			return;
		}
		try {
			final String searchFileName = nameField.getText();
			final String searchText = sfSearchText.getText();
			final String searchTextEncoding = sfSearchTextEncoding.getText();
			final boolean caseSensitive = cbCaseSensitive.isSelected();
			final boolean searchInArchive = cbSearchInArchive.isSelected();
			final String findId = currentFolder + "|" + searchFileName + "|" + searchText + "|" + caseSensitive + "|" + searchInArchive + "|" + new Date();
			final List<String> lstInclude = new ArrayList<String>();
			final List<String> lstExclude = new ArrayList<String>();
			if (cbIncludeExclude.isSelected()) {
				if (cbIncludeSelected.isSelected()) {
					for (AbstractFile f : mainFrame.getActiveTable().getSelectedFiles()) {
						lstInclude.add(f.getAbsolutePath() + "*");
					}
				}
				if (cbExcludeSelected.isSelected()) {
					for (AbstractFile f : mainFrame.getActiveTable().getSelectedFiles()) {
						lstExclude.add(f.getAbsolutePath() + "*");
					}
				}
			}

			findInProgress = findId;

			btnStartSearch.setText("Searching...");

			Thread searchThread = new Thread(new Runnable() {

				public void run() {

					FindManager.getInstance().startSearch(mainFrame, findId, currentFolder, searchFileName, searchText, searchTextEncoding, caseSensitive,
					        searchInArchive, lstInclude, lstExclude);

					findInProgress = null;
					if (FindManager.getInstance().isCancel(findId)) {
						FindManager.getInstance().removeCancel(findId);
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {
								btnStartSearch.setText("Start search");
							}
						});
					} else {
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {
								FindFileDialog.this.dispose();
							}
						});
					}
				}

			});
			searchThread.setName("Search");
			searchThread.setDaemon(true);
			searchThread.start();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {}

}
