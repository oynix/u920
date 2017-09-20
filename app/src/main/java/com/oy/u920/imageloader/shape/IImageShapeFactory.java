package com.oy.u920.imageloader.shape;

import android.graphics.Bitmap;

/**
 * 图片形状接口
 * @author chenbenbin
 * 
 */
public interface IImageShapeFactory {
	/**
	 * 构建新的图片
	 */
	public Bitmap ps(Bitmap source);

}
