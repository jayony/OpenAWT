
/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2011 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&M University 
 *        nor the names of its contributors may be used to endorse or promote 
 *        products derived from this software without specific prior written 
 *        permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *  
 *******************************************************************************/

package org.openawt.draw.android;

import java.awt.Graphics2D;

import org.openawt.BasicStroke;
import org.openawt.Shape;
import org.openawt.geom.*;
import org.openawt.svg.SVGGroup;
import org.openawt.svg.SVGShape;
import org.openawt.svg.Style;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Path;
import android.graphics.Path.FillType;

public class ShapePainter {
	public static void drawGroup(Canvas c, SVGGroup group,Style override){
		if(override==null)
			override = group.getStyle();
		for(SVGShape shape:group){
			if(group.getStyle()!=null)
				shape.setStyle(group.getStyle());
			draw(c,shape,override);
		}
	}
	public static void draw(Canvas c, SVGShape shape){
		draw(c,shape,null);
	}
	public static void draw(Canvas c, SVGShape shape, Style override){
		if(shape==null)
			return;
		if(shape instanceof SVGGroup){
			drawGroup(c,(SVGGroup)shape,override);
		}
		else{
			Paint paint = new Paint();
			Style style = override;
			if(style==null){ //Override style is null
				style = shape.getStyle();
			}
			if(style==null){ //Shape style is null, set it to a black stroke
				style = new Style().setStroke(org.openawt.Color.BLACK).setFill(org.openawt.Color.NONE).setStrokeWidth(1f);
			}
			org.openawt.Color fillColor = style.getFill();
			if(fillColor!=null && !fillColor.isClear()){
				paint.setColor(Color.argb(fillColor.getAlpha(), fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue()));
				fill(c,shape.getShape(),paint);
			}

			org.openawt.Color strokeColor = style.getStroke();
			Float strokeWidth = style.getStrokeWidth();
			
			if(strokeColor!=null)
				paint.setColor(Color.argb(strokeColor.getAlpha(), strokeColor.getRed(), strokeColor.getGreen(), strokeColor.getBlue()));
				//paint.setColor(Color.BLACK);
			else
				paint.setColor(Color.BLACK);
			if(strokeWidth != null)
				paint.setStrokeWidth(strokeWidth);
			else
				paint.setStrokeWidth(2f);

			if(strokeColor!=null || strokeWidth!=null){
				draw(c, shape.getShape(), paint);
			}
		}
	}
	public static void draw(Canvas c, Shape s, BasicStroke strokeStyle, Paint paint){
		if(strokeStyle!=null)
			applyStrokeStyle(strokeStyle,paint);
		draw(c,s,paint);
	}
	public static void draw(Canvas c, Shape s, Paint paint){
		if(s!=null){
			PathIterator pi = s.getPathIterator(null);
			Path path = getPath(pi);
			paint.setStyle(Paint.Style.STROKE);
			c.drawPath(path, paint);
		}
	}
	public static void fill(Canvas c, Shape s, BasicStroke strokeStyle,Paint paint){
		if(strokeStyle!=null)
			applyStrokeStyle(strokeStyle,paint);
		fill(c,s,paint);
	}
	public static void fill(Canvas c, Shape s, Paint paint){
		PathIterator pi = s.getPathIterator(null);
		Path path = getPath(pi);
		switch(pi.getWindingRule()){
		case PathIterator.WIND_EVEN_ODD:
			path.setFillType(FillType.EVEN_ODD);
			break;
		case PathIterator.WIND_NON_ZERO:
			path.setFillType(FillType.WINDING);
			break;
		}
		c.drawPath(path, paint);
	}
	public static void applyStrokeStyle(BasicStroke strokeStyle, Paint paint){
		switch(strokeStyle.getEndCap()){
		case BasicStroke.CAP_BUTT:
			paint.setStrokeCap(Cap.BUTT);
			break;
		case BasicStroke.CAP_ROUND:
			paint.setStrokeCap(Cap.ROUND);
			break;
		case BasicStroke.CAP_SQUARE:
			paint.setStrokeCap(Cap.SQUARE);
		}

		switch(strokeStyle.getLineJoin()){
		case BasicStroke.JOIN_BEVEL:
			paint.setStrokeJoin(Join.BEVEL);
			break;
		case BasicStroke.JOIN_MITER:
			paint.setStrokeJoin(Join.MITER);
			paint.setStrokeMiter(strokeStyle.getMiterLimit());
			break;
		case BasicStroke.JOIN_ROUND:
			paint.setStrokeJoin(Join.ROUND);
			break;
		}

		paint.setStrokeWidth(strokeStyle.getLineWidth());
	}
	public static Path getPath(PathIterator pi){
		Path path = new Path();
		for(;!pi.isDone();pi.next()){
			float[] coords = new float[6];
			switch (pi.currentSegment(coords)){
			case PathIterator.SEG_CLOSE:
				path.close();
				break;
			case PathIterator.SEG_CUBICTO:
				path.cubicTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
				break;
			case PathIterator.SEG_LINETO:
				path.lineTo(coords[0], coords[1]);
				break;
			case PathIterator.SEG_MOVETO:
				path.moveTo(coords[0], coords[1]);
				break;
			case PathIterator.SEG_QUADTO:
				path.quadTo(coords[0], coords[1], coords[2], coords[3]);
				break;
			}
		}
		return path;
	}


}
