package com.xrtb.shared;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.*;

/*
 * Copyright (c) 2017 Pierantonio Cangianiello
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * A thread-safe implementation of a HashMap which entries expires after the
 * specified life time. The life-time can be defined on a per-key basis, or
 * using a default one, that is passed to the constructor. Based on the code
 * by Pierantonio Cangianiello
 * 
 * @author Ben M. Faul
 * @param <K>
 *            the Key type
 * @param <V>
 *            the Value type
 */
public class SelfExpiringHashMap<K, V> implements SelfExpiringMap<K, V> {
	/** The internal map */
	private final Map<K, V> internalMap;
	/** The expiring key */
	private final Map<K, ExpiringKey> expiringKeys;
	/** Holds the keys set to expire. */
	private final DelayQueue<ExpiringKey> delayQueue = new DelayQueue<ExpiringKey>();

	/**
	 * Constructor for the hashmap
	 */
	public SelfExpiringHashMap() {
		internalMap = new ConcurrentHashMap<K, V>();
		expiringKeys = new WeakHashMap();

		ScheduledExecutorService execService = Executors.newScheduledThreadPool(5);
		execService.scheduleAtFixedRate(() -> {
			try {
				cleanup();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, 100L, 100L, TimeUnit.MILLISECONDS);
	}

	/**
	 * Create a hashmap using the default initial capacity.
	 * @param initialCapacity int. How many buckets to load.
	 */
	public SelfExpiringHashMap(int initialCapacity) {
		internalMap = new ConcurrentHashMap<K, V>(initialCapacity);
		expiringKeys = new WeakHashMap(initialCapacity);
	}

	/**
	 * Create a hash with an initial capacity and a loading factor.
	 * @param initialCapacity int. The initial capacity.
	 * @param loadFactor int. The loading factor.
	 */
	public SelfExpiringHashMap(int initialCapacity, float loadFactor) {
		internalMap = new ConcurrentHashMap<K, V>(initialCapacity, loadFactor);
		expiringKeys = new WeakHashMap(initialCapacity, loadFactor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return internalMap.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsKey(Object key) {
		return internalMap.containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsValue(Object value) {
		return internalMap.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return internalMap.get(key);
	}

	/**
	 * {@inheritDoc}private
	 */
	@Override
	public V put(K key, V value) {
		ExpiringKey k = expiringKeys.get(key);
		if (k != null) {
			k.expire();
			cleanup();
		}
		return internalMap.put(key, value); // , maxLifeTimeMillis);
	}
	
	/**
	 * Put a key into the map. If an expiration is set, then leave it. In effect reuse it. If they expiration 
	 * key doesnt exist then there will be no expiration.
	 * @param key K. The key
	 * @param value V. The value. The replacing value.
	 * @return V. The value inserted.
	 */
	public V putNoClearKey(K key, V value) {
		ExpiringKey k = expiringKeys.get(key);
		return internalMap.put(key, value); // , maxLifeTimeMillis);
	}

	/**
	 * Persist the key that was once TTL'ed.
	 * @param key K. The key to persist.
	 */
	public void persist(K key) {
		ExpiringKey k = expiringKeys.get(key);
		if (k == null)
			return;
		expiringKeys.remove(key);
		delayQueue.remove(k);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V put(K key, V value, long lifeTimeMillis) {
		ExpiringKey delayedKey = new ExpiringKey(key, lifeTimeMillis);
		ExpiringKey oldKey = expiringKeys.put((K) key, delayedKey);
		if (oldKey != null) {
			expireKey(oldKey);
			expiringKeys.put(key, delayedKey);
		}
		boolean ok = delayQueue.offer(delayedKey);
		if (!ok)
			System.out.println("FAILED TO MAKE KEY!");
		return internalMap.put(key, value);
	}

	/**
	 * Returns the time to live for this key. If the key doesn't exist or is timed out returns 0.
	 * @param key K. The key value.
	 * @return long. The milliseconds from now it will expire
	 */
	public long ttl(K key) {
		ExpiringKey e = expiringKeys.get(key);
		if (e == null) {
			return 0;
		}
		return e.getDelayMillis();
	}

	/**
	 * Expire a key at a (possibly new) ti,e.
	 * @param key K. The key to expire.
	 * @param ms long. The time in milliseconds from to expire.
	 */
	public void expire(K key, long ms) {
		if (!internalMap.containsKey(key))
			return;

		ExpiringKey delayedKey = new ExpiringKey(key, ms);
		ExpiringKey oldKey = expiringKeys.put(key, delayedKey);
		if (oldKey != null) {
			expireKey(oldKey);
			expiringKeys.put(key, delayedKey);
		}
		delayQueue.offer(delayedKey);
	}
	
	/**
	 * Returns the number of keys in the delayedQueue waiting to expire.
	 * @return int. The number of elements in the queue.
	 */
	public int getDelayedQueueSize() {
		return delayQueue.size();
	}
	
	/**
	 * The expiring key size.
	 * @return int. Returns the number of elements in theexpiring key map.
	 */
	public int getExpringKeysSize() {
		return expiringKeys.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V remove(Object key) {
		V removedValue = internalMap.remove( key);
		expireKey(expiringKeys.remove(key));
		return removedValue;
	}

	/**
	 * Not supported.
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean renewKey(K key) {
		ExpiringKey delayedKey = expiringKeys.get(key);
		if (delayedKey != null) {
			delayedKey.renew();
			return true;
		}
		return false;
	}

	/**
	 * Expire the key no.
	 * @param delayedKey ExpiringKey. The key to expire.
	 */
	private void expireKey(ExpiringKey delayedKey) {
		if (delayedKey != null) {
			delayedKey.expire();
			cleanup();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		delayQueue.clear();
		expiringKeys.clear();
		internalMap.clear();
	}

	@Override
	public Set<K> keySet() {
		return internalMap.keySet();
	}

	/**
	 * Not supported.
	 */
	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	/**
	 * When a key expires it can be pulled from the delayqueue, then you can remove it, and what it references.
	 */
	private  void cleanup() {
		ExpiringKey delayedKey = delayQueue.poll();
		while (delayedKey != null) {
			internalMap.remove(delayedKey.getKey());
			expiringKeys.remove(delayedKey.getKey());
			delayedKey = delayQueue.poll();
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * The expiring key. Implements Delayed so it can be used to trigger map deletions.
	 * @author Ben M. Faul
	 *
	 */
	private class ExpiringKey implements Delayed {

		// The time of creation.
		private long startTime = System.currentTimeMillis();
		// The max time of life
		private final long maxLifeTimeMillis;
		// The key
		private final K key;

		/**
		 * Create an expiring key.
		 * @param key K. The key name.
		 * @param maxLifeTimeMillis long. The max number of ms to love.
		 */
		public ExpiringKey(K key, long maxLifeTimeMillis) {
			this.maxLifeTimeMillis = maxLifeTimeMillis;
			this.key = key;
		} 

		/**
		 * Return the key value of this expiring key
		 * @return K. The expiring key's name
		 */
		public K getKey() {
			return key;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final ExpiringKey other = (ExpiringKey) obj;
			if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
				return false;
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			int hash = 7;
			hash = 31 * hash + (this.key != null ? this.key.hashCode() : 0);
			return hash;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(getDelayMillis(), TimeUnit.MILLISECONDS);
		}

		/**
		 * Returns the time left to live in MS.
		 * @return long. The time left.
		 */
		private long getDelayMillis() {
			long z = (startTime + maxLifeTimeMillis) - System.currentTimeMillis();
			if (z < 0)
				z = 0;
			return z;
		}

		/**Key. Use the form S
		 * The time left to live, caclulated from the start
		 * @return
		 */
		public long ttl() {
			return System.currentTimeMillis() - startTime;
		}

		/**
		 * Renews the key's start time. Resetting the clock.
		 */
		public void renew() {
			startTime = System.currentTimeMillis();
		}

		/**
		 * Expires this key.
		 */
		public void expire() {
			startTime = System.currentTimeMillis() - maxLifeTimeMillis - 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(Delayed that) {
			return Long.compare(this.getDelayMillis(), ((ExpiringKey) that).getDelayMillis());
		}

	}
}