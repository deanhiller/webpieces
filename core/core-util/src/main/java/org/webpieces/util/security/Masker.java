package org.webpieces.util.security;

public class Masker {

    /**
     * Simple method to replace characters (except the last 2 for debug reasons) in a String with Stars to mask sensitive data .
     *
     * @param data sensitive data to mask
     */
    public String maskSensitiveData(String data) {

        if(data == null) {
            return null;
        }

        int lengthOfPassword = data.length();

        if(lengthOfPassword < 3){
            return "****";
        }

        StringBuilder stringBuilder = new StringBuilder("****");
        stringBuilder.append(data.substring(Math.max(0, data.length() - 2)));

        return stringBuilder.toString();
    }
}
