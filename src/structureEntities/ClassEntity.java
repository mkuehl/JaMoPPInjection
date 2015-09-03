package structureEntities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class ClassEntity implements Iterable<MemberEntity>, Iterator<MemberEntity> {

	LinkedList<MemberEntity> members; 

	// iterator related
	int index;
	
	public ClassEntity() {
		members = new LinkedList<MemberEntity>();
		index = 0;
	}
	
	public void add(MemberEntity classEntity) {
		members.add(classEntity);
	}
	
	public void setToFirst() {
		index = 0;
	}
	
	/**
	 * Sets members to the last element. If list is empty, 0 is returned.
	 */
	public void setToLast() {
		index = members.size() == 0 ? 0 : members.size()-1;
	}
	
	/**
	 * Returns changes stored in changesList. If null, null is returned.
	 * @return
	 */
	public MemberEntity getClassEntity() {
		return members != null ? members.get(index) : null;
	}
	
	public MemberEntity getClassEntity(int p_index) {
		return members.get(p_index);
	}
	
	public int size() {
		return members.size();
	}
	
	/** 
	 * Checks if the contained list of Change objects is empty
	 * @return
	 */
	public boolean isEmpty() {
		if (index >= size() || index < 0) {
			throw new NoSuchElementException();
		}
		return members.isEmpty();
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
	public Iterator<MemberEntity> iterator() {
		return this;
	}

	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MemberEntity next() {
		if (index == members.size()) {
			throw new NoSuchElementException();
		}
		return members.get(index++);
	}

	public MemberEntity previous() {
		if (index < 0) {
			throw new NoSuchElementException();
		}
		return members.get(index--);
	}
	
	public void clear() {
		members.clear();
	}

}
