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

        data = data.trim();
        int index = data.indexOf(" ");
        if(index < 0) {
            return parseSecret(data);

        }

        String scheme = data.substring(0, index+1);
        String secret = data.substring(index+1);

        return scheme+" "+parseSecret(secret);
    }

    private static String parseSecret(String data) {

        int lengthOfPassword = data.length();

        StringBuilder stringBuilder;
        if(lengthOfPassword <= 3){
            return "****";
        } else if(lengthOfPassword < 8) {
            stringBuilder = new StringBuilder("****");
            stringBuilder.append(data.substring(Math.max(0, data.length() - 2)));
        } else if(lengthOfPassword > 100) {
            stringBuilder = new StringBuilder("*");
            stringBuilder.append(data.substring(Math.max(0, data.length() - 10)));
        } else {
            stringBuilder = new StringBuilder("**");
            stringBuilder.append(data.substring(Math.max(0, data.length() - 4)));
        }

        return stringBuilder.toString();
    }
}
