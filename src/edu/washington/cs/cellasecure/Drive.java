package edu.washington.cs.cellasecure;

public class Drive {

    private String mName;
    
    public Drive(String name) {
        mName = name;
    }   

    @Override
    public String toString() {
        return mName;
    }

}
