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

	// contains the class/file name
	private String classFile;
	private LinkedList<Change> changesList;
	
	// iterator related
	int index;
	
	public Changes() {
		changesList = new LinkedList<Change>();
		index = 0;
	}
	
	public void setClassFile(String p_classFile) {
		classFile = p_classFile;
	}
	
	public String getClassFile() {
		return classFile;
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
	
	public Change getChange() {
		return changesList.get(index);
	}
	
	public boolean isEmpty() {
		if (index >= changesList.size() || index < 0) {
			throw new NoSuchElementException();
		}
		return changesList.isEmpty();
	}

	@Override
	public Iterator<Change> iterator() {
		// TODO Auto-generated method stub
		return this;
	}
	
	public boolean hasNext() {
		if (index < changesList.size()) {
			return true;
		}
		return false;
	}
	
	public boolean hasPrevious() {
		if (index > 0) {
			return true;
		}
		return false;
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

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
