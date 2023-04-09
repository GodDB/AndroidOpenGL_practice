package com.example.testopengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TestRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private var programId: Int = 0

    private val vertices = floatBufferOf(
        0.0f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f
    )

    private val indices = intBufferOf(
        0, 1, 2
    )

    private val vboId = intArrayOf(0)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.e("godgod", "onSurfaceCreated")
        val vertexShaderSourceCode = FileReader.readFile(context, R.raw.vertex_shader)
        val fragmentShaderSourceCode = FileReader.readFile(context, R.raw.fragment_shader)
        val vertexShaderId = createShader(Type.VERTEX, vertexShaderSourceCode)
        val fragmentShaderId = createShader(Type.FRAGMENT, fragmentShaderSourceCode)
        programId = createProgram(vertexShaderId, fragmentShaderId)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.e("godgod", "onSurfaceChanged")
        runGL { GLES30.glViewport(0, 0, width, height) }
        runGL { GLES30.glEnable(GLES30.GL_DEPTH_TEST) } // z버퍼 적용
        runGL { GLES30.glEnable(GLES30.GL_BLEND) } // 알파 채널 사용

        // gl vbo 생성
        runGL { GLES30.glGenBuffers(1, vboId, 0) }
        // vbo 활성화
        runGL { GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId[0]) }
        // vbo에 vertex 정보 저장
        runGL { GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.capacity() * FLOAT_BYTE_SIZE, vertices, GLES30.GL_STATIC_DRAW) }
        // vao에게 vbo 연결 및 vbo의 데이터 구조 설명
        runGL { GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0) }
    }

    override fun onDrawFrame(gl: GL10?) {
        // gl clear
        runGL { GLES30.glClearColor(0f, 0f, 0f, 0f) }
        runGL { GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT) }

        // vao에 활성화
        runGL { GLES30.glEnableVertexAttribArray(0) }
        // program 활성화
        runGL { GLES30.glUseProgram(programId) }
        //draw
        // 이건 vbo에 정의된 버텍스로 그리는 방법임 -> 버텍스 갯수가 많아 버퍼의 갯수를 많이 차지하기 때문에 ibo 방식으로 인덱스로 전달하는게 좋음
        //runGL { GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3) }
        runGL { GLES30.glDrawElements(GLES30.GL_TRIANGLES, 3, GLES30.GL_UNSIGNED_INT, indices) }

        runGL { GLES30.glDisableVertexAttribArray(0) }
        // 프로그램 종료
        runGL { GLES20.glUseProgram(0) }
    }
}
