/**
 * 
 */
package org.idch.texts;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.UUID;

/**
 * @author Neal Audenaert
 */
public interface TokenSequence {

	public String getText();
	
	public UUID getWorkUUID();
	
	/** Return the start position (inclusive) in the underlying <tt>Work</tt>'s token stream. */
	abstract int getStart();
    
	/** Return the end position (exclusive) in the underlying <tt>Work</tt>'s token stream. */
    abstract int getEnd();

	/* (non-Javadoc)
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Token o);

	/* (non-Javadoc)
	 * @see java.util.List#get(int)
	 */
	public Token get(int index);

	/* (non-Javadoc)
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty();
	
	/* (non-Javadoc)
	 * @see java.util.List#size()
	 */
	public int size();

	/* (non-Javadoc)
	 * @see java.util.List#iterator()
	 */
	public Iterator<Token> iterator();
	
	public Iterator<Token> iterator(int startAt);

	/* (non-Javadoc)
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<Token> listIterator();
	
	/* (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<Token> listIterator(int index);
	
	/* (non-Javadoc)
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Token token);
	
	/* (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	public TokenSequence subSequence(int fromIndex, int toIndex);

	/* (non-Javadoc)
	 * @see java.util.List#toArray()
	 */
	public Token[] toArray();

	/* (non-Javadoc)
	 * @see java.util.List#toArray(T[])
	 */
	public Token[] toArray(Token[] a);
	

}
