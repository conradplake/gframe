package gframe.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import gframe.engine.KeyPosition;
import gframe.engine.Object3D;
import gframe.engine.Point3D;

public class AnimationsParser {

	public static List<KeyPosition> parse(File aniFile, long timestampOffset) {
		return parse(aniFile, timestampOffset, 1);
	}
	
	public static List<KeyPosition> parse(File aniFile, long timestampOffset, float scaleFactor) {

		List<KeyPosition> result = new LinkedList<KeyPosition>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(aniFile));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();

				// System.out.println(line);

				if (line.startsWith("#")) {
					continue;
				}

				String[] fields = line.split("\t");

				if (fields.length != 13 && fields.length != 4) {
					continue;
				}

				long timestamp = timestampOffset + (long)(Long.parseLong(fields[0]) * scaleFactor);
				float x = Float.parseFloat(fields[1]);
				float y = Float.parseFloat(fields[2]);
				float z = Float.parseFloat(fields[3]);

				float xx = 1;
				float xy = 0;
				float xz = 0;
				float yx = 0;
				float yy = 1;
				float yz = 0;
				float zx = 0;
				float zy = 0;
				float zz = 1;
				
				if(fields.length==13){
					xx = Float.parseFloat(fields[4]);
					xy = Float.parseFloat(fields[5]);
					xz = Float.parseFloat(fields[6]);			
					
					yx = Float.parseFloat(fields[7]);
					yy = Float.parseFloat(fields[8]);
					yz = Float.parseFloat(fields[9]);

					zx = Float.parseFloat(fields[10]);
					zy = Float.parseFloat(fields[11]);
					zz = Float.parseFloat(fields[12]);	
				}						

				Object3D position = new Object3D(new Point3D(x, y, z));
				position.getMatrix().setXAxis(xx, xy, xz);
				position.getMatrix().setYAxis(yx, yy, yz);
				position.getMatrix().setZAxis(zx, zy, zz);

				result.add(new KeyPosition(timestamp, position));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignore) {
				}
			}
		}

		return result;

	}

}
