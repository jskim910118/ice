package org.eclipse.ice.datastructures.form.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.ice.datastructures.form.TreeComposite;

/**
 * This class provides a base for implementing traversals of
 * {@link TreeComposite} instances using Java's {@link Iterator} interface. The
 * purpose of this class and any sub-classes is that iterating over a
 * TreeComposite should require something like the following code:
 * 
 * <pre>
 * <code>
 * TreeComposite root;
 * // Set up your tree here...
 * 
 * Iterator<TreeComposite> iterator = new TreeCompositeIterator(root);
 * while (iterator.hasNext()) {
 *     TreeComposite child = iterator.next();
 * 
 *     // Do something with the child tree here...
 * }
 * </code>
 * </pre>
 * 
 * Sub-classes should implement an iterative tree traversal algorithm instead of
 * a recursive one.
 * 
 * @author Jordan H. Deyton
 * 
 */
public abstract class AbstractTreeCompositeIterator implements
		Iterator<TreeComposite> {

	/**
	 * The root TreeComposite that is the starting point for this iterator.
	 */
	protected final TreeComposite root;

	/**
	 * The default constructor.
	 * 
	 * @param root
	 *            The root TreeComposite that is the starting point for this
	 *            iterator.
	 */
	public AbstractTreeCompositeIterator(TreeComposite root) {
		// begin-user-code

		// Set the root node of the tree.
		if (root != null) {
			this.root = root;
		}
		// If the argument is null, we must throw an IllegalArgumentException.
		else {
			// Set the root TreeComposite so that this instance is not in an
			// incomplete state.
			this.root = new TreeComposite();
			throw new IllegalArgumentException("TreeCompositeIterator error: "
					+ "Root cannot be null.");
		}

		return;
		// end-user-code
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public abstract boolean hasNext();

	/**
	 * Sub-classes should <b>override this method and call it via
	 * <code>super.next()</code></b>. If {@link #hasNext()} is false, this
	 * method throws a {@link NoSuchElementException} as specified in the
	 * {@link Iterator} API.
	 * 
	 * @see java.util.Iterator#next()
	 */
	public TreeComposite next() {
		// begin-user-code

		// Set the default return value.
		TreeComposite next = null;

		// Throw a NoSuchElementException if there is no element left to
		// traverse.
		if (!hasNext()) {
			throw new NoSuchElementException("TreeCompositeIterator error: "
					+ "No elements remaining in iterative traversal.");
		}

		return next;
		// end-user-code
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		// begin-user-code

		// TODO We may or may not want to implement this.
		throw new UnsupportedOperationException("TreeCompositeIterator error: "
				+ "Removing elements is currently not supported.");

		// end-user-code
	}

}