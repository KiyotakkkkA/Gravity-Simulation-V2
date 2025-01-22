package com.physics.effects;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.physics.model.Ball;
import com.physics.model.Particle;

public class GravityWave {
    private boolean isActive = false;
    private double phase = 0.0;
    private BufferedImage waveBuffer;
    
    private final List<WaveRing> rings = new ArrayList<>();
    private static final float PHASE_SPEED = 0.05f;
    private static final float WAVE_SPEED = 4.0f;
    private static final float MAX_RADIUS = 800.0f;
    private static final float INITIAL_RADIUS = 50.0f;
    private static final float FORCE_MULTIPLIER = 15.0f;
    
    private class WaveRing {
        float radius;
        float strength;
        float initialPhase;
        
        WaveRing() {
            this.radius = INITIAL_RADIUS;
            this.strength = 1.0f;
            this.initialPhase = (float)phase;
        }
        
        void update() {
            radius += WAVE_SPEED;
            // Сила уменьшается с расстоянием
            strength = 1.0f - (radius / MAX_RADIUS);
            if (strength < 0) strength = 0;
            
            // Добавляем пульсацию
            strength *= 0.5f + 0.5f * Math.sin(phase * 5 + initialPhase);
        }
        
        void draw(Graphics2D g2d, int width, int height) {
            float centerX = width / 2.0f;
            float centerY = height / 2.0f;
            
            // Рисуем кольцо волны
            float alpha = strength * 0.8f;
            Color waveColor = Color.getHSBColor(0.6f + (float)Math.sin(phase) * 0.1f, 0.8f, 1.0f);
            
            // Градиентная заливка для кольца
            RadialGradientPaint paint = new RadialGradientPaint(
                new Point2D.Float(centerX, centerY),
                radius,
                new float[]{0.8f, 0.9f, 1.0f},
                new Color[]{
                    new Color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), 0),
                    new Color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), (int)(255 * alpha)),
                    new Color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), 0)
                }
            );
            
            g2d.setPaint(paint);
            g2d.fillOval(
                (int)(centerX - radius),
                (int)(centerY - radius),
                (int)(radius * 2),
                (int)(radius * 2)
            );
            
            // Рисуем волновые искажения
            g2d.setStroke(new BasicStroke(2.0f));
            Path2D.Float path = new Path2D.Float();
            int points = 60;
            
            for (int i = 0; i <= points; i++) {
                float angle = (float)(i * Math.PI * 2 / points);
                float wave = (float)(Math.sin(angle * 8 + phase * 5) * 10);
                float r = radius + wave;
                
                float x = centerX + (float)(Math.cos(angle) * r);
                float y = centerY + (float)(Math.sin(angle) * r);
                
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }
            
            path.closePath();
            g2d.setColor(new Color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), 
                                 (int)(100 * alpha)));
            g2d.draw(path);
        }
        
        void applyForce(Ball ball, int width, int height) {
            float centerX = width / 2.0f;
            float centerY = height / 2.0f;
            
            double dx = ball.getX() - centerX;
            double dy = ball.getY() - centerY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            // Применяем силу только в области кольца
            float wavePeak = radius;
            float waveWidth = 100.0f;
            
            if (Math.abs(dist - wavePeak) < waveWidth) {
                // Сила зависит от близости к пику волны
                float forceFactor = 1.0f - Math.abs((float)dist - wavePeak) / waveWidth;
                forceFactor *= strength;
                
                // Направление силы - от центра
                double angle = Math.atan2(dy, dx);
                // Добавляем колебания к углу для создания турбулентности
                angle += Math.sin(phase * 3) * 0.2;
                
                double forceX = Math.cos(angle) * forceFactor * FORCE_MULTIPLIER;
                double forceY = Math.sin(angle) * forceFactor * FORCE_MULTIPLIER;
                
                ball.addForce((float) forceX, (float) forceY);
            }
        }
        
        void applyForce(Particle particle, int width, int height) {
            float centerX = width / 2.0f;
            float centerY = height / 2.0f;
            
            double dx = particle.getX() - centerX;
            double dy = particle.getY() - centerY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            float wavePeak = radius;
            float waveWidth = 100.0f;
            
            if (Math.abs(dist - wavePeak) < waveWidth) {
                float forceFactor = 1.0f - Math.abs((float)dist - wavePeak) / waveWidth;
                forceFactor *= strength * 0.5f; // Меньшая сила для частиц
                
                double angle = Math.atan2(dy, dx);
                angle += Math.sin(phase * 3) * 0.2;
                
                double forceX = Math.cos(angle) * forceFactor * FORCE_MULTIPLIER;
                double forceY = Math.sin(angle) * forceFactor * FORCE_MULTIPLIER;
                
                particle.addForce((float) forceX, (float) forceY);
            }
        }
    }
    
    public void setActive(boolean active, int x, int y) {
        if (active && !isActive) {
            rings.clear();
            addWave();
        }
        isActive = active;
    }
    
    private void addWave() {
        rings.add(new WaveRing());
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void update() {
        if (!isActive) return;
        
        phase += PHASE_SPEED;
        
        // Удаляем затухшие волны
        rings.removeIf(ring -> ring.strength <= 0);
        
        // Обновляем существующие волны
        for (WaveRing ring : rings) {
            ring.update();
        }
        
        // Добавляем новые волны с определенной частотой
        if (isActive && rings.isEmpty() || 
            (!rings.isEmpty() && rings.get(rings.size()-1).radius > MAX_RADIUS * 0.3f)) {
            addWave();
        }
    }
    
    public void draw(Graphics2D g2d, int width, int height) {
        if (!isActive || rings.isEmpty()) return;
        
        if (waveBuffer == null || waveBuffer.getWidth() != width || waveBuffer.getHeight() != height) {
            waveBuffer = EffectUtils.createCompatibleImage(width, height, true);
        }
        
        Graphics2D bufferG2d = waveBuffer.createGraphics();
        try {
            bufferG2d.setComposite(AlphaComposite.Clear);
            bufferG2d.fillRect(0, 0, width, height);
            bufferG2d.setComposite(AlphaComposite.SrcOver);
            bufferG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            for (WaveRing ring : rings) {
                ring.draw(bufferG2d, width, height);
            }
        } finally {
            bufferG2d.dispose();
        }
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g2d.drawImage(waveBuffer, 0, 0, null);
    }
    
    public void applyEffect(ArrayList<Ball> balls, ArrayList<Particle> particles) {
        if (!isActive || rings.isEmpty()) return;
        
        // Используем фиксированные размеры для эффекта
        int width = 800;  // значение по умолчанию
        int height = 600; // значение по умолчанию
        
        for (WaveRing ring : rings) {
            for (Ball ball : balls) {
                ring.applyForce(ball, width, height);
            }
            for (Particle particle : particles) {
                ring.applyForce(particle, width, height);
            }
        }
    }
} 