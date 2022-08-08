public class Vector2 {
    int x;
    int y;

    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        String result = x + " " + y;
        return result;
    }
}
