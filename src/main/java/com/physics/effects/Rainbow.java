package com.physics.effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import com.physics.model.Ball;

public class Rainbow {
    private final ExecutorService executor = EffectUtils.getExecutor();
    private boolean isActive = false;
    private final Random random = new Random();
    private double phase = 0.0;
    private List<RainbowParticle> particles = new ArrayList<>();
    private List<AmorphousCircle> circles = new ArrayList<>();
    
    private BufferedImage particleBuffer;
    private BufferedImage circleBuffer;
    
    private static final float PHASE_SPEED = 0.01f;
    private static final int PARTICLE_COUNT = 150;
    private static final int CIRCLE_COUNT = 5;
    private static final float MIN_CIRCLE_SIZE = 50f;
    private static final float MAX_CIRCLE_SIZE = 300f;
    
    private int width, height;
    
    private class RainbowParticle {
        float x, y;
        float vx, vy;
        float size;
        float hue;
        float alpha;
        
        RainbowParticle() {
            reset();
        }
        
        void reset() {
            x = random.nextFloat() * width;
            y = random.nextFloat() * height;
            float angle = random.nextFloat() * (float)(Math.PI * 2);
            float speed = 1f + random.nextFloat() * 2f;
            vx = (float)Math.cos(angle) * speed;
            vy = (float)Math.sin(angle) * speed;
            size = 5f + random.nextFloat() * 15f;
            hue = random.nextFloat();
            alpha = 0.4f + random.nextFloat() * 0.4f;
        }
        
        void update() {
            x += vx;
            y += vy;
            hue = (hue + 0.005f) % 1.0f;
            
            // Отражение от границ
            if (x < 0) { x = 0; vx *= -0.8f; }
            if (x > width) { x = width; vx *= -0.8f; }
            if (y < 0) { y = 0; vy *= -0.8f; }
            if (y > height) { y = height; vy *= -0.8f; }
            
            // Случайные изменения движения
            if (random.nextFloat() < 0.02f) {
                float angle = random.nextFloat() * (float)(Math.PI * 2);
                float force = 0.5f;
                vx += Math.cos(angle) * force;
                vy += Math.sin(angle) * force;
            }
            
            // Ограничение скорости
            float speed = (float)Math.sqrt(vx * vx + vy * vy);
            if (speed > 3) {
                vx = vx / speed * 3;
                vy = vy / speed * 3;
            }
        }
        
