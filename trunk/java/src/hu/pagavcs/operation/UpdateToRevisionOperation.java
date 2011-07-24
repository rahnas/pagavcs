package hu.pagavcs.operation;

import hu.pagavcs.bl.Cancelable;
import hu.pagavcs.bl.Manager;
import hu.pagavcs.bl.PagaException;
import hu.pagavcs.bl.SvnHelper;
import hu.pagavcs.gui.UpdateToRevisionGui;

import java.io.File;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

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
public class UpdateToRevisionOperation implements Cancelable {

	private String              path;
	private UpdateToRevisionGui gui;
	private boolean             autoClose;
	private boolean             cancel;

	public UpdateToRevisionOperation(String path) throws BackingStoreException, SVNException {
		this.path = path;
	}

	public void execute() throws SVNException, BackingStoreException, PagaException {
		gui = new UpdateToRevisionGui(this);
		gui.display();
		gui.setStatus(GeneralStatus.INIT);
		File wcFile = new File(path);
		SVNClientManager mgrSvn = Manager.getSVNClientManager(new File(path));
		SVNWCClient wcClient = mgrSvn.getWCClient();
		gui.setStatus(GeneralStatus.START);
		try {
			SVNInfo svnInfo = wcClient.doInfo(wcFile, SVNRevision.WORKING);

			gui.setURL(svnInfo.getURL().toDecodedString());

			gui.setStatus(GeneralStatus.COMPLETED);
		} catch (SVNException ex) {
			Manager.handle(ex);
			gui.setStatus(GeneralStatus.FAILED);
		} finally {
			mgrSvn.dispose();
		}
		if (autoClose) {
			gui.close();
		}
	}

	public String getPath() {
		return path;
	}

	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	public boolean isAutoClose() {
		return autoClose;
	}

	public void setCancel(boolean cancel) throws Exception {
		if (this.cancel != cancel) {
			this.cancel = cancel;
			if (cancel) {
				gui.setCancel(true);
			}
		}
	}

	public boolean isCancel() {
		return cancel;
	}

	public void doUpdateToRevision(String wc, String toRevision) throws Exception {
		SVNRevision revision;
		if (toRevision.isEmpty()) {
			revision = SVNRevision.HEAD;
		} else {
			revision = SVNRevision.create(Long.valueOf(toRevision));
		}
		Update update = new Update(Arrays.asList(wc));
		update.setUpdateToRevision(revision);
		update.execute();
	}

	public void doShowLog(String pathToShowLog) throws SVNException, BackingStoreException, Exception {
		new Log(pathToShowLog).execute();
	}

	public void storeUrlForHistory(String url) {
		SvnHelper.storeUrlForHistory(url);
	}
}
