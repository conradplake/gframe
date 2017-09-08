package gframe.engine.shader;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import gframe.engine.AbstractShader;
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.Point3D;
import gframe.engine.RenderFace;
import gframe.engine.Toolbox;
import gframe.engine.Vector3D;

/**
 * 
 * */
public class LSystemTurtleShader extends AbstractShader {

	long lastTimeInMillis = 0;
	long timePassedInMillis = 0;

	int textureWidth = 300;
	int textureHeight = 300;

	ImageRaster texture;

	Collection<Rule> rules;

	Turtle turtle;

	String axiom; // the start word
	String sentence;

	int maxGrowthSteps = 4;
	int step = 0;

	public LSystemTurtleShader(Lightsource lightsource) {
		super(lightsource);
		texture = new ImageRaster(textureWidth, textureHeight);

		this.turtle = new Turtle(textureWidth, 0);

		this.rules = new ArrayList<Rule>();

		// this rule generates a nice tree structure:
		// rules.add(new Rule('F', "FF+[+F-F-F]-[-F+F+F]"));
		// turtle.turningAngle = 25;
		// turtle.shrinkageFactor = .9f;

		// koch curve:
		rules.add(new Rule('F', "F+F-F-F+F"));
		turtle.turningAngle = 90;

		axiom = "-F";
		sentence = axiom;

		lastTimeInMillis = System.currentTimeMillis();
	}

	private String generateNextSentence() {
		StringBuilder nextSentence = new StringBuilder();
		for (int i = 0; i < sentence.length(); i++) {
			char current = sentence.charAt(i);
			boolean applied = false;
			for (Rule rule : rules) {
				if (rule.in == current) {
					nextSentence.append(rule.out);
					applied = true;
					break;
				}
			}
			if (!applied) {
				nextSentence.append(current);
			}
		}
		return nextSentence.toString();
	}

	@Override
	public void preShade(RenderFace renderFace) {

		long currentTimeInMillis = System.currentTimeMillis();
		timePassedInMillis += (currentTimeInMillis - lastTimeInMillis);

		long timestepInMillis = 200;

		while (timePassedInMillis > timestepInMillis) {

			if (step < maxGrowthSteps) {
				String nextSentence = generateNextSentence();
				sentence = nextSentence;

				turtle.reset();
				turtle.stepLength *= turtle.shrinkageFactor;
				turtle.move(sentence, texture);
				// turtle.move("F[+F-F+F]-F", texture);

				step++;
			}

			timePassedInMillis -= timestepInMillis;
		}

		lastTimeInMillis = currentTimeInMillis;
	};

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y,
			float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {

		float x = Math.min(textureWidth - 1, texel_u * (textureWidth));
		float y = Math.min(textureHeight - 1, texel_v * (textureHeight));

		int texel = texture.getPixel((int) x, (int) y);

		return super.shade(texel, world_x, world_y, world_z, normal_x, normal_y, normal_z);
	}

	@Override
	public boolean isPerPixelShader() {
		return true;
	}

	/**
	 * Simple rule says: each character from input sequence matching 'in'
	 * becomes 'out' in the output sequence.
	 */
	class Rule {
		char in;
		String out;

		public Rule(char in, String out) {
			this.in = in;
			this.out = out;
		}
	}

	/**
	 * A turtle can move around by reading a sequence of characters from
	 * alphabet A = {F, f, +, -, [, ]}.
	 */
	class Turtle {

		Color c = new Color(255, 255, 255, 100);

		float original_x;
		float original_y;

		Point3D position;
		Vector3D direction;

		float turningAngle = 25;
		float shrinkageFactor = 1;

		Stack<Object[]> stack;

		float stepLength = 4;

		Turtle(float x, float y) {
			this.original_x = x;
			this.original_y = y;
			this.position = new Point3D(x, y, 0);
			this.direction = new Vector3D(0, stepLength, 0); // point upwards by
																// default
			this.stack = new Stack<Object[]>();
		}

		public void reset() {
			this.position.setCoordinates(original_x, original_y, 0);
			this.direction.set(0, stepLength, 0); // point upwards by default
			this.stack.clear();
		}

		public void move(String sentence, ImageRaster texture) {

			for (int i = 0; i < sentence.length(); i++) {
				char current = sentence.charAt(i);

				if (current == 'F') {
					Point3D oldPos = position.copy();
					// move forward by stepLength and draw a line
					position.move(direction.x, direction.y, direction.z);

					int oldx = (int) oldPos.x;
					int oldy = (int) oldPos.y;
					int newx = (int) position.x;
					int newy = (int) position.y;

					if (Toolbox.isOutisde(oldx, 0, textureWidth - 1) || Toolbox.isOutisde(oldy, 0, textureHeight - 1)
							|| Toolbox.isOutisde(newx, 0, textureWidth - 1)
							|| Toolbox.isOutisde(newy, 0, textureHeight - 1)) {
						continue;
					} else {
						Toolbox.drawPolygon(texture, new int[] { oldx, newx }, new int[] { oldy, newy }, c.getRGB());
					}

				} else if (current == 'f') {
					// move forward by stepLength but dont draw a line
					position.move(direction.x, direction.y, direction.z);
				} else if (current == '+') {
					// turn right
					Toolbox.getZrotMatrix(-turningAngle).transform(direction);
				} else if (current == '-') {
					// turn left
					Toolbox.getZrotMatrix(turningAngle).transform(direction);
				} else if (current == '[') {
					// push current pos and direction onto stack
					stack.push(new Object[] { position.copy(), direction.copy() });
				} else if (current == ']') {
					// pull last state from stack
					Object[] state = stack.pop();
					if (state != null) {
						this.position = (Point3D) state[0];
						this.direction = (Vector3D) state[1];
					}
				}

			}

		}
	}

}
