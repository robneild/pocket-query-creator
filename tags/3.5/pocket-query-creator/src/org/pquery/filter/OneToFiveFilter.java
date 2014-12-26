package org.pquery.filter;

import junit.framework.Assert;

public class OneToFiveFilter {

    public int value;
    public boolean up;

    public OneToFiveFilter() {
        value = 5;
        up = false;
    }
    
    public OneToFiveFilter(String s) {
        if (s.equals("1")) {
            value = 1;
            up = false;
        } else if (s.equals("5")) {
            value = 5;
            up = true;
        } else if (s.equals("All")) {
            value = 1;
            up = true;
        } else {
            String[] a = s.split(" - ");
            if (a == null)
                Assert.fail();

            int v1 = Integer.parseInt(a[0]);
            int v2 = Integer.parseInt(a[1]);
            
            if (v1==1) {
                up = false;
                value = v2;
            }
            else if (v2 == 5) {
                up = true;
                value  = v1;
            }
            else
                Assert.fail();
            
        }
    }

    @Override
    public String toString() {
        if (value == 5 && up)
            return "5";
        if (value == 1 && !up)
            return "1";

        if ((value == 1 && up) || (value == 5 && !up))
            return "All";

        if (up)
            return "" + value + " - 5";
        return "1 - " + value;
    }

    public boolean isAll() {
        if (value == 1 && up || value == 5 && !up)
            return true;
        return false;
    }
}
