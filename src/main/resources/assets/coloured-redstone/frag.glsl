#version 110

varying vec4 outColor;
varying vec2 outTexCoords;
varying float redstone;

uniform sampler2D textureIn;
uniform vec3 chunkOffset;
uniform float time;

const vec3 luminance = vec3(0.2126, 0.7152, 0.0722);

void main() {
    vec4 color = texture2D(textureIn, outTexCoords) * outColor;
    gl_FragColor = color;

    if (redstone == 1.0) {
        vec3 c = 0.5 + 0.5 * cos(time + chunkOffset.xzz + vec3(0, 2, 4));
        gl_FragColor = vec4(c, color.a);
    }
}