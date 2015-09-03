package structureEntities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class PackageEntity implements Iterable<ClassEntity>, Iterator<ClassEntity> {

	LinkedList<ClassEntity> classes; 

	// iterator related
	int index;
	
	public PackageEntity() {
		classes = new LinkedList<ClassEntity>();
		index = 0;
	}
	
	public void add(ClassEntity classEntity) {
		classes.add(classEntity);
	}
	
	public void setToFirst() {
		index = 0;
	}
	
	/**
	 * Sets classes to the last element. If list is empty, 0 is returned.
	 */
	public void setToLast() {
		index = classes.size() == 0 ? 0 : classes.size()-1;
	}
	
	/**
	 * Returns changes stored in changesList. If null, null is returned.
	 * @return
	 */
	public ClassEntity getClassEntity() {
		return classes != null ? classes.get(index) : null;
	}
	
	public ClassEntity getClassEntity(int p_index) {
		return classes.get(p_index);
	}
	
	public int size() {
		return classes.size();
	}
	
	/** 
	 * Checks if the contained list of Change objects is empty
	 * @return
	 */
	public boolean isEmpty() {
		if (index >= size() || index < 0) {
			throw new NoSuchElementException();
		}
		return classes.isEmpty();
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
	public Iterator<ClassEntity> iterator() {
		return this;
	}

	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ClassEntity next() {
		if (index == classes.size()) {
			throw new NoSuchElementException();
		}
		return classes.get(index++);
	}

	public ClassEntity previous() {
		if (index < 0) {
			throw new NoSuchElementException();
		}
		return classes.get(index--);
	}
	
	public void clear() {
		classes.clear();
	}
	
}
