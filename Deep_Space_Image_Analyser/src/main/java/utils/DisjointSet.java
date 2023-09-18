package utils;

public class DisjointSet {

    public static int findRoot(int[] list, int index){
        return isRoot(list, index) ? index : findRoot(list, list[index]);
    }

    public static boolean isRoot(int[] list, int index){
        return list[index] == index;
    }

    public static void union(int[] list, int index1, int index2){
        list[findRoot(list, index1)] = findRoot(list, index2);
    }

    public int numberOfRoots(int[] list){
        int[] countedRoots = new int[list.length];
        int count = 0;

        for(int i : list){
            if(i != -1 && isRoot(list, i) && countedRoots[i] == 0){
                countedRoots[findRoot(list, i)] = 1;
            }
        }

        for(int i : countedRoots){
            if(i == 1){
                count++;
            }
        }
        return count;
    }
}
