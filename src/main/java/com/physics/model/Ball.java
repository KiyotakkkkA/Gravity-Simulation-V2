package com.physics.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Ball {
    private float x, y;
    private float velocityX, velocityY;
    private float radius;
    private Color color;
    private double width;
    private double height;

    public Ball(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.velocityX = 0;
        this.velocityY = 0;
        this.color = new Color(200, 100, 100);
        this.width = radius * 2;
        this.height = radius * 2;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getVelocityX() { return velocityX; }
    public float getVelocityY() { return velocityY; }
    public float getRadius() { return radius; }
    public Color getColor() { return color; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setVelocityX(float vx) { this.velocityX = vx; }
    public void setVelocityY(float vy) { this.velocityY = vy; }
    public void setColor(Color color) { this.color = color; }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void addForce(float fx, float fy) {
        velocityX += fx;
        velocityY += fy;
    }

    public boolean update(int width, int height, double energyField, double gravity) {
        velocityY += gravity;
        x += velocityX;
        y += velocityY;

        if (x - radius < 0) {
            x = radius;
            velocityX = -velocityX * (float)energyField;
        } else if (x + radius > width) {
            x = width - radius;
            velocityX = -velocityX * (float)energyField;
        }

        if (y - radius < 0) {
            y = radius;
            velocityY = -velocityY * (float)energyField;
        } else if (y + radius > height) {
            y = height - radius;
            velocityY = -velocityY * (float)energyField;
            return Math.abs(velocityY) > 15.0;
        }
        
        return false;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        int drawX = (int)(x - radius);
        int drawY = (int)(y - radius);
        int size = (int)(radius * 2);
        g2d.fillOval(drawX, drawY, size, size);
    }

    public ArrayList<Particle> createParticles() {
        ArrayList<Particle> particles = new ArrayList<>();
        int particleCount = 20;
        
        for (int i = 0; i < particleCount; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 2 + Math.random() * 5;
            double px = Math.cos(angle) * speed;
            double py = Math.sin(angle) * speed;
            
            particles.add(new Particle(x, y, (float)px, (float)py, 50, color, 4));
        }
        return particles;
    }
} 