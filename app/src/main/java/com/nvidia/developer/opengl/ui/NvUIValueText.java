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
package com.nvidia.developer.opengl.ui;

import com.nvidia.developer.opengl.utils.NvUtils;

/**
 * A UI element that converts numeric values to text and draws title and value.<p>
 * This class wrappers rendering of a title string and a value string to the screen
 * leveraging NvUIText.<p>
 * Valid combinations of alignments of the two items and the resulting output are:<pre>
    - title: left, value: left
        ==> [TITLE VALUE         ]
    - title: left, value: right
        ==> [TITLE          VALUE]
    - title: right, value: right
        ==> [         VALUE TITLE]
 * </pre>
 * @author Nvidia 2014-9-9 21:17
 *
 */
public class NvUIValueText extends NvUIText{

	/** The current value. */
	protected float m_value;
	/** Whether or not the value is actually an integer. */
	protected boolean m_integral;
    /** Number of digits after decimal point to display, zero for default string formatting. */
	protected int m_precision;
    /** The NvUIText defining the output value text. */
	protected NvUIText m_valueText;
    /** The alignment of the title output, so we can position value relative to title. */
	protected int m_titleAlign;
    /** The alignment of the value output, so we can position relative to title. */
	protected int m_valueAlign;
    /** If nonzero, the action code we respond to for changing our value. */
	protected int m_action;
    
	/**
	 * Constructor for onscreen text element displaying a floating-point value.
	 * @param str Title string to display.
	 * @param font Font family to use for this text.
	 * @param size Font size to use for this text.
	 * @param titleAlign Alignment to use for the title text.
	 * @param value Starting value to show.
	 * @param precision Number of digits after decimal point to display.
	 * @param valueAlign Alignment to use for the value text.
	 * @param actionCode <b>[</b>optional<b>]</b> Reaction code to match to set our value.
	 */
	public NvUIValueText(String str, int font, float size, int titleAlign, 
			float value, int precision, int valueAlign, int actionCode) {
		super(str, font, size, titleAlign);
		m_value = value;
		m_precision = (precision>6)?6:precision;
		m_valueText = new NvUIText("0", font, size, valueAlign);
		m_titleAlign = titleAlign;
		m_valueAlign = valueAlign;
		m_action = actionCode;
	
		setValue(value);
		positionOutput();
	}
	
	/**
	 * Constructor for onscreen text element displaying a integer value.
	 * @param str Title string to display.
	 * @param font Font family to use for this text.
	 * @param size Font size to use for this text.
	 * @param titleAlign Alignment to use for the title text.
	 * @param value Starting value to show.
	 * @param valueAlign Alignment to use for the value text.
	 * @param actionCode <b>[</b>optional<b>]</b> Reaction code to match to set our value.
	 */
	public NvUIValueText(String str, int font, float size, int titleAlign, 
			int value, int valueAlign, int actionCode) {
		super(str, font, size, titleAlign);
		
		m_value = value;
		m_integral = true;
		m_valueText = new NvUIText("0", font, size, valueAlign);
		m_titleAlign = titleAlign;
		m_valueAlign = valueAlign;
		m_action = actionCode;
		
		setValue(value);
		positionOutput();
	}
	
	/** Set the horizontal alignment of the value text. */
    public void setValueAlignment(int halign){
    	m_valueAlign = halign;
    	m_valueText.setAlignment(halign);
    	positionOutput();
    }
    
    /** Set the alpha transparency of both text strings. */
    public void setAlpha(float alpha){
    	super.setAlpha(alpha);
    	m_valueText.setAlpha(alpha);
    }
    
    /** Set the overall color of both text strings. */
    public void setColor(int color){
    	super.setColor(color);
    	setValueColor(color);
    }
    
    /** Set the color of just the value text string. */
    public void setValueColor(int color){
    	if(m_valueText == null){
    		return;
    	}
    	
    	m_valueText.setColor(color);
    }
    
    /** Set drop-shadow features on the text strings. */
    @Override
    public void setShadow(byte offset, int color){
    	super.setShadow(offset, color);
    	m_valueText.setShadow(offset, color);
    }

    /** Set a box the two strings will render within.
        @see NvUIText
    */
    @Override
    public void setTextBox(float width, float height, int lines, int dots){
    	super.setTextBox(width, height, lines, dots);
    	m_valueText.setTextBox(width, height, lines, dots);
    }

    /** Override to set dimensions of both text strings. */
    public void setDimensions(float w, float h){
    	super.setDimensions(w, h);
    	
    	positionOutput();
    }

    /** Override to set origin of both text strings. */
    public void setOrigin(float x, float y){
    	super.setOrigin(x, y);
    	
    	positionOutput();
    }
    
    //======================================================================
    // valid combinations for now are:
    // title: left, value: left   == [TITLE VALUE         ]
    // title: left, value: right  == [TITLE          VALUE]
    // title: right, value: right == [         VALUE TITLE]
    //======================================================================
    private void positionOutput(){
    	if(m_valueText == null)
    		return;
    	
    	final float enSpace = getFontSize() * 0.25f; // !!!!TBD TODO look up real en space size?
    	NvUIRect tr = getScreenRect();
        // reset.
        m_valueText.setDimensions(tr.width, tr.height);
        m_valueText.setOrigin(tr.left, tr.top);

        final float tw = super.getStringPixelWidth();

        if (m_valueAlign == NvUITextAlign.RIGHT)
        {
            // if both alignments are right, then put number on the LEFT
            if (m_titleAlign == NvUITextAlign.RIGHT)
            {// reset WIDTH of valuetext to base width minus title width minus padding
                if (tw>0) // then we need to offset, else we're fine.
                    m_valueText.setOrigin(tr.left - tw - enSpace, tr.top);
            }
            else // put number far right of rect
            {
                m_valueText.setOrigin(tr.left + tr.width, tr.top);
            }
        }
        else // else value must be left-aligned, base off title
        {
            if (tw>0) // then we need to offset, else we're fine.
                m_valueText.setOrigin(tr.left + tw + enSpace, tr.top);
        }
    }

    /** Override to draw both title and value strings to the viewport. */
    public void draw(NvUIDrawState drawState){
    	if (!m_isVisible) return;
    	
    	super.draw(drawState);
    	m_valueText.draw(drawState);
    }

    /** Set the font size to use for both text strings. */
    public void setFontSize(float size){
    	super.setFontSize(size);
    	m_valueText.setFontSize(size);
    }

    /** Set the value to be drawn as a string. */
    public void setValue(float value){
    	m_value = value;
    	
    	m_valueText.setString(NvUtils.formatPercisice(value, m_precision));
    }
    
    /** Set an integer value to be drawn as a string. */
    public void setValue(int value){
    	m_integral = true;
    	m_value = value;
    	m_valueText.setString(Integer.toString(value));
    }

    /** Override to set our value if an appropriate reaction comes through. */
    public int handleReaction(NvUIReaction react){
    	if (((react.flags & NvReactFlag.FORCE_UPDATE) != 0)
    	        || (m_action != 0 && (react.code==m_action)))
    	    {
    	        if (m_integral) // uses integer value.
    	            setValue(react.ival);
    	        else // uses the float value
    	            setValue(react.fval);
    	    }
    	    return NvUIEventResponse.nvuiEventNotHandled;
    }

}
