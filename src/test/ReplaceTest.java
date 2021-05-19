package test;

import java.io.File;

public class ReplaceTest {

    public static void main(String[] args) {
        File file = new File(args[0]);
        File[] fileArray = file.listFiles();
        System.out.println(fileArray.length);
    }

}
