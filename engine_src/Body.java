package engine_src;

public class Body {
    private Vector3 position;
    private Vector3 velocity;
    private Vector3 color;
    private double mass;
    private double charge;
    private double radius;
    private double elasticity;
    private String name;

    public Body(String name, double mass, double charge, double elasticity, double radius, Vector3 position, Vector3 velocity, Vector3 color) {
        this.name = name;
        this.mass = mass;
        this.charge = charge;
        this.elasticity = elasticity;
        this.radius = radius;
        this.position = position;
        this.velocity = velocity;
        this.color = color;
    }

    // Getters
    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public Vector3 getColor() {
        return color;
    }

    public double getMass() {
        return mass;
    }

    public double getCharge() {
        return charge;
    }

    public double getElasticity() {
        return elasticity;
    }

    public double getRadius() {
        return radius;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public void setVelocity(Vector3 velocity) {
        this.velocity = velocity;
    }

    public void setColor(Vector3 color) {
        this.color = color;
    }

    public String toString() {
        return this.name;
    }

    public Vector3 forceFrom(Body other) {
        Vector3 r = this.position.minus(other.getPosition());
        Vector3 rHat = r.normalize();
        double gravity = -Engine.G * this.mass * other.getMass() / Math.pow(r.magnitude(), 2);
        double electrostatic = Engine.K * this.charge * other.getCharge() / Math.pow(r.magnitude(), 2);
        return rHat.scalarMultiply(gravity).plus(rHat.scalarMultiply(electrostatic));
    }

    public boolean collideWith(Body other) {
        if (this.position.minus(other.position).magnitude() < this.radius + other.radius) {
            Vector3 normal = this.position.minus(other.getPosition()).normalize();
            Vector3 tangent = new Vector3(-normal.y(), normal.x());

            double v1n = normal.dot(this.velocity);
            double v1t = tangent.dot(this.velocity);
            double v2n = normal.dot(other.getVelocity());
            double v2t = tangent.dot(other.getVelocity());

            double v1nNew = (v1n * (this.mass - other.getMass()) + 2 * other.getMass() * v2n) / (this.mass + other.getMass());
            double v2nNew = (v2n * (other.getMass() - this.mass) + 2 * this.mass * v1n) / (this.mass + other.getMass());

            this.velocity = normal.scalarMultiply(v1nNew).plus(tangent.scalarMultiply(v1t)).scalarMultiply(this.elasticity);
            other.setVelocity(normal.scalarMultiply(v2nNew).plus(tangent.scalarMultiply(v2t)).scalarMultiply(other.getElasticity()));

            // Separate the bodies to prevent overlap
            double overlap = this.radius + other.radius - this.position.minus(other.getPosition()).magnitude();
            Vector3 separation = normal.scalarMultiply(overlap / 2);
            this.position = this.position.plus(separation);
            other.setPosition(other.getPosition().minus(separation));

            return true;
        }
        return false;
    }
}