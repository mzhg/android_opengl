package com.nvidia.developer.opengl.utils;

import android.opengl.GLES20;

/**
 * Created by mazhen'gui on 2017/10/20.
 */

public class AttribBindingTask implements NvGLSLProgram.LinkerTask {
    private AttribBinder[] binders;

    public AttribBindingTask(AttribBinder...binders){
        this.binders = binders;
    }

    @Override
    public void invoke(int programID) {
        int length = binders != null ? binders.length : 0;
        for(int i = 0; i < length; i++){
            if(binders[i] != null){
                GLES20.glBindAttribLocation(programID, binders[i].getLocation(), binders[i].getName());
            }
        }
    }
}
