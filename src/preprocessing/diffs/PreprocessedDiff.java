package preprocessing.diffs;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * PreprocessedDiff is designated to keep all modifications within its list.
 * The particular changes and necessary information is kept within the Changes
 * objects.
 * @author Max
 *
 */
public class PreprocessedDiff implements Iterable<Changes>, Iterator<Changes> {

	/* 
	 * Contains all changes and necessary meta information. Each change object
	 * is designed to keep all changes of a particular class.
	 */
	private LinkedList<Changes> modificationList;
	
	int index;

	public PreprocessedDiff() {
		modificationList = new LinkedList<Changes>();
		index = 0;
	}
	
	/**
	 * Adds code changes with meta information as Changes object to the specified
	 * list. If no list object exists or the parameter is null, nothing is added.
	 * @param changes
	 */
	public void add(Changes changes) {
		if (modificationList != null && changes != null) {
			modificationList.add(changes);
		}
	}
	
	public void setModificationList(LinkedList<Changes> changesList) {
		modificationList = changesList;
	}
	
	public void setToFirst() {
		index = 0;
	}
	
	public void setToLast() {
		index = modificationList.size()-1;
	}
	
	/**
	 * Returns changes stored in modificationList. If null, null is returned.
	 * @return
	 */
	public Changes getChanges() {
		return modificationList != null ? get(index) : null;
	}
	
	/**
	 * Returns size of inner modifications list. If empty, -1 is returned.
	 * @return
	 */
	public int size() {
		if (modificationList != null) {
			return modificationList.size();
		}
		return -1;
	}
	
	public boolean hasPrevious() {
		if (index > 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasNext() {
		if (index < modificationList.size()) {
			return true;
		}
		return false;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Changes> iterator() {
		return this;
	}

	@Override
	public Changes next() {
		if (index == modificationList.size()) {
			throw new NoSuchElementException();
		}
		return modificationList.get(index++);
	}
	
	public Changes previous() {
		if (index < 0) {
			throw new NoSuchElementException();
		}
		return modificationList.get(index--);
	}

	
	public void clear() {
		modificationList = new LinkedList<Changes>();
		index = 0;
	}
	
	/**
	 * If p_index is smaller than the size of the list, a normal get(index) is performed. If p_index is equal
	 * to the modificationList's size the last item is returned.
	 * @param p_index
	 * @return
	 */
	private Changes get(int p_index) {
		int size = modificationList.size();
		return p_index >= size ? modificationList.get(size - 1) : 
			modificationList.get(p_index);
	}
}
