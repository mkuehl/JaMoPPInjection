package preprocessing.diffs;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Designed to contain all changes for a single class.
 * @author Max
 *
 */
public class Changes implements Iterable<Change>, Iterator<Change> {

	private String commitHash;
	private String commitMessage;
	private LinkedList<Change> changesList;
	
	// iterator related
	int index;
	
	public Changes() {
		commitHash = "";
		commitMessage = "";
		changesList = new LinkedList<Change>();
		index = 0;
	}
	
	public void setCommitHash(String p_commitHash) {
		commitHash = p_commitHash;
	}
	
	public String getCommitHash() {
		return commitHash;
	}
	
	public void setCommitMessage(String p_commitMessage) {
		commitMessage = p_commitMessage;
	}
	
	public String getCommitMessage() {
		return commitMessage;
	}
	
	public void add(Change change) {
		changesList.add(change);
	}
	
	public void setToFirst() {
		index = 0;
	}
	
	public void setToLast() {
		index = changesList.size()-1;
	}
	
	/**
	 * Returns changes stored in changesList. If null, null is returned.
	 * @return
	 */
	public Change getChange() {
		return changesList != null ? changesList.get(index) : null;
	}
	
	public Change getChange(int p_index) {
		return changesList.get(p_index);
	}
	
	public int size() {
		return changesList.size();
	}
	
	/** 
	 * Checks if the contained list of Change objects is empty
	 * @return
	 */
	public boolean isEmpty() {
		if (index >= size() || index < 0) {
			throw new NoSuchElementException();
		}
		return changesList.isEmpty();
	}
	
	public boolean hasPrevious() {
		if (index > -1) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasNext() {
		if (index < size()) {
			return true;
		}
		return false;
	}

	@Override
	public Iterator<Change> iterator() {
		// TODO Auto-generated method stub
		return this;
	}

	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Change next() {
		if (index == changesList.size()) {
			throw new NoSuchElementException();
		}
		return changesList.get(index++);
	}

	public Change previous() {
		if (index < 0) {
			throw new NoSuchElementException();
		}
		return changesList.get(index--);
	}
	
	public void clear() {
		changesList.clear();
	}
	
	/**
	 * Checks if commit hash is set and the Change list has at least one element. The commit message can and
	 * must not be checked because it is optional.
	 * @return
	 */
	public boolean isEverythingSet() {
		if (!changesList.isEmpty() && commitHash != "") {
			return true;
		}
		return false;
	}
}
