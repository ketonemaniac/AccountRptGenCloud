package net.ketone.accrptgen.app.util;

public class PasswordUtils {

    private static final String SEED = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String generatePassword(int len) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < len; i++) {
            int pos = (int) (Math.random() * 10000) % SEED.length();
            sb.append(SEED.substring(pos, pos+1));
        }
        return sb.toString();
    }

//    public static void main(String [] args) {
//        for(int j = 0; j < 100; j++) {
//            System.out.println(generatePassword(8));
//        }
//    }
}
