
package project_4;

import static java.lang.Math.abs;
import java.math.BigInteger;
import java.util.*;

/* Author: Christian Vincent
   COP3530 - Peyman Faizian
   Last edited: 4-14-19
*/

public class CuckooHashMap<K,V> {
    
Entry[] table1;
Entry[] table2;
Entry[] iterableTable1; 
Entry[] iterableTable2;
int prime;
int cap;
int numEntries = 0;
int numOfTries = 0;
int oldCap;
Iterable<Entry<K,V>> tables;

//*******constructors*********
public CuckooHashMap() {
    this(4);
}

public CuckooHashMap(int cap) {
    
    if(cap % 2 != 0)
        cap = cap + 1;
    
    this.cap = cap;
    table1 = new Entry[cap / 2];
    table2 = new Entry[cap / 2];
    prime = 10007;
    
}

public CuckooHashMap(int cap, int prime) {
    
    if(cap % 2 != 0)
        cap = cap + 1;
    
    this.cap = cap;
    table1 = new Entry[cap / 2];
    table2 = new Entry[cap / 2];
    this.prime = prime;
    
}
//*********end of constructors*********

public float loadFactor() {  
    return ((float)numEntries / cap);
}

public int size() {
   return numEntries; 
}

public int capacity() {
    return cap;
}

public V put(K key, V value) {
    
   checkLoadFactorMax();
    
    int index = h1(key);
    numOfTries = 0;
    
    //Check for existing key in the map
    int index1 = h1(key);
    int index2 = h2(key);
    Entry<K,V> existingEntry1 = table1[index1];
    Entry<K,V> existingEntry2 = table2[index2];
    
    if(existingEntry1 != null && compare(existingEntry1.getKey(), key))
    {
       V oldValue = existingEntry1.getValue();
       existingEntry1.setValue(value);
       return oldValue;
    }
    if(existingEntry2 != null && compare(existingEntry2.getKey(), key))
    {
        V oldValue = existingEntry2.getValue();
        existingEntry2.setValue(value);
        return oldValue;
    }
    
    //Key doesn't exist; hash new entry into map
    Entry<K,V> entryToInsert = new Entry<>(key, value);
    while(entryToInsert != null)
    {
        //******table1******
        index = h1(entryToInsert.getKey());
        Entry<K,V> existingEntry = table1[index];
        if(existingEntry == null)
        {
            table1[index] = entryToInsert;
            numEntries++;
            return null;
        }
        //There's an existing entry at table1[index]. Place entry to be inserted 
        //at table1[index] and rehash the replaced entry to table2
        table1[index] = entryToInsert;
        entryToInsert = existingEntry;
        
        //******table2******
        index = h2(entryToInsert.getKey());
        existingEntry = table2[index];
        if(existingEntry == null)
        {
            table2[index] = entryToInsert;
            numEntries++;
            return null;
        }
        //There's an existing entry at table2[index]. Place entry to be inserted
        //at table2[index] and rehash the replaced entry to table1
        table2[index] = entryToInsert;
        entryToInsert = existingEntry;
        
        numOfTries++;
        
        if(numOfTries > cap)
            rehash();
    }
     
    return null;
}

public V get(K key) {
    
    int index = h1(key);
    Entry<K,V> existingEntry = table1[index];
    if(existingEntry != null && compare(existingEntry.getKey(), key))
        return existingEntry.getValue();
    
    index = h2(key);
    existingEntry = table2[index];
    if(existingEntry != null && compare(existingEntry.getKey(), key))
        return existingEntry.getValue();
        
    return null;
    
}

public V remove(K key) {
    
    Entry<K,V> existingEntry;
    V valueFound;
    for(int i = 0; i < capacity() / 2; i++) {
        existingEntry = table1[i];
        if(existingEntry != null && compare(existingEntry.getKey(), key))
        {
            valueFound = existingEntry.getValue();
            table1[i] = null;
            numEntries--;
            return valueFound;
        }
        existingEntry = table2[i];
        if(existingEntry != null && compare(existingEntry.getKey(), key))
        {
            valueFound = existingEntry.getValue();
            table2[i] = null;
            numEntries--;
            return valueFound;
        }
    }
    
    checkLoadFactorMin();
    
   return null; 
}

public Iterable<Entry<K,V>> entrySet() {
   iterableTable1 = table1.clone();
   iterableTable2 = table2.clone();
   oldCap = cap;
   return new EntryIterable();
}

public Iterable<K> keySet() {
    iterableTable1 = table1.clone();
    iterableTable2 = table2.clone();
    oldCap = cap;
    return new KeyIterable();
}

public Iterable<V> valueSet() {
    iterableTable1 = table1.clone();
    iterableTable2 = table2.clone();
    oldCap = cap;
    return new ValueIterable();
}


//******private section********
private void rehash() {
    
    BigInteger nextPrime = new BigInteger(String.valueOf(prime));
    prime = Integer.parseInt(nextPrime.nextProbablePrime().toString());
    
    //rehash
    for (Entry<K,V> e : tables) {
        if(e != null) {
            put(e.getKey(), e.getValue());
            numEntries--;                     //used to counter numEntries++ in put()
        }                                     //function
    }
    
}

private void resize(int newCap) {
 
    tables = this.entrySet();
    table1 = new Entry[newCap / 2];
    table2 = new Entry[newCap / 2];
    cap = newCap;
    rehash();
    
}

private int h1(K key) {
    return (abs(key.hashCode()) % prime) % (cap / 2); 
}

private int h2(K key) {
    return ((abs(key.hashCode()) / prime) % prime) % (cap / 2);
}

private void checkLoadFactorMax() {
    if (loadFactor() > 0.5) 
        resize(cap * 2); 
}

private void checkLoadFactorMin() {
    if (loadFactor() < 0.25 && capacity() > 4)
        resize(cap / 2);
}


private class EntryIterator implements Iterator<Entry<K,V>> {
    
