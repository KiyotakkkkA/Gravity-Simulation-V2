package com.physics.model;

import java.awt.Color;
import java.awt.Graphics2D;

public class Particle {
    private float x, y;
    private float velocityX, velocityY;
    private int lifetime;
    private Color color;
    private float size;

    public Particle(float x, float y, float vx, float vy, int lifetime, Color color, float size) {
        this.x = x;
        this.y = y;
        this.velocityX = vx;
        this.velocityY = vy;
        this.lifetime = lifetime;
        this.color = color;
        this.size = size;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getVelocityX() { return velocityX; }
    public float getVelocityY() { return velocityY; }
    public int getLifetime() { return lifetime; }
    public Color getColor() { return color; }
    public float getSize() { return size; }
    public float getWidth() { return size; }
    public float getHeight() { return size; }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setVelocityX(float vx) { this.velocityX = vx; }
    public void setVelocityY(float vy) { this.velocityY = vy; }
    public void setLifetime(int lifetime) { this.lifetime = lifetime; }
    public void setColor(Color color) { this.color = color; }
    public void setSize(float size) { this.size = size; }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void addForce(float fx, float fy) {
        velocityX += fx;
        velocityY += fy;
    }

    public void update(int width, int height, double energyField, double gravity) {
        velocityY += gravity;
        x += velocityX;
        y += velocityY;
        lifetime -= 1;

        if (x < 0 || x > width || y < 0 || y > height) {
            lifetime = 0;
        }
    }

    public void draw(Graphics2D g2d) {
        if (lifetime <= 0) return;
        
        Color particleColor = new Color(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            (int)(255 * (lifetime / 100.0))
        );
        
        g2d.setColor(particleColor);
        int drawX = (int)(x - size/2);
        int drawY = (int)(y - size/2);
        g2d.fillOval(drawX, drawY, (int)size, (int)size);
    }
} 