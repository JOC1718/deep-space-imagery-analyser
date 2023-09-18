package utils;

import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.*;
import java.util.Map.Entry;


public class ObjectImageProcessing{
    Image image;
    int[] objectsCopy;
    int[] objects; //whats pixels for??????
    int width, height, numberPixels;
    int preMin = 0;
    int preMax = 10000;
    double preBrightness;
    LinkedHashMap<Integer, Integer> orderedBySize;
    HashMap<Integer, Integer> objectSizes;

    public ObjectImageProcessing(Image image){
        this.image = image;
        width = (int)image.getWidth();
        height = (int)image.getHeight();
        numberPixels = width*height;
        objectsCopy = new int[numberPixels];
        objects = new int[numberPixels];
    }

    public Image getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public WritableImage convertImageBW(double threshold, int min, int max){
        WritableImage writableImage = new WritableImage(width, height);
        //int[] bwList = new int[numberPixels];
        preBrightness = threshold;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (image.getPixelReader().getColor(x, y).getBrightness() >= threshold) {
                    objects[coordToIndex(x, y)] = coordToIndex(x, y);
                    writableImage.getPixelWriter().setColor(x, y, Color.WHITE);
                } else {
                    objects[coordToIndex(x, y)] = -1;
                    writableImage.getPixelWriter().setColor(x, y, Color.BLACK);
                }
            }
        }

