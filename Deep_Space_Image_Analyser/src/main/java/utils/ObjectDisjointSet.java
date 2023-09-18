package utils;

public class ObjectDisjointSet extends DisjointSet {

    int[] disjointSet;
    int length;

    public ObjectDisjointSet(int[] list, int imageHeight){
        disjointSet = list;
        scan(disjointSet, imageHeight);
        length = disjointSet.length;
    }

    public int length() {
        return disjointSet.length;
    }

    private void scan(int[] list, int imageHeight){
        for(int i = 0; i < list.length; i++){
            if(list[i] != -1 && list[i+1] != -1){
                list[i+1] = i;
            }
        }

        for(int i = 0; i<list.length-imageHeight; i++){
            if(list[i] != -1 && list[i+imageHeight] != -1){
                union(list, findRoot(list, i), findRoot(list, i+imageHeight));
            }
        }
    }

    public int index(int i){
        return disjointSet[i];
    }
}
