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

uniform mat4 viewProjectionMatrix;
uniform mat4 worldMatrix;
uniform vec3 camPos;
uniform vec3 ambientColor;

attribute vec3 pos;
attribute vec2 texCoords;
attribute vec4 tangent;

struct Light {
    vec4 pos;
    vec3 color;
};

//3 lights used
uniform Light lights[3];

varying vec4 lightTangentSpacePositions[3];
varying vec2 oTexCoords;
varying vec3 eyePos;

vec3 getLocalVector(vec3 global, vec3 tangentFrame[3]) {
    return vec3( dot(global,tangentFrame[1]), dot(global,tangentFrame[2]), dot(global,tangentFrame[0]) );
}

void main()
{
    vec3 tangentFrame[3];    

    vec4 worldPos = worldMatrix * vec4(pos, 1.0);

    // Note: The breaking of a vector and scale computation statement into
    //       2 separate statements is intentional to workaround a shader
    //       compiler bug on the Freescale iMX6 platform. See RT-37789 for details. 
    vec3 t1 = tangent.xyz * tangent.yzx;
         t1 *= 2.0;
    vec3 t2 = tangent.zxy * tangent.www;
         t2 *= 2.0;
    vec3 t3 = tangent.xyz * tangent.xyz;
         t3 *= 2.0;
    vec3 t4 = 1.0-(t3+t3.yzx);

    vec3 r1 = t1 + t2;
    vec3 r2 = t1 - t2;

    tangentFrame[0] = vec3(t4.y, r1.x, r2.z);
    tangentFrame[1] = vec3(r2.x, t4.z, r1.y);
    tangentFrame[2] = vec3(r1.z, r2.y, t4.x);
    tangentFrame[2] *= (tangent.w>=0.0) ? 1.0 : -1.0;
    
    mat3 sWorldMatrix = mat3(worldMatrix[0].xyz, 
                             worldMatrix[1].xyz, 
                             worldMatrix[2].xyz);

    //Translate the tangent frame to world space.
    tangentFrame[0] = sWorldMatrix * tangentFrame[0];
    tangentFrame[1] = sWorldMatrix * tangentFrame[1];
    tangentFrame[2] = sWorldMatrix * tangentFrame[2];
   
    vec3 Eye = camPos - worldPos.xyz;
    
    eyePos = getLocalVector(Eye, tangentFrame);
   
    vec3 L = lights[0].pos.xyz - worldPos.xyz;
    lightTangentSpacePositions[0] = vec4( getLocalVector(L,tangentFrame)*lights[0].pos.w, 1.0);

    L = lights[1].pos.xyz - worldPos.xyz;
    lightTangentSpacePositions[1] = vec4( getLocalVector(L,tangentFrame)*lights[1].pos.w, 1.0);

    L = lights[2].pos.xyz - worldPos.xyz;
    lightTangentSpacePositions[2] = vec4( getLocalVector(L,tangentFrame)*lights[2].pos.w, 1.0);

     mat4 mvpMatrix = viewProjectionMatrix * worldMatrix;

    //Send texcoords to Pixel Shader and calculate vertex position.
    oTexCoords = texCoords;
    gl_Position = mvpMatrix * vec4(pos,1.0);
}
