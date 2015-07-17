package org.dbwiki.data.security;

public class Pair<L, R> {

	private final L left;
	private final R right;
	
	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		return left.hashCode() ^ right.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Pair)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Pair pairObject = (Pair) object;
		return this.left.equals(pairObject.getLeft()) && this.right.equals(pairObject.getRight());
	}

}