//        objects = createDisjointSet(objects).clone());
//        objectsCopy = objects.clone();
        objects = createDisjointSet(objects);
        objectsCopy = objects.clone();

        return writableImage;
    }

    private int[] createDisjointSet(int[] list){
        for(int i = 0; i < list.length; i++){
            if(list[i] != -1 && list[i+1] != -1){
                list[i+1] = i;
            }
        }

        for(int i = 0; i<list.length-width; i++){
            if(list[i] != -1 && list[i+width] != -1){
                union(findRoot(i), findRoot(i+width));
            }
        }

        objectSizes = findObjectSizes(list);
        return list;
    }

    private HashMap<Integer, Integer> findObjectSizes(int[] list){
        objectSizes = new HashMap<>();

        for(int i : list) {
            if (i != -1)
                objectSizes.put(findRoot(i), (objectSizes.get(findRoot(i))==null ? 1 : objectSizes.get(findRoot(i))+1));
        }

        return objectSizes;
    }

    public WritableImage colourObject(int x, int y){
        WritableImage wb = new WritableImage(width, height);

        System.out.println("IN METHOD");
        System.out.println(coordToIndex(x, y));
        System.out.println(objects[coordToIndex(x, y)]);
        if(objects[coordToIndex(x, y)] != -1){
            System.out.println("IN If");
            Color color = generateRandomColour();
            int root = findRoot(coordToIndex(x, y));
            for(int i = 0; i<objects.length; i++){
                if(objects[i] != -1 && findRoot(i) == root){
                    wb.getPixelWriter().setColor(findXCoord(i), findYCoord(i), color);
                }else if(objects[i] != -1){
                    wb.getPixelWriter().setColor(findXCoord(i), findYCoord(i), Color.WHITE);
                }else {
                    wb.getPixelWriter().setColor(findXCoord(i), findYCoord(i), Color.WHITE);
                }
            }
        } else wb = convertImageBW(preBrightness, preMin, preMax);

        return wb;
    }

    public WritableImage findObjects(int min, int max){
        WritableImage wb = new WritableImage(width, height);

        objects = objectsCopy.clone(); //copy original, use copy

//        if(min < preMin || max < preMax){
//            objects = createDisjointSet(objectsCopy).clone();
//            objectsCopy = objects.clone();
//            System.out.println("TRUE");
//        }

        for(int i = 0; i<objects.length; i++){
            if(objects[i] == -1|| (objectSizes.get(findRoot(objects[i])) < min || objectSizes.get(findRoot(objects[i])) > max)){
                wb.getPixelWriter().setColor(findXCoord(i), findYCoord(i), Color.BLACK);
                if(objects[i] != -1){
                    removeObject(findRoot(objects[i]));
                }
            } else {
                wb.getPixelWriter().setColor(findXCoord(i), findYCoord(i), Color.WHITE);

            }
        }

        preMin = min;
        preMax = max;

        return wb;
    }

    private void removeObject(int root){
        ArrayList<Integer> indexes = new ArrayList<>();

        for(int i = 0; i<objects.length; i++){
            if(objects[i] != -1 && findRoot(objects[i]) == root){
                indexes.add(i);
            }
        }

        for(int i : indexes){
            objects[i] = -1;
        }
    }

    public WritableImage colourObjects(){
        WritableImage wbImage = new WritableImage(width, height);
        LinkedHashMap<Integer, Color> colourMapping = createColourMapping();

        //objectSizes = findObjectSizes();

        for(int i = 0 ; i < objects.length; i++){
            if(objects[i]!=-1 && colourMapping.containsKey(findRoot(objects[i]))){
                wbImage.getPixelWriter().setColor(findXCoord(i), findYCoord(i), colourMapping.get(objects[findRoot(i)]));
            } else {
                wbImage.getPixelWriter().setColor(findXCoord(i), findYCoord(i), Color.BLACK);
            }
        }

        return wbImage;
    }

    private LinkedHashMap<Integer, Color> createColourMapping(){
        LinkedHashMap<Integer, Color> colourMapping = new LinkedHashMap<>();

        for(int index : objects){
            if(index != -1 && !colourMapping.containsKey(findRoot(index))){
                colourMapping.put(index, generateRandomColour());
            }
        }

        return colourMapping;
    }

    private Color generateRandomColour(){
        Random random = new Random();
        return Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble());
    }

    public Object[] identifyObjects(int offSetX, int offsetY){
        HashMap<Integer, int[]> points = findPoints(); //HashMap contains index of first coordinate and last coordinate
        HashMap<Integer, int[]> midpoints = findMidPoints(points); //HashMap contains X coordinate and Y coordinate of midpoints
        HashMap<Integer, float[]> gasAnalysis = gaseousAnalysis();
        ArrayList<Circle> circles = new ArrayList<>();
        ArrayList<Text> labels = new ArrayList<>();
        orderedBySize = sortObjects();

        for(int[] i : points.values()){
            int xC = offSetX+midpoints.get(findRoot(i[0]))[0];
            int yC = offsetY+midpoints.get(findRoot(i[0]))[1];
            int r = estimateRadius(points, i);
            int xT = (xC+(lengthOfLine(i[0], width*yC+xC)-r));
            int yT = yC+(lengthOfLine(i[0], width*yC+xC)-r);

            Circle circle = createCircle(xC, yC,r+5);
            Tooltip.install(circle, new Tooltip("Object: "+orderedBySize.get(findRoot(i[0]))+"\nEstimated Size(pixel units): "+objectSizes.get(findRoot(i[0]))+
                    "\nSulphur Content: "+gasAnalysis.get(findRoot(i[0]))[0]+"\nHydrogen Content: "+gasAnalysis.get(findRoot(i[0]))[1]+
                    "\nOxygen Content: "+gasAnalysis.get(findRoot(i[0]))[2]));
            circles.add(circle);

            labels.add(createLabel(xT, yT, orderedBySize.get(findRoot(i[0])).toString()+"."));
        }

        Object[] info = new Object[2];
        info[0] = circles;
        info[1] = labels;

        return info;
    }

    private LinkedHashMap<Integer, Integer> sortObjects(){
        LinkedHashMap<Integer, Integer> orderBySize = new LinkedHashMap<>();
        //HashMap<Integer, Integer> objectSizes = findObjectSizes();
        ArrayList<Entry<Integer, Integer>> entrySets = new ArrayList<>(objectSizes.entrySet());

        entrySets.sort((a, b) ->
                ((Entry<Integer, Integer>) a).getValue() - ((Entry<Integer, Integer>) b).getValue());
        Collections.reverse(entrySets);

        for(int i = 0; i<entrySets.size(); i++){
            orderBySize.put(entrySets.get(i).getKey(), i+1);
        }

        return orderBySize;
    }

    private HashMap<Integer, int[]> findPoints(){
        HashMap<Integer, int[]> points = new HashMap<>();

        for(int i = 0; i < objects.length; i++){
            if(objects[i] != -1){
                if(!points.containsKey(findRoot(objects[i]))){
                    int[] coords = new int[2];
                    coords[0] = coords[1] = i;
                    points.put(findRoot(objects[i]), coords);
                } else{
                    int[] coords = points.get(findRoot(objects[i]));
                    if(i >= coords[1] && objects[i+1] == -1){
                        coords[1] = i;
                        points.replace(findRoot(objects[i]), coords);
                    }
                }
            }
        }

        return points;
    }

    public HashMap<Integer, float[]> gaseousAnalysis(){
        HashMap<Integer, float[]> gasAnalysis = new HashMap<>();

        for(int i = 0; i<objects.length; i++){
            if(objects[i] != -1){
                Color color = image.getPixelReader().getColor(findXCoord(i), findYCoord(i));
                if(!gasAnalysis.containsKey(findRoot(objects[i]))) {
                    float[] colors = new float[3];
                    colors[0] += color.getRed();
                    colors[1] += color.getGreen();
                    colors[2] += color.getBlue();
                    gasAnalysis.put(findRoot(objects[i]), colors);
                } else {
                    float[] colors = gasAnalysis.get(findRoot(objects[i]));
                    colors[0] += color.getRed();
                    colors[1] += color.getGreen();
                    colors[2] += color.getBlue();
                    gasAnalysis.put(findRoot(objects[i]), colors);
                }
            }
        }

        for(Entry<Integer, float[]> i : gasAnalysis.entrySet()){
            if(isRoot(i.getKey())){
                float[] colors = gasAnalysis.get(i.getKey());
                int size = objectSizes.get(i.getKey());
                colors[0] += colors[0]/size;
                colors[1] += colors[1]/size;
                colors[2] += colors[2]/size;
                gasAnalysis.put(i.getKey(), colors);
            }
        }

        return gasAnalysis;

    }

    private HashMap<Integer, int[]> findMidPoints(HashMap<Integer, int[]> points){
        HashMap<Integer, int[]> midpoints = new HashMap<>();

        for(int[] i : points.values()){
            if(i[0] == i[1]){
                int[] cords = new int[2];
                cords[0] = findXCoord(i[0]);
                cords[1] = findYCoord(i[0]);
                midpoints.put(i[0], cords);
            } else {
                int[] cords = new int[2];
                cords[0] = calculateMidpoint(findXCoord(i[0]), findXCoord(i[1]));
                cords[1] = calculateMidpoint(findYCoord(i[0]), findYCoord(i[1]));
                midpoints.put(findRoot(i[0]), cords);
            }
        }

        return midpoints;
    }

    public int estimateRadius(HashMap<Integer, int[]> points, int[] indexes){
        int x1  = findXCoord(indexes[0]);
        int y1 = findYCoord(indexes[0]);
        int x2  = findXCoord(indexes[1]);
        int y2 = findYCoord(indexes[1]);

        double x = Math.pow((x1-x2), 2);
        double y = Math.pow((y1-y2), 2);

        return ((int)Math.sqrt(x+y)/2);
    }

    private Text createLabel(int x, int y, String text){
        Text t = new Text(x, y, text);
        t.setFill(Color.WHITE);
        t.setUnderline(true);
        return t;
    }

    private Circle createCircle(int x, int y, int r){
        Circle circle = new Circle(x, y, r+5);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.BLUE);

        return circle;
    }

    private void union(int index1, int index2){
        objects[index1] = objects[index2];
    }

    private int findRoot2(int index){
        return isRoot(index) ? index : (objects[index] = findRoot(objects[index]));
    }
    private int findRoot(int index){
        if(objects[index]==-1) return -1;
        return isRoot(index) ? index : findRoot(objects[index]);
    }

    private boolean isRoot(int index){
        return objects[index] == index;
    }

    public int findXCoord(int i){
        return i % width;
    }

    public int findYCoord(int i){
        return i / width;
    }

    public int coordToIndex(int x, int y){ return width*y+x;}

    public int calculateMidpoint(int i, int j){
        return (i+j)/2;
    }

    public int lengthOfLine(int p1, int pt2){
        int x1  = findXCoord(p1);
        int y1 = findYCoord(p1);
        int x2  = findXCoord(p1);
        int y2 = findYCoord(p1);

        double x = Math.pow((x1-x2), 2);
        double y = Math.pow((y1-y2), 2);

        return ((int)Math.sqrt(x+y));
    }


}
