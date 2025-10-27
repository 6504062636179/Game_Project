public class Player {
    public int x, y;
    public int width, height;
    public int level = 2;
    public int baseWidth = 100;

    public void updateWidth() {
        switch(level) {
            case 1: width = baseWidth / 2; break;
            case 2: width = baseWidth; break;
            case 3: width = baseWidth * 2; break;
        }
    }
}
