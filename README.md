# Problem Set 2 : Particle Simulation - Explorer Mode

This is a particle simulation application that has a client-server communication method implemented. It operates in two modes: Developer(Server) and Explorer(Client). In Developer Mode, users can add particles to a 1280x720 pixel canvas, specifying their initial position, angle, and velocity. These particles move in straight lines and bounce off the canvas walls without changing speed, as all collisions are elastic. Particles can be added individually or in batches, with uniform distribution across positions, angles, or velocities. In Explorer Mode, a sprite is introduced that the user can move using various controls. The canvas zooms in on the sprite’s immediate surroundings, showing a 19x33 pixel area around it. As the sprite moves, any particles that come into this periphery are displayed until they exit. For both modes, the program must display all particles within the respective viewing areas and show an FPS counter every 0.5 seconds.

# Requirements

* Java 8 or higher.
* SDK 21
* A screen resolution capable of displaying 1280x720 pixels.

# Usage

* Run the Server's JAR file
  * This will open the Developer's window with 1280 x 720 pixel canvas. The coordinates (0, 0) are the southwest corner of the canvas, and the coordinates (1280, 720) are the northeast corner.

* Run the Client's JAR file
  * This will open the Explorer's window with 1280 x 720 pixel canvas. The coordinates (0, 0) are the southwest corner of the canvas, and the coordinates (1280, 720) are the northeast corner.
    
* In developer mode, three buttons and the canvas for the simulation will be presented.
    * Three of the buttons are for the user to add particles.
      * The X and Y inputs from the user are read as pixel coordinates.
      * When adding particles using methods addParticles, addParticlesByAngle, and addParticlesByVelocity, the angle parameter represents the initial angle of the particle in degrees.
      * Parameters startX and startY specify the minimum pixel coordinates for the starting position of the particles, while endX and endY specify the pixel coordinates for the ending position.

    * Once the user has inputted their desired value, it will be visible to the user on the canvas after adding the submit button.
      
    * A sprite is spawned in a space and is controlled using WASD keys or arrow keys.
