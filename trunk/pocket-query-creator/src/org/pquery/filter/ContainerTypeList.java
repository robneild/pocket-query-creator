package org.pquery.filter;

import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.Assert;


import android.content.res.Resources;

public class ContainerTypeList implements Iterable<ContainerType> {

    private LinkedList<ContainerType> inner = new LinkedList<ContainerType>();
    
    public ContainerTypeList() {
    }

    public ContainerTypeList(String s) {
        Assert.assertNotNull(s);
        
        String[] cacheList = s.split(",");
        
        for (String cache : cacheList) {
            if (cache.length()>0)
                inner.add(ContainerType.valueOf(cache));
        }
        
        if (inner.size()==0) {
            setAll();
        }
    }
    
    public ContainerTypeList(boolean[] selection) {
        
        Assert.assertEquals(ContainerType.values().length, selection.length);
        
        for (int i=0; i<selection.length; i++) {
            
            if (selection[i]) {
                inner.add(ContainerType.values()[i]);
            }
        }
        
        if (ContainerType.values().length == inner.size())
            inner.clear();
    }

    
    public String toString() {
        String ret = "";
        for(ContainerType cache : inner) {
            ret += cache.toString() + ",";
        }
        return ret;
    }

    public String toLocalisedString(Resources res) {
        StringBuffer ret = new StringBuffer();
        
        for (ContainerType container : inner) {
            ret.append(res.getString(container.getResourceId()));
            ret.append(", ");
        }
        // Knock off ending ', '
        if (ret.length()>0)
            ret.setLength(ret.length() - 2) ;

        return ret.toString();
    }
    
    public boolean add(ContainerType container) {
        return inner.add(container);
    }
    
    public void clear() {
        inner.clear();
    }
    
    public void setAll() {
        inner.clear();
    }
    
    public boolean isAll() {
        if (inner.size() == 0)
            return true;
        return false;
    }

    @Override
    public Iterator <ContainerType> iterator() {
        return inner.iterator();
    }
    
    public boolean[] getAsBooleanArray() {
        boolean[] ret = new boolean[ContainerType.values().length];
        
        if (isAll()) {
            for (int i=0; i<ret.length; i++) {
                ret[i] = true;
            }
            return ret;
        }
        
        for (int i=0; i<ContainerType.values().length; i++) {
            if (inner.contains(ContainerType.values()[i]))
                ret[i] = true;
        }
                    
        return ret;
    }
}
