package engine_src;

import java.lang.Math;
import java.util.Random;


public class Vector3 {

    public static final Vector3 ORIGIN = new Vector3(0, 0, 0);
    public static final Vector3 UP = new Vector3(0, 0, 1);
    public static final Vector3 DOWN = new Vector3(0, 0, -1);
    public static final Vector3 LEFT = new Vector3(-1, 0, 0);
    public static final Vector3 RIGHT = new Vector3(1, 0, 0);
    public static final Vector3 IN = new Vector3(0, 1, 0);
    public static final Vector3 OUT = new Vector3(0, -1, 0);

    private double x;
    private double y;
    private double z;
    private static Random r = new Random();

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(double x, double y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    public double x() { return this.x; }
    public double y() { return this.y; }
    public double z() { return this.z; }

    public Vector3 plus(Vector3 other) {
        return new Vector3(
            this.x() + other.x(),
            this.y() + other.y(),
            this.z() + other.z()
        );
    }

    public Vector3 minus(Vector3 other) {
        return new Vector3(
            this.x() - other.x(),
            this.y() - other.y(),
            this.z() - other.z()
        );
    }

    public Vector3 scalarMultiply(double scalar) {
        return new Vector3(
             this.x() * scalar,
             this.y() * scalar,
             this.z() * scalar
        );
     }

     public Vector3 elementProduct(Vector3 other) {
        return new Vector3(
            this.x() * other.x(),
            this.y() * other.y(),
            this.z() * other.z()
        );
    }

    public Vector3 elementDivide(Vector3 other) {
        return new Vector3(
            this.x() / other.x(),
            this.y() / other.y(),
            this.z() / other.z()
        );
    }

    public double dot(Vector3 other) {
        return (this.x() * other.x()) + (this.y() * other.y()) + (this.z() * other.z());
    }

    public Vector3 cross(Vector3 other) {
        return new Vector3(
            (this.y() * other.z()) - (this.z() * other.y()),
            (this.z() * other.x()) - (this.x() * other.z()),
            (this.x() * other.y()) - (this.y() * other.x())
        );
    }

    public double magnitude() {
        return Math.sqrt(this.x()*this.x() + this.y()*this.y() + this.z()*this.z());
    }

    public Vector3 normalize() {
        return this.scalarMultiply(1.0 / this.magnitude());
    }

    public static Vector3 randomDirection() {
        double randomX = r.nextGaussian();
        double randomY = r.nextGaussian();
        double randomZ = r.nextGaussian();

        Vector3 ans = new Vector3(randomX, randomY, randomZ);
        return ans.normalize();
    }

    public static Vector3 average(Vector3[] values) {

        double xsum = 0;
        double ysum = 0;
        double zsum = 0;

        for (Vector3 value : values) {
            xsum += value.x();
            ysum += value.y();
            zsum += value.z();
        }
        return new Vector3(xsum, ysum, zsum).scalarMultiply(1.0/values.length);
    }

    public static Vector3 sum(Vector3[] values) {

        double xsum = 0;
        double ysum = 0;
        double zsum = 0;

        for (Vector3 value : values) {
            xsum += value.x();
            ysum += value.y();
            zsum += value.z();
        }
        return new Vector3(xsum, ysum, zsum);
    }

    public String toString() {
        return "[" + String.valueOf(x) + ", " + String.valueOf(y) + ", " + String.valueOf(z) + "]";
    }
}
