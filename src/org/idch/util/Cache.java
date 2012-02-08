/* Created on       
 * Last Modified on $Date: 2008-07-17 19:08:01 $
 * $Revision: 1.1 $
 * $Log: Cache.java,v $
 * Revision 1.1  2008-07-17 19:08:01  neal
 * Reattached NADL Project to a CVS Repository. This time the HTML, JS, and other webcomponents are being backed up as well.
 *
 * Revision 1.3  2008-07-17 18:52:56  neal
 * Last change before migrating CVS repository to include more content.
 *
 * Complete - task 8: Implement lougout JavaScript function
 *
 * Incomplete - task 33: Implment CIBO website for registring new users
 *
 * Complete - task 25: List user permissions
 *
 * Complete - task 7: Permissions Javascript
 *
 * Revision 1.2  2008-04-11 20:39:38  neal
 * minor modifications to remove warnings.
 *
 * Revision 1.1  2007-11-08 15:39:17  neal
 * Creating a general project to provide a consistent codebase for NADL. This is being expanded to include most of the components from the old CSDLCommon and CSDLWeb packages, as I reorganize the package structure and improve those components.
 *
 * 
 * Copyright TEES Center for the Study of Digital Libraries (CSDL),
 *           Neal Audenaert
 * 
 * THIS CODE IS ADAPTED FROM EXAMPLES PROVIDED BY GEORGE REESE
 * 
 * ALL RIGHTS RESERVED. PERMISSION TO USE THIS SOFTWARE MAY BE GRANTED 
 * TO INDIVIDUALS OR ORGANIZATIONS ON A CASE BY CASE BASIS. FOR MORE 
 * INFORMATION PLEASE CONTACT THE DIRECTOR OF THE CSDL. IN THE EVENT 
 * THAT SUCH PERMISSION IS GIVEN IT SHOULD BE UNDERSTOOD THAT THIS 
 * SOFTWARE IS PROVIDED ON AN AS IS BASIS. THIS CODE HAS BEEN DEVELOPED 
 * FOR USE WITHIN A PARTICULAR RESEARCH PROJECT AND NO CLAIM IS MADE AS 
 * TO IS CORRECTNESS, PERFORMANCE, OR SUITABILITY FOR ANY USE.
 */
package org.idch.util;

// in his book Java Database Best Practices. Adapted by Neal
// Audenaert

// Java imports
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * <p class="text">
 * Provides a cache of objects that will expire its contents as those
 * contents fail to be used. This cache uses
 * <span class="code">java.lang.ref.SoftReference</span> to guarantee
 * that cached items will be removed when they have not been
 * referenced for a long time. A maximum size can be set for the cache so that
 * the cache will drop objects to ensure that it does not exceed the maximum
 * specified size. The cache will drop the least recently accessed items.</p>
 * 
 * <p class="text">
 * The cache class can be activated or deactivated by a set of static methods. If 
 * caching is deactivated, all boolean queries will return false and all get 
 * operations will ruturn null. The cache() operation will still place items in 
 * the cache, but these items cannot be retrieved directly. This is intended 
 * largely for testing purposes.
 * </p>
 *  
 * <p class="text">
 * This class is not synchronized and therefore should be synchronized
 * by the application for multi-threaded use.</p>
 * 
 * @modified $Date: 2008-07-17 19:08:01 $
 * @version $Revision: 1.1 $
 * @author George Reese/george.reese@jwt.com
 */
public class Cache<K, V> implements Collection<V>, Serializable {
    private static final long serialVersionUID = -8190057006531607218L;
    private static final Logger LOGGER = Logger.getLogger(Cache.class);

    /**
     * Indicates whether or not caching is active or not. If 
     * caching is deactivated, all boolean queries will return false
     * and all get operations will ruturn null. The cache() operation
     * will still place items in the cache, but these items cannot
     * be retrieved directly. This is intended largely for testing
     * purposes.
     */
    private static boolean cacheActive = true;
    
    /** Activates caching actions. */
    public static void activate() {cacheActive = true;}
    
    /** Deactivates caching actions. */
    public static void deactivate() {cacheActive = false;}
    
    /** Indicates whether or not caching actions are activated. */
    public static boolean isActive() {return cacheActive;}
    
