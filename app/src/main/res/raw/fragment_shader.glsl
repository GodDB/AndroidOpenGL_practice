#version 300 es
precision mediump float;

layout(location = 0) out vec4 fragColor;

in vec3 f_Color;

void main() {
    fragColor = vec4(f_Color, 1.0f);
}
