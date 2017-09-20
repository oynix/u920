package com.oy.u920.imageloader.shape;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

/**
 * 圆形图片工厂
 * @author chenbenbin
 * 
 */
public class RoundImageFactory implements IImageShapeFactory {

	@Override
	public Bitmap ps(Bitmap source) {
		int width = source.getWidth();
		int height = source.getHeight();
		int size = Math.min(width, height);
		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Paint p = new Paint();
		p.setAntiAlias(true);
		BitmapShader bitmapShader = new BitmapShader(source,
				Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		p.setShader(bitmapShader);
		c.drawCircle(size / 2, size / 2, size / 2, p);
		return b;
	}

}
