package com.physics.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

import com.physics.model.Ball;
import com.physics.model.Particle;

public class Teleport {
    private boolean isActive = false;
    private final Random random = new Random();
    private Point2D.Float teleportPoint;
    private ArrayList<TeleportParticle> particles = new ArrayList<>();
    
    private static final int PARTICLE_COUNT = 50;
    private static final float TELEPORT_RADIUS = 100f;
    
    private class TeleportParticle {
        float x, y;
        float vx, vy;
        float alpha = 1.0f;
        Color color;
        
        TeleportParticle(float x, float y) {
            this.x = x;
            this.y = y;
            float angle = random.nextFloat() * (float)(Math.PI * 2);
            float speed = 1f + random.nextFloat() * 2f;
            vx = (float)Math.cos(angle) * speed;
            vy = (float)Math.sin(angle) * speed;
            color = new Color(100, 200, 255);
        }
        
        void update() {
            x += vx;
            y += vy;
            alpha *= 0.95f;
        }
        
        void draw(Graphics2D g2d) {
            if (alpha < 0.05f) return;
            
            Color particleColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int)(alpha * 255)
            );
            g2d.setColor(particleColor);
            g2d.fillOval((int)x - 2, (int)y - 2, 4, 4);
        }
    }
    
    public void setActive(boolean active, float x, float y) {
        if (active && !isActive) {
            teleportPoint = new Point2D.Float(x, y);
            initializeParticles();
        }
        isActive = active;
    }
    
    private void initializeParticles() {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new TeleportParticle(
                teleportPoint.x,
                teleportPoint.y
            ));
        }
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void update() {
        if (!isActive) return;
        
        particles.removeIf(p -> p.alpha < 0.05f);
        for (TeleportParticle particle : particles) {
            particle.update();
        }
        
        if (particles.isEmpty()) {
            isActive = false;
        }
    }
    
    public void draw(Graphics2D g2d) {
        if (!isActive || teleportPoint == null) return;
        
        // Рисуем портал
        Point2D center = new Point2D.Float(teleportPoint.x, teleportPoint.y);
        float[] dist = {0.0f, 0.7f, 1.0f};
        Color[] colors = {
            new Color(100, 200, 255, 150),
            new Color(100, 200, 255, 50),
            new Color(100, 200, 255, 0)
        };
        RadialGradientPaint paint = new RadialGradientPaint(
            center, TELEPORT_RADIUS, dist, colors
        );
        g2d.setPaint(paint);
        g2d.fillOval(
            (int)(teleportPoint.x - TELEPORT_RADIUS),
            (int)(teleportPoint.y - TELEPORT_RADIUS),
            (int)(TELEPORT_RADIUS * 2),
            (int)(TELEPORT_RADIUS * 2)
        );
        
        // Рисуем частицы
        for (TeleportParticle particle : particles) {
            particle.draw(g2d);
        }
    }
    
    public void applyEffect(ArrayList<Ball> balls, ArrayList<Particle> particles) {
        if (!isActive || teleportPoint == null) return;
        
        // Телепортируем шары
        for (Ball ball : balls) {
            float dx = ball.getX() - teleportPoint.x;
            float dy = ball.getY() - teleportPoint.y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist < TELEPORT_RADIUS) {
                // Телепортируем в случайную точку в радиусе
                float angle = random.nextFloat() * (float)(Math.PI * 2);
                float radius = random.nextFloat() * TELEPORT_RADIUS * 2;
                ball.setPosition(
                    teleportPoint.x + (float)Math.cos(angle) * radius,
                    teleportPoint.y + (float)Math.sin(angle) * radius
                );
                
                // Добавляем случайный импульс
                angle = random.nextFloat() * (float)(Math.PI * 2);
                float force = 5f + random.nextFloat() * 5f;
                ball.addForce(
                    (float)Math.cos(angle) * force,
                    (float)Math.sin(angle) * force
                );
            }
        }
        
        // Телепортируем частицы
        for (Particle particle : particles) {
            float dx = particle.getX() - teleportPoint.x;
            float dy = particle.getY() - teleportPoint.y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist < TELEPORT_RADIUS) {
                float angle = random.nextFloat() * (float)(Math.PI * 2);
                float radius = random.nextFloat() * TELEPORT_RADIUS * 2;
                particle.setPosition(
                    teleportPoint.x + (float)Math.cos(angle) * radius,
                    teleportPoint.y + (float)Math.sin(angle) * radius
                );
                
                angle = random.nextFloat() * (float)(Math.PI * 2);
                float force = 5f + random.nextFloat() * 5f;
                particle.addForce(
                    (float)Math.cos(angle) * force,
                    (float)Math.sin(angle) * force
                );
            }
        }
    }
} 