    /** A hash map indexing references by unique keys. */
    private Map<K, SoftReference<V>> cache = 
        new HashMap<K, SoftReference<V>>();
    
    private SortedMap<K, Long> timestamps = new TreeMap<K, Long>();

    private int max = -1;
    
    private String name = "unknown";
    
    private CacheStatistics stats = new CacheStatistics();

    /** Constructs a new empty cache. */
    public Cache() { super(); }
    
    public Cache(String name, int maxSize) {
        super();
        this.name = name;
        this.max = maxSize;
    }

    /** Returns statistics about the cache performance. */
    public CacheStatistics getStatistics() {
        return stats;
    }
    
    /**
     * Caches the specified object identified by the specified key, overwriting
     * any previously existing value.
     *
     * @param key a unique key for this object
     * @param val the object to be cached
     */
    public void cache(K key, V val) {
        if ((this.max > 0) && (cache.size() >= this.max)){
            Object last = null;
            while (cache.size() >= this.max) { 
                last = this.getLeastRecentlyTouched();
                
                if (last == null) {
                    // there is a programming error if last is null
                    assert false : "no last touched value"; 
                    break;   // handle "gracefully"
                } else this.release(last);    
            }
        }
        
        stats.cache();
        long time = System.currentTimeMillis();
        cache.put(key, new SoftReference<V>(val));
        timestamps.put(key, time);
    }
    
    /**
     * Provides the cached object identified by the specified key. This
     * method will return <span class="code">null</span> if the
     * specified object is not in the cache.
     * 
     * @param key the unique identifier of the desired object
     * @return the cached object or null
     */
    public V get(K key) {
        // NOTE this modifies the cache hash table. While this should not 
        //      affect the performance of this collection (iterators do not
        //      opperate directly over the underlying cache hash table) it
        //      does affect any iterations over the cache based on iterators
        //      returned by cache.iterator(). When used within this class
        //      
        if (!cacheActive) return null;

        V ob = null;
        SoftReference<V> ref = cache.get(key);
        if (ref != null) {
            ob = ref.get();
            if (ob == null) this.release(key);
            else this.touch(key);
        } else if (timestamps.containsKey(key)) timestamps.remove(key);
        
        if (ob == null) {
            stats.miss();
            LOGGER.debug("cache miss [" + name + "]: " + key);
        } else {
            stats.hit();
            LOGGER.debug("cache hit  [" + name + "]: " + key);
        }
        
        return ob;
    }
    
    /** 
     * Returns the last time the object specified by this key was accessed. 
     * Note that this may return a timestamp for object which are no longer in
     * the cache. 
     */
    public long getTimestamp(Object key) { return timestamps.get(key); }
    
    /**
     * Releases the specified object from the cache.
     * @param key the unique identified for the item to release
     */
    public synchronized void release(Object key) { 
        this.cache.remove(key);
        this.timestamps.remove(key);
        this.stats.eject();
    }
    
    /** Returns the key of the object that was accessed least reacently. */
    private Object getLeastRecentlyTouched() {
        long mintime = Long.MAX_VALUE;
        Object min   = null;
        
        // since the this.get(key) method may modify the underlying cache
        // hash table (removing old entries), it is not safe to simply 
        // iterate over the hash tables keys, so we create a new set of keys  
        // and iterate over those.
        Set<K> keys = new HashSet<K>(this.cache.keySet());
        for (K key : keys) {
//            if (this.get(key) == null) continue; // removes & ignores old entries 
            
            long time = this.timestamps.get(key);
            long current = System.currentTimeMillis();
            
            assert time != 0 : "Invalid time recorded";
            assert time <= current : "Future time found: " + 
                                     time + " > " + current;
            
            if (mintime > time) {
                mintime = time;
                min = key;
            }
        }
        
        return min;
    }
    
    /** 
     * Marks the object identified by the specified key as having been 
     * accessed at the current time. 
     */
    private long touch(K key) {
        assert cache.containsKey(key) && timestamps.containsKey(key) :
            "Attemped to touch invalid key";
        
        long time = System.currentTimeMillis();
        timestamps.put(key, time);
        return time;
    }

    /** Clears the entire cache.  */
    public void clear() { cache.clear(); timestamps.clear(); }
    