    private int i = 0;
    private int j = 0;
    private int k = 0;
    private int l = 0;
    private int size = size();
    private List<Entry<K,V>> iterableTables = createList();
    
    private List<Entry<K,V>> createList() {
        
        List<Entry<K,V>> tablesArray = new ArrayList<>();
        List<Entry<K,V>> list1 = new ArrayList<>();
        List<Entry<K,V>> list2 = new ArrayList<>();
        
        //Create new lists from table1 and table2
        for(int i = 0; i < oldCap / 2; i++)
            list1.add(iterableTable1[i]);
        
        for(int i = 0; i < oldCap / 2; i++)
            list2.add(iterableTable2[i]);
        
        //Remove empty elements from list1 and list2
        list1.removeAll(Collections.singleton(null));
        list2.removeAll(Collections.singleton(null));
        
        //Combine list1 and list2 alternatively
        while(i < size) {
            
            if(k < list1.size())
            {
               tablesArray.add(list1.get(k++)); 
               i++; 
            }
            if(l < list2.size()) 
            {
                tablesArray.add(list2.get(l++));
                i++;
            }
            
        }
            
       return tablesArray; 
    }
   
    @Override
    public boolean hasNext() {
        return j < iterableTables.size();
    }
    
    @Override
    public Entry<K,V> next() {
      
        if(j > iterableTables.size())
            throw new NoSuchElementException();
        
       return iterableTables.get(j++);
        
    }
    
}

private class KeyIterator implements Iterator<K> {
    private Iterator<Entry<K,V>> entries = entrySet().iterator();
    public boolean hasNext() {
            return entries.hasNext();
    }
    public K next() {
        return entries.next().getKey();
    }
    
}

private class ValueIterator implements Iterator<V> {
    private Iterator<Entry<K,V>> entries = entrySet().iterator();
    public boolean hasNext() {
        return entries.hasNext();
    }
    public V next() {
        return entries.next().getValue();
    }
    
}

private class EntryIterable implements Iterable<Entry<K,V>>{
        @Override
        public Iterator<Entry<K,V>> iterator() {
            return new EntryIterator();
        }
}

private class KeyIterable implements Iterable<K> {
    @Override
    public Iterator<K> iterator() {
        return new KeyIterator();
    }
}

private class ValueIterable implements Iterable<V> {
    public Iterator<V> iterator() {
        return new ValueIterator();
    }
}

private boolean compare(K key1, K key2) {
   if(key1 == null && key2 == null)
       return true;
   else if(key1 == null || key2 == null)
       return false;
   else 
       return key1.equals(key2);

}

}