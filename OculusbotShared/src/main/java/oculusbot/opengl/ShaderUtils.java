package oculusbot.opengl;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderUtils {

	/**
	 * Creates a shader program which can be used by OpenGL.
	 * 
	 * @param vertexShaderFilename
	 * @param fragmentShaderFilename
	 * @return handler for the shader program as int
	 */
	public static int createShaderProgram(String vertexShaderFilename, String fragmentShaderFilename)
			throws FileNotFoundException {
		String vertexShaderSrc;
		String fragmentShaderSrc;
		vertexShaderSrc = loadFile(vertexShaderFilename);
		fragmentShaderSrc = loadFile(fragmentShaderFilename);

		int program = glCreateProgram();
		int vertexShader = createShader(GL_VERTEX_SHADER, vertexShaderSrc);
		int fragmentShader = createShader(GL_FRAGMENT_SHADER, fragmentShaderSrc);
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);

		int status = glGetShaderi(program, GL_LINK_STATUS);
		if (status == GL_FALSE) {
			System.err.println("Linker failure: " + glGetProgramInfoLog(program));
		}

		glDetachShader(program, vertexShader);
		glDetachShader(program, fragmentShader);

		return program;
	}

	/**
	 * Tries to create a shader from the source code which can be linked to a
	 * shader program.
	 * 
	 * @param shaderType
	 *            - type of the shader
	 * @param shaderSrc
	 *            - source code of the shader
	 * @return shader handler as int
	 */
	private static int createShader(int shaderType, String shaderSrc) {
		int shader = glCreateShader(shaderType);
		glShaderSource(shader, shaderSrc);
		glCompileShader(shader);

		int status = glGetShaderi(shader, GL_COMPILE_STATUS);
		if (status == GL_FALSE) {
			System.err.println("Error in shader: " + glGetShaderInfoLog(shader));
			System.err.println("\nSource:\n" + shaderSrc);
		}

		return shader;
	}

	/**
	 * Loads a text file.
	 * 
	 * @param filename
	 *            - name/path for the file
	 * @return Content of the file as string with newlines
	 * @throws FileNotFoundException
	 */
	private static String loadFile(String filename) throws FileNotFoundException {
		StringBuffer buffer = new StringBuffer();
		Scanner in = new Scanner(new File(filename));
		while (in.hasNextLine()) {
			buffer.append(in.nextLine() + "\n");
		}
		in.close();
		return buffer.toString();
	}

}
