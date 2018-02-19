package org.icpclive.backend.graphics;

import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

/**
 * Created by Aksenov239 on 04.09.2016.
 */
public class GraphicsSWT extends AbstractGraphics {

    public static final float TEXT_VERTICAL_SHIFT = -0.03f;
    public static final double GLOBAL_OPACITY = .9;
    public Graphics2D g;

    public GraphicsSWT(Graphics2D g) {
        this.g = (Graphics2D) g.create();
    }

    @Override
    public AbstractGraphics create() {
        GraphicsSWT newG = new GraphicsSWT(g);
        newG.x0 = x0;
        newG.y0 = y0;
        return newG;
    }

    @Override
    public AbstractGraphics create(int x, int y, int width, int height) {
        GraphicsSWT g2 = new GraphicsSWT((Graphics2D) g.create(x + x0, y + y0, width, height));
        g2.width = width;
        g2.height = height;
        return g2;
    }

    @Override
    public void clear(int width, int height) {
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(0, 0, width, height);
    }

    @Override
    public void drawString(String text, int x, int y, Font font, Color color, double opacity) {
        g.setFont(font);
        setColor(color, opacity);
        g.drawString(text, x + x0, y + y0);
    }

    @Override
    public void drawRectWithText(String text, int x, int y, int width, int height, PlateStyle.Alignment alignment, Font font,
                                 PlateStyle plateStyle, double opacity, double textOpacity, double margin,
                                 boolean scale) {
        Graphics2D saved = g;
        x += x0;
        y += y0;
        g = (Graphics2D) saved.create();
        g.setFont(font);
        int textWidth = g.getFontMetrics().stringWidth(text);
        double textScale = 1;

        margin = height * margin;

        if (width == -1) {
            width = (int) (textWidth + 2 * margin);
            if (alignment == PlateStyle.Alignment.CENTER) {
                x -= width / 2;
            } else if (alignment == PlateStyle.Alignment.RIGHT) {
                x -= width;
            }
        } else if (scale) {
            int maxTextWidth = (int) (width - 2 * margin);
            if (textWidth > maxTextWidth) {
                textScale = 1.0 * maxTextWidth / textWidth;
            }
        }

        drawRect(x - x0, y - y0, width, height, plateStyle.background, opacity, plateStyle.rectangleType);

        setColor(plateStyle.text, textOpacity);

        FontMetrics wh = g.getFontMetrics();

        float yy = (float) (y + 1.0 * (height - wh.getStringBounds(text, g).getHeight()) / 2) + wh.getAscent()
                + TEXT_VERTICAL_SHIFT * height;

        float xx;
        if (alignment == PlateStyle.Alignment.LEFT) {
            xx = (float) (x + margin);
        } else if (alignment == PlateStyle.Alignment.CENTER) {
            xx = (float) (x + (width - textWidth * textScale) / 2);
        } else {
            xx = (float) (x + width - textWidth * textScale - margin);
        }
        AffineTransform transform = g.getTransform();
        transform.concatenate(AffineTransform.getTranslateInstance(xx, yy));
        transform.concatenate(AffineTransform.getScaleInstance(textScale, 1));
        g.setTransform(transform);
        g.drawString(text, 0, 0);
        g.dispose();
        g = saved;
    }

    @Override
    public void drawRect(int x, int y, int width, int height, Color color, double opacity, PlateStyle.RectangleType rectangleType) {
        setColor(color, opacity * GLOBAL_OPACITY);
        g.fillRect(x + x0, y + y0, width, height);
        //super.drawRect(x, y, width, height, color, opacity, rectangleType);
    }

    @Override
    public void clear(int x, int y, int width, int height) {
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(x + x0, y + y0, width, height);
    }

    @Override
    public void drawTextThatFits(String text, int x, int y, int width, int height, Font font, Color color, double margin) {
        Graphics2D saved = g;
        x += x0;
        y += y0;
        g = (Graphics2D) saved.create();
        g.setFont(font);
        g.setColor(color);
        FontMetrics wh = g.getFontMetrics();
        int textWidth = g.getFontMetrics().stringWidth(text);
        double textScale = 1;

        margin = height * margin;

        int maxTextWidth = (int) (width - 2 * margin);
        if (textWidth > maxTextWidth) {
            textScale = 1. * maxTextWidth / textWidth;
        }

        float yy = (float) y + wh.getAscent() - 0.03f * height;
        float xx = (float) (x + margin);

        AffineTransform transform = g.getTransform();
        transform.concatenate(AffineTransform.getTranslateInstance(xx, yy));
        transform.concatenate(AffineTransform.getScaleInstance(textScale, 1));
        g.setTransform(transform);
        g.drawString(text, 0, 0);
        g.dispose();
        g = saved;
    }

    @Override
    public void drawImage(Image image, int x, int y, int width, int height) {
        g.drawImage(image, x0 + x, y0 + y, width, height, null);
    }

    @Override
    public void drawImage(Image image, int x, int y, int width, int height, double opacity) {
        g.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
        g.drawImage(image, x0 + x, y0 + y, width, height, null);
        g.setComposite(AlphaComposite.SrcOver.derive(1f));
    }

    public void drawImage(BufferedImage image, int x, int y, double opacity) {
        RescaleOp rop = new RescaleOp(new float[]{1f, 1f, 1f, (float) opacity}, new float[4], null);
        g.drawImage(image, rop, x, y);
    }

    @Override
    public void fillPolygon(int[] x, int[] y, Color color, double opacity) {
        setColor(color, opacity * 0.99);
        int[] xx = new int[x.length];
        int[] yy = new int[y.length];
        for (int i = 0; i < x.length; i++) {
            xx[i] = x[i] + x0;
            yy[i] = y[i] + y0;
        }
        g.fillPolygon(xx, yy, xx.length);
    }

    @Override
    public Rectangle2D getStringBounds(String message, Font font) {
        g.setFont(font);
        return g.getFontMetrics().getStringBounds(message, g);
    }

    @Override
    public void clip(int x, int y, int width, int height) {
        g.clipRect(x + x0, y + y0, width, height);
    }

    @Override
    public void setColor(Color color) {
        g.setColor(color);
    }

    @Override
    public void setColor(Color color, double opacity) {
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * 255));
        g.setColor(color);
//        g.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
    }

    @Override
    public void dispose() {
        g.dispose();
    }
}
