# gframe
A Java 3D engine that comes with a software renderer plus some demo applications

Features include:
 - pixel-perfect perspective correct texture mapping
 - sub-pixel & sub-texel accuracy
 - flat & phong shading
 - normal & specular mapping
 - shadow mapping
 - physics support & particles
 - importer for OBJ files


This project has a single dependency to lib/graph.jar, a library of graph algorithms I wrote during my time at university. It is used for generating voroni textures. Simply don't call the corresponding method in TextureGenerator.class if you want no other dependency than standard Java.

Feel free to do whatever you want with this code.
