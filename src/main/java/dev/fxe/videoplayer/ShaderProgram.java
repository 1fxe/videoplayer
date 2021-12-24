package dev.fxe.videoplayer;

import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class ShaderProgram {

	private final int program;
	private final HashMap<String, Integer> uniforms = new HashMap<>();

	public ShaderProgram(String vertexShader, String fragShader) {
		boolean doVs = vertexShader != null;
		boolean doFs = fragShader != null;

		this.program = GL20.glCreateProgram();
		int vsID = 0;
		if (doVs) {
			try {
				InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(vertexShader);
				vertexShader = IOUtils.toString(inputStream);
				IOUtils.closeQuietly(inputStream);
				vsID = this.compileShader(GL20.GL_VERTEX_SHADER, vertexShader);
				GL20.glAttachShader(program, vsID);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		int fsID = 0;
		if (doFs) {
			try {
				InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fragShader);
				fragShader = IOUtils.toString(inputStream);
				IOUtils.closeQuietly(inputStream);
				fsID = this.compileShader(GL20.GL_FRAGMENT_SHADER, fragShader);
				GL20.glAttachShader(program, fsID);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		GL20.glLinkProgram(program);
		GL20.glValidateProgram(program);
		if (doVs) {
			GL20.glDeleteShader(vsID);
		}
		if (doFs) {
			GL20.glDeleteShader(fsID);
		}
	}

	private int compileShader(int type, String source) {
		int id = GL20.glCreateShader(type);
		GL20.glShaderSource(id, source);
		GL20.glCompileShader(id);
		int result = GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS);
		if (result == GL11.GL_FALSE) {
			String info = GL20.glGetShaderInfoLog(id, 500);
			System.out.println(info);
			GL20.glDeleteShader(id);
			throw new RuntimeException("Could not create shader");
		}
		return id;
	}

	public void registerUniform(String uniform) {
		int location = GL20.glGetUniformLocation(this.program, uniform);
		this.uniforms.put(uniform, location);
	}

	public int getUniform(String uniform) {
		return this.uniforms.get(uniform);
	}

	public void bind() {
		GL20.glUseProgram(this.program);
	}

	public void unbind() {
		GL20.glUseProgram(0);
	}

}
