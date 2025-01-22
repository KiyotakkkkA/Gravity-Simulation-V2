package com.physics.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

import com.physics.model.Ball;
import com.physics.model.Particle;

public class SlowMotion {
    private boolean isActive = false;
    private final Random random = new Random();
    private Point2D.Float slowField;
    private ArrayList<SlowParticle> particles = new ArrayList<>();
    
    private static final int PARTICLE_COUNT = 100;
    private static final float FIELD_RADIUS = 150f;
    private static final float SLOW_FACTOR = 0.2f;
    
    private class SlowParticle {
        float x, y;
        float vx, vy;
        float alpha = 1.0f;
        Color color;
        
        SlowParticle() {
            reset();
        }
        
        void reset() {
            if (slowField == null) return;
            
            float angle = random.nextFloat() * (float)(Math.PI * 2);
            float radius = random.nextFloat() * FIELD_RADIUS;
            x = slowField.x + (float)Math.cos(angle) * radius;
            y = slowField.y + (float)Math.sin(angle) * radius;
            
            float speed = 0.5f + random.nextFloat();
            vx = (float)Math.cos(angle) * speed;
            vy = (float)Math.sin(angle) * speed;
            
            alpha = 0.3f + random.nextFloat() * 0.7f;
            color = new Color(200, 200, 255);
        }
        
        void update() {
            if (slowField == null) return;
            
            x += vx * 0.2f;
            y += vy * 0.2f;
            
            float dx = x - slowField.x;
            float dy = y - slowField.y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist > FIELD_RADIUS || alpha < 0.1f) {
                reset();
            } else {
                alpha *= 0.99f;
            }
        }
        
        void draw(Graphics2D g2d) {
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
            slowField = new Point2D.Float(x, y);
            initializeParticles();
        }
        isActive = active;
    }
    
    private void initializeParticles() {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new SlowParticle());
        }
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void update() {
        if (!isActive) return;
        
        for (SlowParticle particle : particles) {
            particle.update();
        }
    }
    
    public void draw(Graphics2D g2d) {
        if (!isActive || slowField == null) return;
        
        // Рисуем поле замедления
        Point2D center = new Point2D.Float(slowField.x, slowField.y);
        float[] dist = {0.0f, 0.7f, 1.0f};
        Color[] colors = {
            new Color(200, 200, 255, 100),
            new Color(200, 200, 255, 30),
            new Color(200, 200, 255, 0)
        };
        RadialGradientPaint paint = new RadialGradientPaint(
            center, FIELD_RADIUS, dist, colors
        );
        g2d.setPaint(paint);
        g2d.fillOval(
            (int)(slowField.x - FIELD_RADIUS),
            (int)(slowField.y - FIELD_RADIUS),
            (int)(FIELD_RADIUS * 2),
            (int)(FIELD_RADIUS * 2)
        );
        
        // Рисуем частицы
        for (SlowParticle particle : particles) {
            particle.draw(g2d);
        }
    }
    
    public void applyEffect(ArrayList<Ball> balls, ArrayList<Particle> particles) {
        if (!isActive || slowField == null) return;
        
        // Замедляем шары
        for (Ball ball : balls) {
            float dx = ball.getX() - slowField.x;
            float dy = ball.getY() - slowField.y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist < FIELD_RADIUS) {
                float factor = 1.0f - (1.0f - dist / FIELD_RADIUS) * (1.0f - SLOW_FACTOR);
                ball.setVelocityX(ball.getVelocityX() * factor);
                ball.setVelocityY(ball.getVelocityY() * factor);
            }
        }
        
        // Замедляем частицы
        for (Particle particle : particles) {
            float dx = particle.getX() - slowField.x;
            float dy = particle.getY() - slowField.y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist < FIELD_RADIUS) {
                float factor = 1.0f - (1.0f - dist / FIELD_RADIUS) * (1.0f - SLOW_FACTOR);
                particle.setVelocityX(particle.getVelocityX() * factor);
                particle.setVelocityY(particle.getVelocityY() * factor);
            }
        }
    }
} 