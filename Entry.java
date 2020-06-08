
package project_4;


public class Entry<K, V> {
    
    private K k;
    private V v;
    
    public Entry(K key, V value) {
        k = key;
        v = value;
    }
    
    public K getKey() {
        return k;
    }
    
    public V getValue() {
        return v;
    }
    
    protected void setKey(K key) {
        k = key;
    }
    
    protected V setValue(V value) {
        V old = v;
        v = value;
        return old;
    }
}
