package com.physics.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class EffectIndicator {
    private static final int WIDTH = 140;
    private static final int HEIGHT = 25;

    public static void draw(Graphics2D g2d, String name, String key, boolean active, 
                          int x, int y, Point mousePosition, Point[] tooltipData) {
        // Проверяем, находится ли мышь над эффектом
        if (mousePosition != null) {
            Rectangle effectBounds = new Rectangle(x, y, WIDTH, HEIGHT);
            if (effectBounds.contains(mousePosition)) {
                // Рисуем подсветку при наведении
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(x, y, WIDTH, HEIGHT, 10, 10);
                
                // Устанавливаем позицию и текст подсказки
                tooltipData[0] = new Point(x + WIDTH + 10, y);
                tooltipData[1] = new Point(0, 0); // Используется для передачи текста через Constants
            }
        }

        // Фон с градиентом
        GradientPaint bgGradient = new GradientPaint(
            x, y, active ? new Color(0, 100, 0, 50) : new Color(100, 0, 0, 50),
            x + WIDTH, y, active ? new Color(0, 150, 0, 50) : new Color(150, 0, 0, 50)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(x, y, WIDTH, HEIGHT, 10, 10);
        
        // Рамка
        g2d.setColor(active ? new Color(0, 200, 0, 100) : new Color(200, 0, 0, 100));
        g2d.drawRoundRect(x, y, WIDTH, HEIGHT, 10, 10);
        
        // Текст и клавиша
        g2d.setColor(active ? new Color(0, 100, 0) : new Color(100, 0, 0));
        g2d.drawString(name + " [" + key + "]", x + 5, y + 17);
        
        // Индикатор
        g2d.setColor(active ? new Color(0, 255, 0) : new Color(255, 0, 0));
        g2d.fillOval(x + WIDTH - 15, y + 7, 10, 10);
    }
} 