        void draw(Graphics2D g2d) {
            Color color = Color.getHSBColor(hue, 0.8f, 1.0f);
            Point2D center = new Point2D.Float(x, y);
            float[] dist = {0.0f, 0.5f, 1.0f};
            Color[] colors = {
                new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * alpha)),
                new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(127 * alpha)),
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)
            };
            
            RadialGradientPaint paint = new RadialGradientPaint(
                center, size * 2, dist, colors
            );
            
            g2d.setPaint(paint);
            g2d.fillOval(
                (int)(x - size), 
                (int)(y - size),
                (int)(size * 2),
                (int)(size * 2)
            );
        }
    }
    
    private class AmorphousCircle {
        float centerX, centerY;
        float targetX, targetY;
        float currentRadius;
        float targetRadius;
        float hue;
        float alpha;
        
        AmorphousCircle() {
            reset();
        }
        
        void reset() {
            centerX = random.nextFloat() * width;
            centerY = random.nextFloat() * height;
            targetX = random.nextFloat() * width;
            targetY = random.nextFloat() * height;
            currentRadius = MIN_CIRCLE_SIZE + random.nextFloat() * (MAX_CIRCLE_SIZE - MIN_CIRCLE_SIZE);
            targetRadius = MIN_CIRCLE_SIZE + random.nextFloat() * (MAX_CIRCLE_SIZE - MIN_CIRCLE_SIZE);
            hue = random.nextFloat();
            alpha = 0.2f + random.nextFloat() * 0.3f;
        }
        
        void update() {
            // Плавное движение к целевой точке
            float dx = targetX - centerX;
            float dy = targetY - centerY;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist < 1) {
                targetX = random.nextFloat() * width;
                targetY = random.nextFloat() * height;
            } else {
                centerX += dx * 0.02f;
                centerY += dy * 0.02f;
            }
            
            // Плавное изменение радиуса
            float dr = targetRadius - currentRadius;
            if (Math.abs(dr) < 1) {
                targetRadius = MIN_CIRCLE_SIZE + random.nextFloat() * (MAX_CIRCLE_SIZE - MIN_CIRCLE_SIZE);
            } else {
                currentRadius += dr * 0.02f;
            }
            
            // Изменение цвета
            hue = (hue + 0.002f) % 1.0f;
        }
        
        void draw(Graphics2D g2d) {
            Color baseColor = Color.getHSBColor(hue, 0.8f, 1.0f);
            Point2D center = new Point2D.Float(centerX, centerY);
            float[] dist = {0.0f, 0.7f, 1.0f};
            Color[] colors = {
                new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int)(255 * alpha)),
                new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int)(127 * alpha)),
                new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0)
            };
            
            RadialGradientPaint paint = new RadialGradientPaint(
                center, currentRadius, dist, colors
            );
            
            g2d.setPaint(paint);
            g2d.fillOval(
                (int)(centerX - currentRadius),
                (int)(centerY - currentRadius),
                (int)(currentRadius * 2),
                (int)(currentRadius * 2)
            );
        }
    }
    
    public Rainbow() {
        initializeParticles();
        initializeCircles();
    }
    
    private void initializeParticles() {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new RainbowParticle());
        }
    }
    
    private void initializeCircles() {
        circles.clear();
        for (int i = 0; i < CIRCLE_COUNT; i++) {
            circles.add(new AmorphousCircle());
        }
    }
    
    private void ensureBufferSize(int width, int height) {
        if (particleBuffer == null || particleBuffer.getWidth() != width || particleBuffer.getHeight() != height) {
            particleBuffer = EffectUtils.createCompatibleImage(width, height, true);
            circleBuffer = EffectUtils.createCompatibleImage(width, height, true);
        }
    }
    
    public void setActive(boolean active) {
        if (active && !isActive) {
            initializeParticles();
            initializeCircles();
        }
        isActive = active;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void update() {
        if (!isActive) return;
        
        phase += PHASE_SPEED;
        
        for (RainbowParticle particle : particles) {
            particle.update();
        }
        
        for (AmorphousCircle circle : circles) {
            circle.update();
        }
    }
    
    public void draw(Graphics2D g2d, int width, int height) {
        if (!isActive) return;
        
        this.width = width;
        this.height = height;
        
        ensureBufferSize(width, height);
        
        // Рисуем круги
        Graphics2D circleG2d = circleBuffer.createGraphics();
        try {
            circleG2d.setComposite(AlphaComposite.Clear);
            circleG2d.fillRect(0, 0, width, height);
            circleG2d.setComposite(AlphaComposite.SrcOver);
            circleG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            for (AmorphousCircle circle : circles) {
                circle.draw(circleG2d);
            }
        } finally {
            circleG2d.dispose();
        }
        
        // Рисуем частицы
        Graphics2D particleG2d = particleBuffer.createGraphics();
        try {
            particleG2d.setComposite(AlphaComposite.Clear);
            particleG2d.fillRect(0, 0, width, height);
            particleG2d.setComposite(AlphaComposite.SrcOver);
            particleG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            for (RainbowParticle particle : particles) {
                particle.draw(particleG2d);
            }
        } finally {
            particleG2d.dispose();
        }
        
        // Отрисовка с наложением
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g2d.drawImage(circleBuffer, 0, 0, null);
        g2d.drawImage(particleBuffer, 0, 0, null);
    }
    
    public void applyEffect(List<Ball> balls) {
        if (!isActive) return;
        
        float globalHue = (float)(phase % 1.0);
        for (Ball ball : balls) {
            float hue = (globalHue + (float)(ball.getX() + ball.getY()) / 1000.0f) % 1.0f;
            Color color = Color.getHSBColor(hue, 0.8f, 1.0f);
            ball.setColor(color);
            
            if (random.nextFloat() < 0.05f) {
                double angle = random.nextDouble() * Math.PI * 2;
                double force = 0.5;
                ball.addForce((float) (Math.cos(angle) * force), (float) (Math.sin(angle) * force));
            }
        }
    }
} 