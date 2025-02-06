import numpy as np
from PIL import Image, ImageDraw
import os
import glob
import subprocess


G = 1
im_width, im_height = 1920, 1080
world_width = 50
world_height = (world_width / im_width) * im_height


class body:

    def __init__(self, name, mass, radius, position, velocity, color=(255, 255, 255)):
        self.name = name
        self.mass = mass
        self.radius = radius
        self.position = position
        self.velocity = velocity
        self.color = color

    def __str__(self):
        return self.name

    def force_from(self, other):
        # Gravity
        r = self.position - other.position
        r_hat = r / np.linalg.norm(r)
        force = -G * self.mass * other.mass / np.linalg.norm(r)**2
        return force * r_hat


def main():

    earth = body('Earth', 50, 5, np.array([0, 0], dtype=float), np.array([0, 0], dtype=float), color=(0, 100, 200))
    moon = body('Moon', 1, 1, np.array([10, 0], dtype=float), np.array([0, 2.3], dtype=float), color=(100, 100, 100))
    ship = body('Ship', 0.01, 0.1, np.array([8, 0], dtype=float), np.array([0, 2], dtype=float), color=(255, 100, 100))
    world = [earth, moon, ship]
    simulate(world, 1000, fps=15)


def simulate(world, frames, fps=60):
    delta_t = 1/fps
    os.makedirs('images', exist_ok=True)
    for i in range(frames):

        # Compute net forces
        net_forces = []
        for thing in world:
            net_force = sum([thing.force_from(other) for other in world if other != thing])
            net_forces.append(net_force)

        # Perform integrations
        for j in range(len(world)):
            thing = world[j]
            acceleration = net_forces[j] / thing.mass
            thing.velocity += acceleration * delta_t
            thing.position += thing.velocity * delta_t

        # Detect elastic collisions
        for j in range(len(world)):
            thing = world[j]
            for k in range(j+1, len(world)):
                other_thing = world[k]
                if np.linalg.norm(thing.position - other_thing.position) < thing.radius + other_thing.radius:
                    # Calculate the normal and tangent vectors
                    normal = (thing.position - other_thing.position) / np.linalg.norm(thing.position - other_thing.position)
                    tangent = np.array([-normal[1], normal[0]])

                    # Project velocities onto the normal and tangent vectors
                    v1n = np.dot(normal, thing.velocity)
                    v1t = np.dot(tangent, thing.velocity)
                    v2n = np.dot(normal, other_thing.velocity)
                    v2t = np.dot(tangent, other_thing.velocity)

                    # Calculate new normal velocities using 1D elastic collision equations
                    v1n_new = (v1n * (thing.mass - other_thing.mass) + 2 * other_thing.mass * v2n) / (thing.mass + other_thing.mass)
                    v2n_new = (v2n * (other_thing.mass - thing.mass) + 2 * thing.mass * v1n) / (thing.mass + other_thing.mass)

                    # Convert scalar normal and tangential velocities into vectors
                    thing.velocity = v1n_new * normal + v1t * tangent
                    other_thing.velocity = v2n_new * normal + v2t * tangent

        # Render
        image = Image.new('RGB', (im_width, im_height), (0, 0, 0))
        draw = ImageDraw.Draw(image)
        for body in world:
            draw.circle(
                world_to_pixel(body.position),
                world_to_pixel(body.radius, coord=None),
                fill=body.color
            )
        image.save(f'images/{i:04d}.png')

        print(str(i/frames * 100))

    subprocess.run(["ffmpeg", "-nostdin", "-y", "-framerate", str(fps), "-pattern_type", "glob", "-i", "images/*.png", "-c:v", "libx264", "-pix_fmt", "yuv420p", "images/out.mp4"])
    for file in glob.glob("images/*.png"):
        if os.path.basename(file).isdigit():
            os.remove(file)


def world_to_pixel(num, coord="xy"):

    ans = (num / world_width) * im_width

    if coord=="xy":
        ans = np.array([
            ans[0] + (im_width / 2),
            -ans[1] + (im_height / 2)
        ])
    elif coord=="x":
        ans += im_width / 2
    elif coord=="y":
        ans = -ans + im_height
    else:
        pass

    return ans


if __name__ == '__main__':
    main()