    /**
     * Checks the specified object against the cache and verifies that it
     * is in the cache. This method will return
     * <span class="keyword">false</span> if the object was once in the cache
     * but has expired due to inactivity.
     *
     * @param ob the object to check for in the cache
     * @return true if the object is in the cache
     */
    public boolean contains(Object ob) {
        if (!cacheActive) return false;

        for (SoftReference<V> ref : cache.values()) {
            V item = ref.get();
            if (item != null && ob.equals(item)) return true;
        }
        return false;
    }
    
    /**
     * Checks the passed in collection and determines if all elements
     * of that collection are contained within this cache. Care should
     * be taken in reading too much into a failure. If one of the elements
     * was once in this cache but has expired due to inactivity, this
     * method will return false.
     * @param coll the collection to test
     * @return true if all elements of the tested collection are in the cache
     */
    public boolean containsAll(Collection<?> coll) {
        if (!cacheActive) return false;
        
        for (Object obj : coll) { 
            if (!this.contains(obj)) return false;
        }
        
        return true;
    }
    
    /**
     * Checks if an object with the specified key is in the cache.
     * 
     * @param key the object's identifier
     * @return true if the object is in the cache
     */
    public boolean containsKey(K key) {
        if (!cacheActive) return false;
        
        SoftReference<V> ref = cache.get(key);
        if (ref == null) return false;
        else if (ref.get() == null) {
            this.release(key);
            return false;
        } else return true;
    }

    /** 
     * Returns true if the cache is empty. Note that this ignores the fact that 
     * soft references may have been lost. That is, this might return false  
     * while <code>toArray()</code> returns a zero length array. 
     */
    public boolean isEmpty() { return cache.isEmpty(); }

    /**
     * Provides all of the valid objects in the cache.
     * This method will not be the snappiest method in the world.
     * 
     * @return all valid objects in the cache
     */
    public Iterator<V> iterator() { return toList().iterator(); }
    
    /** @return the number of elements in the cache */
    public int size() { return toList().size(); }

    /** Returns the cache as an array */
    public Object[] toArray() { return toList().toArray(); }

    /** Returns the cache as an array */
    @SuppressWarnings("unchecked")
    public Object[] toArray(Object[] arr) { return toList().toArray(arr); }

    /** Returns the cache as an array. */
    private ArrayList<V> toList() {
        ArrayList<V> tmp = new ArrayList<V>();

        for (SoftReference<V> ref : this.cache.values()) {
            V ob = ref.get();
            if (ob != null) tmp.add(ob);
        }
        return tmp;
    }

    /**
     * Unsupported.
     *
     * @param ob ignored
     * @return never returns
     * @throws java.lang.UnsupportedOperationException always
     */
    public boolean add(V ob) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Unsupported.
     *
     * @param coll ignored
     * @return never returns
     * @throws java.lang.UnsupportedOperationException always
     */
    public boolean addAll(Collection<? extends V> coll) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Unsupported.
     * @param ob ignored
     * @return never returns
     * @throws java.lang.UnsupportedOperationException always
     */
    public boolean remove(Object ob) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     * @param coll ignored
     * @return never returns
     * @throws java.lang.UnsupportedOperationException always
     */
    public boolean removeAll(Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     * @param coll ignored
     * @return never returns
     * @throws java.lang.UnsupportedOperationException always
     */
    public boolean retainAll(Collection<?> coll) {
        throw new UnsupportedOperationException();
    }
    
    public class CacheStatistics {
        private int hit = 0;
        private int miss = 0;
        private int cached = 0;
        private int ejected = 0;
        
        private void hit() { hit++; }
        private void miss() { miss++; }
        private void cache() { cached++; }
        private void eject() { ejected++; }
        
        public int getHits() { return hit; }
        public int getMisses() { return miss; }
        public int getCacheCount() { return cached; }
        public int getEjectedCount() { return ejected; }
        
        public void reset() {
            hit = 0;
            miss = 0;
            cached = 0;
            ejected = 0;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            
            sb.append("Cache Statistics:  \n")
              .append("   Objects Cached:  ").append(cached).append("\n")
              .append("   Objects Ejected: ").append(ejected).append("\n")
              .append("   Cache Hit:       ").append(hit).append("\n")
              .append("   Cache Miss:      ").append(miss).append("\n")
              .append("   Ratio:           ").append((float)hit/(hit + miss));
            
            return sb.toString();
              
        }
        
        
    }
}
