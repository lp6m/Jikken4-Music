/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

// main fragment shader

#ifdef GL_ES

#extension GL_OES_standard_derivatives : enable

// Define default float precision for fragment shaders
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
precision highp int;
#else
precision mediump float;
precision mediump int;
#endif

#else

// Ignore GL_ES precision specifiers:
#define lowp
#define mediump
#define highp

#endif

vec4 apply_diffuse();
vec4 apply_specular();
vec3 apply_normal();
vec4 apply_selfIllum();

struct Light {
    vec4 pos;
    vec3 color;
};

uniform vec3 ambientColor;
uniform Light lights[3];

varying vec3 eyePos;
varying vec4 lightTangentSpacePositions[3];

void main()
{
    gl_FragColor = vec4(0.0,0.0,0.0,1.0);
    vec4 diffuse = apply_diffuse();

    if (diffuse.a == 0.0) discard;

    vec3 n = apply_normal();

    vec3 d = vec3(0.0);
    vec3 s = vec3(0.0);

    vec3 refl = reflect(normalize(eyePos), n);
    vec4 specular = apply_specular();
    float power = specular.a;

    vec3 l = normalize(lightTangentSpacePositions[0].xyz);
    d = clamp(dot(n,l), 0.0, 1.0)*(lights[0].color).rgb;
    s = pow(clamp(dot(-refl, l), 0.0, 1.0), power)*lights[0].color.rgb;

    l = normalize(lightTangentSpacePositions[1].xyz);
    d += clamp(dot(n,l), 0.0, 1.0)*(lights[1].color).rgb;
    s += pow(clamp(dot(-refl, l), 0.0, 1.0), power) * lights[1].color.rgb;

    vec3 rez = (ambientColor+d) * diffuse.xyz + s*specular.rgb;
    rez += apply_selfIllum().xyz;

    gl_FragColor = vec4(clamp(rez, 0.0, 1.0) , diffuse.a);
}
