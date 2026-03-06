package com.interview.ai.utils;

import java.util.Collection;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static String parseArrayString(Collection<?> c){
        if(c==null||c.isEmpty()){
            return null;
        }
        StringBuilder stringBuilder=new StringBuilder();
        for(Object o:c){
            stringBuilder.append(o).append(",");
        }
        return stringBuilder.substring(0,stringBuilder.length()-1);
    }
}
