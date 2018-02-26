////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
// Copyright 2017 mzhg
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations
// under the License.
////////////////////////////////////////////////////////////////////////////////
package com.nvidia.developer.opengl.app;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.nvidia.developer.opengl.utils.Pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mazhen'gui on 2017/4/14.
 */

public class NvInputHandler {
    private NvInputCallbacks mInputListener;
    private boolean[] pressedKeys = new boolean[128];
    private _OnKeyListener keyListener;

    private final int[] touchX = new int[20];
    private final int[] touchY = new int[20];
    private final boolean[] isTouched = new boolean[20];
    private final NvPointerEvent[][] specialEvents = new NvPointerEvent[6][12];
    private int mainCursor;
    private final int[] subCursor = new int[6];
    private final int[] eventType = new int[6];
    private _OnTouchListener touchListener;

    public NvInputHandler(View view){
        keyListener = new _OnKeyListener();
        touchListener = new _OnTouchListener();
        view.setOnKeyListener(keyListener);
        view.setOnTouchListener(touchListener);
    }

    public void setInputListener(NvInputCallbacks listener){
        mInputListener = listener;
    }

    public NvInputCallbacks getInputListener(){
        return mInputListener;
    }

    public void pollEvents(){
        List<_KeyEvent> events = keyListener.getKeyEvents();
        List<NvPointerEvent> pEvents = touchListener.getTouchEvents();

        synchronized (this){
            if(mInputListener ==null){
                return;
            }

            for(int i = 0; i < events.size(); i++){
                boolean handled = false;
                _KeyEvent e = events.get(i);
                int code = e.keyCode;
                boolean down = e.down;

                handled = mInputListener.keyInput(code, down ? NvKeyActionType.DOWN : NvKeyActionType.UP);
                if(!handled && down){
                    char c = e.keyChar;
                    if(c != 0)
                        mInputListener.characterInput(c);
                }
            }

            if(pEvents.size() > 0){
                splitEvents(pEvents);

                for(int i = 0; i <= mainCursor; i++){
                    mInputListener.pointerInput(NvInputDeviceType.TOUCH, eventType[i], 0, subCursor[i], specialEvents[i]);
                }
            }
        }
    }

    public boolean isKeyPressd(int keyCode){
        return (keyCode < 0 || keyCode > 127) ? false : pressedKeys[keyCode];
    }

    public boolean isTouchDown(int pointer){
        return (pointer < 0 || pointer >=20) ? false :isTouched[pointer];
    }

    public int getTouchX(int pointer){
        return (pointer < 0 || pointer >=20) ? 0 :touchX[pointer];
    }

    public int getTouchY(int pointer){
        return (pointer < 0 || pointer >=20) ? 0 :touchY[pointer];
    }

    private final void splitEvents(List<NvPointerEvent> pEvents){
        mainCursor = -1;
        Arrays.fill(subCursor, 0);

        int size = pEvents.size();
        int lastType = -1;
        for(int i = 0; i < size; i++){
            NvPointerEvent event = pEvents.get(i);

            if(event.type !=lastType){
                lastType = event.type;
                mainCursor ++;

//				Log.e("splitEvents", "mainCursour = " + mainCursor);
                int pact = 0;
                switch (event.type) {
                    case MotionEvent.ACTION_CANCEL:
                        pact = NvPointerActionType.UP;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        pact = NvPointerActionType.MOTION;
                        break;
                    case MotionEvent.ACTION_DOWN:
                        pact = NvPointerActionType.DOWN;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        pact = NvPointerActionType.EXTRA_DOWN;
                        break;
                    case MotionEvent.ACTION_UP:
                        pact = NvPointerActionType.UP;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        pact = NvPointerActionType.EXTRA_UP;
                        break;
                    default:
                        break;
                }

//				   Log.e("splitEvents", "pack = " + pact);
                eventType[mainCursor] = pact;
            }

            specialEvents[mainCursor][wrap(subCursor[mainCursor] ++, 12)] = event;
        }
    }

    private static int wrap(int value, int limit){
        return value < limit ? value : limit - 1;
    }

    private static final class _KeyEvent{
        boolean down;
        int keyCode;
        char keyChar;
    }

    private final class _OnTouchListener implements View.OnTouchListener {
        Pool<NvPointerEvent> touchEventPool;
        List<NvPointerEvent> touchEvents = new ArrayList<>();
        List<NvPointerEvent> touchEventsBuffer = new ArrayList<>();

