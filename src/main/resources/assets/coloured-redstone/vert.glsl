#version 110
#define SIZE 2

varying vec4 outColor;
varying vec2 outTexCoords;
varying float redstone;

// Don't really care about fog rn
// varying float outVertexDistance;

uniform vec4 redstoneTex[SIZE];
uniform sampler2D lightIn;

void main()
{
    outColor = gl_Color * texture2D(lightIn, (gl_MultiTexCoord1.xy / 256.0) + (0.5 / 16.0));
    outTexCoords = gl_MultiTexCoord0.xy;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    redstone = 0.0;
    for (int i = 0; i < SIZE; i++) {
        if (gl_MultiTexCoord0.x > redstoneTex[i].x && gl_MultiTexCoord0.x < redstoneTex[i].y && gl_MultiTexCoord0.y > redstoneTex[i].z && gl_MultiTexCoord0.y < redstoneTex[i].w) {
            redstone = 1.0;
            break;
        }
    }
}
