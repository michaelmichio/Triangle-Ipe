import java.util.ArrayList;
public class Main {
    public static void main(String[] args) {
        if(args.length == 3) {
            ArrayList<Vector2> vectors = new ArrayList<>();
            for(int i = 0; i < args.length; i++) {
                try {
                    String[] strVector = args[i].split(",");
                    vectors.add(new Vector2(Integer.parseInt(strVector[0]), Integer.parseInt(strVector[1])));
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            if(vectors.size() == args.length) {
                TriangleIPE triangleIPE = new TriangleIPE(vectors);
                triangleIPE.printPDF();
            }
            else {
                System.out.println("Invalid value.");
            }
        }
        else {
            System.out.println("Command: java -jar FileName.jar x1,y1 x2,y2 x3,y3");
        }
    }
}
