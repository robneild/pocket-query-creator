package org.pquery;

public enum ContainerType {
    
    SMALL(R.string.container_small), 
    OTHER(R.string.container_other), 
    VIRTUAL(R.string.container_virtual),
    LARGE(R.string.container_large),
    REGULAR(R.string.container_regular),
    MICRO(R.string.container_micro),
    UNKNOWN(R.string.container_unknown);

    
    private int resId;
    
    ContainerType(int resId) {
        this.resId = resId;
    }
    
    public int getResourceId() {
        return resId;
    }
}