        public _OnTouchListener() {
            Pool.PoolObjectFactory<NvPointerEvent> factory = new Pool.PoolObjectFactory<NvPointerEvent>() {
                @Override
                public NvPointerEvent createObject() {
                    return new NvPointerEvent();
                }
            };

            touchEventPool = new Pool<>(factory, 100);
        }

        @Override
        public boolean onTouch(View arg0, MotionEvent event) {
            synchronized (this) {
                int action = event.getAction() & MotionEvent.ACTION_MASK;
                int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK)
                        >> MotionEvent.ACTION_POINTER_ID_SHIFT;
                int pointerId = event.getPointerId(pointerIndex);

                NvPointerEvent touchEvent;
                switch(action){
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        touchEvent = touchEventPool.newObject();
                        touchEvent.type = action;
                        touchEvent.m_id = pointerId;
                        touchEvent.m_x = touchX[pointerId] = (int) (event.getX(pointerIndex) + 0.5f);
                        touchEvent.m_y = touchY[pointerId] = (int) (event.getY(pointerIndex) + 0.5f);
                        isTouched[pointerId] = true;
                        touchEventsBuffer.add(touchEvent);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        touchEvent = touchEventPool.newObject();
                        touchEvent.type = action;
                        touchEvent.m_id = pointerId;
                        touchEvent.m_x = touchX[pointerId] = (int) (event.getX(pointerIndex) + 0.5f);
                        touchEvent.m_y = touchY[pointerId] = (int) (event.getY(pointerIndex) + 0.5f);
                        isTouched[pointerId] = false;
                        touchEventsBuffer.add(touchEvent);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int pointerCount = event.getPointerCount();
                        for(int i = 0; i < pointerCount; i++){
                            pointerIndex = i;
                            pointerId = event.getPointerId(pointerIndex);

                            touchEvent = touchEventPool.newObject();
                            touchEvent.type = action;
                            touchEvent.m_id = pointerId;
                            touchEvent.m_x = touchX[pointerId] = (int) (event.getX(pointerIndex) + 0.5f);
                            touchEvent.m_y = touchY[pointerId] = (int) (event.getY(pointerIndex) + 0.5f);
                            touchEventsBuffer.add(touchEvent);
                        }
                        break;
                }
                return true;
            }
        }

        List<NvPointerEvent> getTouchEvents(){
            synchronized (this) {
                int len = touchEvents.size();
                for(int i = 0; i < len; i++)
                    touchEventPool.freeObject(touchEvents.get(i));

                touchEvents.clear();
                touchEvents.addAll(touchEventsBuffer);
                touchEventsBuffer.clear();
                return touchEvents;
            }
        }
    };

    private final class _OnKeyListener implements View.OnKeyListener {

        Pool<_KeyEvent> keyEventPool;
        List<_KeyEvent> keyEventsBuffer = new ArrayList<_KeyEvent>();
        List<_KeyEvent> keyEvents = new ArrayList<_KeyEvent>();

        public _OnKeyListener() {
            Pool.PoolObjectFactory<_KeyEvent> factory = new Pool.PoolObjectFactory<_KeyEvent>() {
                public _KeyEvent createObject() {
                    return new _KeyEvent();
                }
            };

            keyEventPool = new Pool<>(factory, 100);
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(event.getAction() == KeyEvent.ACTION_MULTIPLE)
                return false;

            synchronized (this) {
                _KeyEvent keyEvent = keyEventPool.newObject();
                keyEvent.keyCode = keyCode;
                keyEvent.keyChar = (char)event.getUnicodeChar();
                keyEvent.down = event.getAction() == KeyEvent.ACTION_DOWN;

                if(keyCode > 0 && keyCode <127)
                    pressedKeys[keyCode] = keyEvent.down;

                keyEventsBuffer.add(keyEvent);
            }

            return false;
        }

        List<_KeyEvent> getKeyEvents(){
            synchronized (this) {
                int len = keyEvents.size();
                for(int i = 0; i < len; i++)
                    keyEventPool.freeObject(keyEvents.get(i));

                keyEvents.clear();
                keyEvents.addAll(keyEventsBuffer);
                keyEventsBuffer.clear();
                return keyEvents;
            }
        }
    };
}
