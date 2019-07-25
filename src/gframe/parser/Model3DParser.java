package gframe.parser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import gframe.engine.Model3D;
import gframe.engine.Point3D;

public class Model3DParser {

	public static void write(Model3D model, String outputfile) throws java.io.IOException {
		FileWriter fw = new FileWriter(outputfile);

		fw.write("[Points]\n");

		Iterator<Point3D> it = model.getVertices().iterator();
		while (it.hasNext()) {
			Point3D p = it.next();
			fw.write(p.x + ", " + p.y + ", " + p.z + "\n");
		}

		// fw.write("[Faces]\n");
		// it = model.getFaces().iterator();
		
		fw.close();
	}

	public static Model3D parse(String filename) {
		Model3D mod = null;
		try {
			mod = parseModel3D(filename);
		} catch (Exception e) {
			mod = null;
		}
		return mod;
	}

	public static Model3D parseModel3D(String filename) throws java.io.IOException, NumberFormatException {
		return parse(new Model3D(), filename);
	}

	private static Model3D parse(Model3D model, String filename) throws java.io.IOException, NumberFormatException {
		model.clearGeometry();
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		float x = 0, y = 0, z = 0;
		int r = 0, g = 0, b = 0, p1 = 0, p2 = 0, p3 = 0, p4 = 0, state = 0;

		while (state != 100) {
			String token = nextToken(is);
			if (token.equals("") || token.equals("COMMENT"))
				continue;
			switch (state) {
			case 0:
				if (token.equals("[Points]")) {
					state = 1;
				} else if (token.equals("EOF")) {
					state = 100;
				}
				break;
			case 1:
				if (token.equals("[Faces]")) {
					state = 4;
					break;
				} else if (token.equals("EOF")) {
					state = 100;
					break;
				}
				x = Float.parseFloat(token);
				state = 2;
				break;
			case 2:
				y = Float.parseFloat(token);
				state = 3;
				break;
			case 3:
				z = Float.parseFloat(token);
				model.addVertex(new Point3D(x, y, z));
				state = 1;
				break;
			case 4:
				if (token.equals("EOF")) {
					state = 100;
					break;
				}
				p1 = Integer.parseInt(token);
				state = 5;
				break;
			case 5:
				p2 = Integer.parseInt(token);
				state = 6;
				break;
			case 6:
				p3 = Integer.parseInt(token);
				state = 7;
				break;
			case 7:
				p4 = Integer.parseInt(token);
				state = 8;
				break;
			case 8:
				r = Integer.parseInt(token);
				state = 9;
				break;
			case 9:
				g = Integer.parseInt(token);
				state = 10;
				break;
			case 10:
				b = Integer.parseInt(token);
				model.stretchFace(p1, p2, p3, p4, r, g, b);
				state = 4;
				break;
			}

		}
	
		return model;
	}

	private static String nextToken(InputStream is) {
		try {
			char c = (char) is.read();
			while ((c == '\n') || (c == ' ') || (c == '\t'))
				c = (char) is.read(); // skip
			if ((int) c == 65535) {
				return ("EOF");
			}
			if (c == '#') { // comment
				while ((c != '\n') && ((int) c != 65535))
					c = (char) is.read();
				return "COMMENT";
			}
			String token = "";
			while ((c != ',') && (c != ' ') && (c != '\n') && (c != '\t') && ((int) c != 65535)) {
				token += c;
				c = (char) is.read();
			}
			return token.trim();
		} catch (IOException ioe) {
			return "EOF";
		}
	}
}