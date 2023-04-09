package com.example.testopengl

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.abs

const val INT_BYTE_SIZE = 4
const val FLOAT_BYTE_SIZE = 4

fun floatBufferOf(vararg value: Float): FloatBuffer {
    return ByteBuffer
        .allocateDirect(value.size * FLOAT_BYTE_SIZE) // native 메모리 할당 사이즈
        .order(ByteOrder.nativeOrder()) // 왼쪽부터 채울것인지? 오른쪽부터 채울것인지? 시스템이 알아서 함
        .asFloatBuffer()
        .apply {
            put(value) // 데이터 전달
            position(0)
        }
}

fun intBufferOf(vararg value: Int): IntBuffer {
    return ByteBuffer
        .allocateDirect(value.size * INT_BYTE_SIZE) // native 메모리 할당 사이즈
        .order(ByteOrder.nativeOrder()) // 왼쪽부터 채울것인지? 오른쪽부터 채울것인지? 시스템이 알아서 함
        .asIntBuffer()
        .apply {
            put(value) // 데이터 전달
            position(0)
        }
}

fun FloatArray.toBuffer(): FloatBuffer {
    return ByteBuffer
        .allocateDirect(this.size * FLOAT_BYTE_SIZE) // native 메모리 할당 사이즈
        .order(ByteOrder.nativeOrder()) // 왼쪽부터 채울것인지? 오른쪽부터 채울것인지? 시스템이 알아서 함
        .asFloatBuffer()
        .apply {
            put(this@toBuffer) // 데이터 전달
            position(0)
        }
}

fun FloatBuffer.deepCopy(): FloatBuffer {
    return this.toFloatArray().toBuffer()
}

fun FloatArray.asBuffer(): FloatBuffer {
    return FloatBuffer.wrap(this).apply {
        position(0)
    }
}

fun IntArray.asBuffer(): IntBuffer {
    return IntBuffer.wrap(this).apply {
        position(0)
    }
}

fun List<Int>.toBuffer(): IntBuffer {
    return ByteBuffer
        .allocateDirect(this.size * FLOAT_BYTE_SIZE) // native 메모리 할당 사이즈
        .order(ByteOrder.nativeOrder()) // 왼쪽부터 채울것인지? 오른쪽부터 채울것인지? 시스템이 알아서 함
        .asIntBuffer()
        .apply {
            this@toBuffer.forEach {
                put(it) // 데이터 전달
            }
            position(0)
        }
}

fun List<Float>.toBuffer(): FloatBuffer {
    return ByteBuffer
        .allocateDirect(this.size * FLOAT_BYTE_SIZE) // native 메모리 할당 사이즈
        .order(ByteOrder.nativeOrder()) // 왼쪽부터 채울것인지? 오른쪽부터 채울것인지? 시스템이 알아서 함
        .asFloatBuffer()
        .apply {
            this@toBuffer.forEach {
                put(it) // 데이터 전달
            }
            position(0)
        }
}

fun FloatBuffer.toFloatArray(): FloatArray {
    val newArray = FloatArray(this.capacity())
    for (i in 0 until this.capacity()) {
        newArray[i] = this.get(i)
    }
    return newArray
}

fun IntBuffer.toIntArray(): IntArray {
    val newArray = IntArray(this.capacity())
    for (i in 0 until this.capacity()) {
        newArray[i] = this.get(i)
    }
    return newArray
}

fun FloatBuffer.asFloatArray(): FloatArray {
    return if (this.hasArray()) {
        this.array()
    } else {
        this.toFloatArray()
    }
}

fun IntBuffer.asIntArray(): IntArray {
    return if (this.hasArray()) {
        this.array()
    } else {
        this.toIntArray()
    }
}

fun createIdentity4Matrix(): FloatArray {
    return FloatArray(16).apply {
        Matrix.setIdentityM(this, 0)
    }
}

fun createVector4DArray(x: Float, y: Float, z: Float): FloatArray {
    return floatArrayOf(x, y, z, 1f)
}

val readOnlyIdentity4Matrix = createIdentity4Matrix()

fun FloatArray.isIdentityM(): Boolean {
    return this.contentEquals(readOnlyIdentity4Matrix)
}

fun FloatArray.deepCopy(): FloatArray {
    val result = FloatArray(this.size)
    this.forEachIndexed { index, value ->
        result[index] = value
    }
    return result
}

inline fun <T> runGL(block: () -> T): T {
    val result = block()
    val error = GLES20.glGetError()
    if (error != GLES20.GL_NO_ERROR) {
        val msg = ": glError 0x" + Integer.toHexString(error)
        Log.e("godgod", msg)
        throw RuntimeException(msg)
    }
    return result
}

internal fun normalizeX(screenX: Float, glViewWidth: Int): Float {
    return ((screenX / glViewWidth) * 2) - 1
}

internal fun normalizeY(screenY: Float, glViewHeight: Int): Float {
    return 1 - ((screenY / glViewHeight) * 2)
}

internal fun denormalizeX(normalizeX: Float, glViewWidth: Int): Float {
    return (normalizeX + 1) / 2 * glViewWidth
}

internal fun denormalizeY(normalizeY: Float, glViewHeight: Int): Float {
    return (-normalizeY + 1) / 2 * glViewHeight
}

/**
 *  receiver로 전달받은 수와, 인자로 전달받은 수 중에 누가 더 targetValue에 가까운지 추려낸다
 * return -1 : receiver수가 더 가깝다
 * return 1 : receiver수가 더 멀다
 * return 0 : 둘이 같다.
 */
internal fun Float.isNear(targetValue: Float, compareValue: Float): Int {
    val value1 = abs(targetValue - this)
    val value2 = abs(targetValue - compareValue)
    return when {
        value1 < value2 -> -1
        value1 > value2 -> 1
        else -> 0
    }.also {
        Log.e("godgod", "$it")
    }
}


enum class Type(val glValue: Int) {
    FRAGMENT(GLES30.GL_FRAGMENT_SHADER),
    VERTEX(GLES30.GL_VERTEX_SHADER);
}

/**
 * shader를 native 영역에 생성한다.
 * @return shader id
 */
fun createShader(type: Type, glslCode: String): Int {
    val shaderId = runGL { GLES30.glCreateShader(type.glValue) }
    if (shaderId == 0) {
        Log.e("godgod", "could not create new shader.")
    }

    runGL { GLES30.glShaderSource(shaderId, glslCode) }
    runGL { GLES30.glCompileShader(shaderId) }
    val compiledInfoArr = IntArray(1)
    runGL { GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compiledInfoArr, 0) }
    return if (compiledInfoArr.first() == 0) {
        Log.e("godgod", "Could not compile shader ${type.name}:")
        Log.e("godgod", " " + GLES20.glGetShaderInfoLog(shaderId))
        GLES30.glDeleteShader(shaderId)
        0
    } else {
        shaderId
    }
}

/**
 * shader를 관리할 프로그램을 생성한다.
 * @return program id
 */
fun createProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
    var program = runGL { GLES30.glCreateProgram() }
    if (program == 0) {
        Log.e("godgod", "Could not create program")
    }

    runGL { GLES30.glAttachShader(program, vertexShaderId) }
    runGL { GLES30.glAttachShader(program, fragmentShaderId) }

    runGL { GLES30.glLinkProgram(program) }
    val linkStatus = IntArray(1)
    GLES30.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
    if (linkStatus[0] != GLES20.GL_TRUE) {
        Log.e("godgod", "Could not link program: ")
        Log.e("godgod", GLES30.glGetProgramInfoLog(program))
        GLES30.glDeleteProgram(program)
        program = 0
    }
    return program
}

