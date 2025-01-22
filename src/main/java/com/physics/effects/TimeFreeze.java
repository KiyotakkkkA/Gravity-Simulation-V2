package com.physics.effects;

import java.awt.Color;
import java.awt.Graphics2D;

public class TimeFreeze {
    private boolean isActive = false;

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }

    public void draw(Graphics2D g2d, int width, int height) {
        if (!isActive) return;

        // Создаем эффект замороженного времени
        g2d.setColor(new Color(200, 200, 255, 30));
        for (int i = 0; i < 20; i++) {
            int x = (int)(Math.random() * width);
            int y = (int)(Math.random() * height);
            int size = 2 + (int)(Math.random() * 4);
            g2d.fillRect(x, y, size, size);
        }
    }

    public boolean shouldUpdatePhysics() {
        return !isActive;
    }
} 