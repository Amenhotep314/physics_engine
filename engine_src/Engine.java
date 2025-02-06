package engine_src;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Engine {

    // Inter-body forces
    public static final double G = 1;
    public static final double K = 10;

    // Fields
    public static final double B = 10;
    public static final double LITTLEG = 0;

    // Dimensions
    public static final double IMWIDTH = 1920;
    public static final double IMHEIGHT = 1080;
    public static final double WORLDWIDTH = 200;
    public static final double WORLDHEIGHT = (WORLDWIDTH / IMWIDTH) * IMHEIGHT;

    public static void main(String[] args) {
        Body[] world = {
            new Body("Earth", 50, 0, 0.8, 5, new Vector3(0, 0), new Vector3(0, 0), new Vector3(0, 150, 250)),
            new Body("Moon", 1, 0, 0.8, 1, new Vector3(10, 0), new Vector3(0, 5), new Vector3(100, 100, 100)),
            new Body("Evil Moon", 5, 0, 0.8, 3, new Vector3(0, -27), new Vector3(-2.7, 0), new Vector3(150, 100, 100)),
            new Body("Ship", 0.01, 0, 0.8, 0.3, new Vector3(0, -22.7), new Vector3(0.2, 0), new Vector3(255, 100, 100)),
        };

        int nParticles = 1000;
        Body[] particles = new Body[nParticles];
        for (int i = 0; i < nParticles; i++) {
            double x = Math.random() * WORLDWIDTH - (WORLDWIDTH / 2);
            double y = Math.random() * WORLDHEIGHT - (WORLDHEIGHT / 2);
            double vx = Math.random() * 0.1 - 0.05;
            double vy = Math.random() * 0.1 - 0.05;
            double mass = 1;
            double radius = 0.5;
            double charge = Math.random() * 2 - 1;
            double elasticity = 0.85;
            Vector3 position = new Vector3(x, y);
            Vector3 velocity = new Vector3(vx, vy);
            double colorn = charge * 127;
            Vector3 color = new Vector3(127+colorn, 0, 127-colorn);
            particles[i] = new Body("Particle", mass, charge, elasticity, radius, position, velocity, color);
        }

        int nCrazy = 100;
        Body[] crazyWorld = new Body[nCrazy];
        crazyWorld[0] = new Body("Earth", 100, 0, 0.99, 25, new Vector3(0, 0), new Vector3(0, 0), new Vector3(0, 175, 250));
        crazyWorld[1] = new Body("Moon", 10, 0, 0.99, 5, new Vector3(45, 0), new Vector3(0, 5), new Vector3(100, 100, 100));
        for (int i = 2; i < nCrazy; i++) {
            double x = Math.random() * (WORLDWIDTH / 4) - (WORLDWIDTH / 2);
            double y = Math.random() * WORLDHEIGHT - (WORLDHEIGHT / 2);
            double vx = Math.random() * 0.1 - 0.05;
            double vy = Math.random() * 0.1 - 0.05;
            double mass = 0.001;
            double radius = 0.5;
            double charge = Math.random() * 2 - 1;
            double elasticity = 0.8;
            Vector3 position = new Vector3(x, y);
            Vector3 velocity = new Vector3(vx, vy);
            double colorn = charge * 127;
            Vector3 color = new Vector3(127+colorn, 0, 127-colorn);
            crazyWorld[i] = new Body("Particle", mass, charge, elasticity, radius, position, velocity, color);
        }

        simulate(particles, 120, 100, 25, false, particles[0]);
        // simulate(world, 120, 100, 25, true, world[0]);
        // simulate(crazyWorld, 60, 100, 25, false, crazyWorld[0]);
        createVideoFromImages(25);
        cleanUpImages();
    }

    public static void simulate(Body[] world, int duration, int physicsFPS, int renderFPS, boolean stabilize, Body center) {

        // Initial vars
        double deltaT = 1.0 / physicsFPS;
        int frames = duration * physicsFPS;
        Vector3[] netForces = new Vector3[world.length];
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // Iterate over all frames
        for (int i = 0; i < frames; i++) {

            // Calculate net forces
            for (int j = 0; j < world.length; j++) {
                netForces[j] = Vector3.ORIGIN;
                Body thing = world[j];

                for (int k = 0; k < world.length; k++) {
                    Body other = world[k];
                    if (thing != other) {
                        netForces[j] = netForces[j].plus(thing.forceFrom(other));
                    }

                    // Check for collisions
                    if (k > j) {
                        thing.collideWith(other);
                    }
                }

                // Walls
                Vector3 position = worldToPixel(thing.getPosition());
                double radius = worldToPixel(thing.getRadius());
                if (position.x() - radius < 0) {
                    thing.setVelocity(thing.getVelocity().elementProduct(new Vector3(-1, 1)).scalarMultiply(thing.getElasticity()));
                    double overlap = radius - position.x();
                    Vector3 offset = new Vector3(pixelToWorld(overlap), 0);
                    thing.setPosition(thing.getPosition().plus(offset));
                } else if (position.x() + radius > IMWIDTH) {
                    thing.setVelocity(thing.getVelocity().elementProduct(new Vector3(-1, 1)).scalarMultiply(thing.getElasticity()));
                    double overlap = position.x() + radius - IMWIDTH;
                    Vector3 offset = new Vector3(pixelToWorld(overlap), 0);
                    thing.setPosition(thing.getPosition().minus(offset));
                }

                if (position.y() - radius < 0) {
                    thing.setVelocity(thing.getVelocity().elementProduct(new Vector3(1, -1)).scalarMultiply(thing.getElasticity()));
                    double overlap = radius - position.y();
                    Vector3 offset = new Vector3(0, pixelToWorld(overlap));
                    thing.setPosition(thing.getPosition().minus(offset));
                } else if (position.y() + radius > IMHEIGHT) {
                    thing.setVelocity(thing.getVelocity().elementProduct(new Vector3(1, -1)).scalarMultiply(thing.getElasticity()));
                    double overlap = position.y() + radius - IMHEIGHT;
                    Vector3 offset = new Vector3(0, pixelToWorld(overlap));
                    thing.setPosition(thing.getPosition().plus(offset));
                }
            }

            // Perform integrations
            for (int j = 0; j < world.length; j++) {
                Body thing = world[j];
                Vector3 acceleration = netForces[j].scalarMultiply(1 / thing.getMass());

                // Add fields here
                Vector3 gravity = new Vector3(0, LITTLEG);
                Vector3 magnetism = new Vector3(0, 0, B);
                acceleration = acceleration.plus(gravity);
                acceleration = acceleration.plus(thing.getVelocity().cross(magnetism).scalarMultiply(thing.getCharge() / thing.getMass()));

                thing.setVelocity(thing.getVelocity().plus(acceleration.scalarMultiply(deltaT)));
                thing.setPosition(thing.getPosition().plus(thing.getVelocity().scalarMultiply(deltaT)));
            }

            // Center
            if (stabilize) {
                Vector3 offset = center.getPosition();
                for (Body body : world) {
                    body.setPosition(body.getPosition().minus(offset));
                }
            }

            if (i % (physicsFPS / renderFPS) == 0) {
                final Body[] worldCopy = deepCopyWorld(world);
                final int frame = i;
                executor.submit(() -> render(worldCopy, frame));
            }

            System.out.println((float) i / frames * 100 + "%");
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Body[] deepCopyWorld(Body[] world) {
        Body[] copy = new Body[world.length];
        for (int i = 0; i < world.length; i++) {
            Body body = world[i];
            Vector3 positionCopy = new Vector3(body.getPosition().x(), body.getPosition().y(), body.getPosition().z());
            Vector3 velocityCopy = new Vector3(body.getVelocity().x(), body.getVelocity().y(), body.getVelocity().z());
            Vector3 colorCopy = new Vector3(body.getColor().x(), body.getColor().y(), body.getColor().z());
            copy[i] = new Body(body.getName(), body.getMass(), body.getCharge(), body.getElasticity(), body.getRadius(), positionCopy, velocityCopy, colorCopy);
        }
        return copy;
    }

    public static void render(Body[] world, int frame) {
        BufferedImage image = new BufferedImage((int) IMWIDTH, (int) IMHEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, (int) IMWIDTH, (int) IMHEIGHT);

        for (Body body : world) {
            Vector3 pixelPos = worldToPixel(body.getPosition());
            int radius = (int) worldToPixel(body.getRadius());
            g.setColor(new Color((int) body.getColor().x(), (int) body.getColor().y(), (int) body.getColor().z()));
            g.fillOval((int) pixelPos.x() - radius, (int) pixelPos.y() - radius, 2 * radius, 2 * radius);
        }

        g.dispose();
        try {
            ImageIO.write(image, "png", new File("images/" + String.format("%05d", frame) + ".png"));
            assert false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Vector3 worldToPixel(Vector3 position) {
        Vector3 ans = position.scalarMultiply(IMWIDTH / WORLDWIDTH);
        return new Vector3(
            ans.x() + (IMWIDTH / 2),
            -ans.y() + (IMHEIGHT / 2)
        );
    }

    public static double worldToPixel(double distance) {
        return distance * (IMWIDTH / WORLDWIDTH);
    }

    public static Vector3 pixelToWorld(Vector3 position) {
        Vector3 ans = new Vector3(
            position.x() - (IMWIDTH / 2),
            -position.y() + (IMHEIGHT / 2)
        );
        return ans.scalarMultiply(WORLDWIDTH / IMWIDTH);
    }

    public static double pixelToWorld(double distance) {
        return distance * (WORLDWIDTH / IMWIDTH);
    }

    public static void createVideoFromImages(int fps) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-nostdin", "-y", "-framerate", String.valueOf(fps), "-pattern_type", "glob", "-i", "images/*.png", "-c:v", "libx264", "-pix_fmt", "yuv420p", "out.mp4"
            );
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void cleanUpImages() {
        File dir = new File("images");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));
        if (files != null) {
            Arrays.stream(files).forEach(File::delete);
        }
    }
}