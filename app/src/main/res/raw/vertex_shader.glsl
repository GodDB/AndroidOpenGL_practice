#version 300 es

layout(location = 0) in vec3 position;

uniform vec3 v_Color;

out vec3 f_Color;

void main() {
    gl_Position = vec4(position, 1.0f);
    f_Color = v_Color;
}

