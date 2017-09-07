# gframe
A java 3D engine that comes with a software rasterizer plus some demo applications

Features include:
 - pixel-perfect perspective correct texture mapping
 - sub-pixel & sub-texel accuracy
 - flat & phong shading
 - normal & specular mapping
 - shadow mapping
 - physics support & particles
 - importer for OBJ files


This project has a single dependency to lib/graph.jar, a library of graph algorithms, for genereating voroni textures. 
Simply don't use the corresponding method in gframe/generator/Texturegenerator if you want no other dependency than standard java.
