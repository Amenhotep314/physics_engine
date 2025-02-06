# Physics Engine
This is for fun! I'm incorporating more functionality over time, slowly adding the capacity to simulate more phenomena.
Right now, this simulator operates on two-dimensional circular bodies.

## Current Features
These circular bodies are characterized by radius, mass, charge, and elasticity. They can be assigned initial positions and velocities.
The following can currently affect the bodies:
 - Gravity (Newton's law of gravitation)
 - Electrostatic attraction (Coulomb's law)
 - Collisions with other bodies and simulation walls
 - Uniform gravitational field up or down
 - Uniform magnetic field into or out of the page (Lorentz force law)

Accelerations are determined Newtonianly, performing numerical integrations from the second law with finite $\Delta t$.

## Coming Features
This is a list of things I want to implement:
 - Time-varying conditions
 - Three-dimensional simulation (easy) and rendering (harder)
 - Better collisions (right now they are elastic with a simple damping coefficient)
 - Other spicy stuff??