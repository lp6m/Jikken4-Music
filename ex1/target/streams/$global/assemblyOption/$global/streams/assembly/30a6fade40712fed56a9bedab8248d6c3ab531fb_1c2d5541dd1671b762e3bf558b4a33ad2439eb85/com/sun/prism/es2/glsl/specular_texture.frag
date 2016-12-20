/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

// specular texture fragment shader

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

uniform vec4 specularColor;
uniform sampler2D specularMap;

vec4 apply_specular()
{
    vec3 tSpec = texture2D(specularMap, oTexCoords).rgb;
    float sPower = specularColor.a;
    sPower *= dot(tSpec, vec3(0.299, 0.587, 0.114)); // Rec. 601 luma conversion
    return vec4(tSpec, sPower);
}
