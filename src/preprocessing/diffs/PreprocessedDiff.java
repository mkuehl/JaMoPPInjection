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

	@Override
	public boolean hasNext() {
		if (index < modificationList.size()) {
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

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Changes> iterator() {
		return this;
	}
	
	public void clear() {
		modificationList = new LinkedList<Changes>();
		index = 0;
	}
}
