public class Particle {
    double x, y; // position
    double angle; // angle in degrees
    double velocity; // velocity in pixels per second

    private final int WIDTH = 1280;
    private final int HEIGHT = 720;

    Particle(double x, double y, double angle, double velocity) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.velocity = velocity;
    }

    void move(double deltaTime) {
        // update particle position
        double newX = x + velocity * Math.cos(Math.toRadians(angle)) * deltaTime;
        double newY = y + velocity * Math.sin(Math.toRadians(angle)) * deltaTime;
        x = newX;
        y = newY;

        // check collision on borders
        if (newX < 0 || newX > WIDTH) {
            angle = 180 - angle;
        }
        if (newY < 0 || newY > HEIGHT) {
            angle = -angle;
        }
    }
}
