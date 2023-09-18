package utils;

public class Utils {
    public static int findXCord(int i, int width){
        return i % width;
    }

    public static int findYCord(int i, int width){
        return i / width;
    }

    public static int corToIndex(int x, int y, int width){ return width*y+x;}
}
