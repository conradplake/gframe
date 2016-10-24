package gframe.parser;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import gframe.engine.Model3D;

public class WavefrontObjParser {

	
	public static Model3D parse(File objFile, Color col){
		
		Model3D model = new Model3D();
				
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(objFile));
			for(String line=reader.readLine();line!=null;line=reader.readLine()){
				line = line.trim();
				
//				System.out.println(line);
								
				if(line.startsWith("v ")){
					String[] fields = line.split("\\s+");
					float x = Float.parseFloat(fields[1]);
					float y = Float.parseFloat(fields[2]);
					float z = Float.parseFloat(fields[3]);
					model.addVertex(x, y, z);
				}					
				else if(line.startsWith("f ")){
					String[] fields = line.split("\\s+");
					int[] vertices = new int[fields.length-1];								
					for(int i=1;i<fields.length;i++){						
						String[] subFields = fields[i].split("/");						
						vertices[i-1] = Integer.parseInt(subFields[0]) - 1;
					}
					model.stretchFace(vertices, col);
				}
			}			
		}
		catch(IOException e){			
			e.printStackTrace(); // :(
		}
		finally{
			if(reader!=null){
				try {
					reader.close();
				} catch (IOException ignore) {
				}
			}
		}
								
		return model;
		
	}

	
	
}
