package com.physics.effects;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.physics.model.Ball;

public class QuantumTunnel {
    private boolean isActive = false;
    private double phase = 0.0;
    private static final Random random = new Random();
    private final ArrayList<QuantumParticle> particles = new ArrayList<>();
    private final Map<Ball, TunnelState> tunnelStates = new HashMap<>();
    
    private static final int PARTICLE_COUNT = 100;
    private static final double PHASE_SPEED = 0.03;
    private static final int TUNNEL_WIDTH = 100;
    private static final double TUNNEL_PROBABILITY = 0.1;
    
    private class QuantumParticle {
        double x, y;
        double vx, vy;
        float hue;
        float alpha;
        float size;
        
        QuantumParticle(double x, double y) {
            this.x = x;
            this.y = y;
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 1 + random.nextDouble() * 2;
            vx = Math.cos(angle) * speed;
            vy = Math.sin(angle) * speed;
            hue = random.nextFloat();
            alpha = 0.3f + random.nextFloat() * 0.4f;
            size = 2 + random.nextFloat() * 3;
        }
        
        void update() {
            x += vx;
            y += vy;
            
            // Плавно меняем цвет
            hue = (hue + 0.02f) % 1.0f;
            
            // Пульсация прозрачности и размера
            alpha = 0.3f + (float)(Math.sin(phase * 2 + x * 0.01) + 1) * 0.3f;
            size = 2 + (float)(Math.sin(phase * 3 + y * 0.01) + 1) * 2;
        }
    }
    
    private class TunnelState {
        double startX, startY;
        double endX, endY;
        double progress;
        boolean isTunneling;
        List<QuantumParticle> tunnelParticles;
        
        TunnelState(double sx, double sy, double ex, double ey) {
            startX = sx;
            startY = sy;
            endX = ex;
            endY = ey;
            progress = 0.0;
            isTunneling = true;
            tunnelParticles = new ArrayList<>();
            
            // Создаем частицы вдоль пути туннелирования
            int particleCount = 20;
            for (int i = 0; i < particleCount; i++) {
                double t = i / (double)(particleCount - 1);
                double x = startX + (endX - startX) * t;
                double y = startY + (endY - startY) * t;
                tunnelParticles.add(new QuantumParticle(x, y));
            }
        }
        
        void update() {
            if (!isTunneling) return;
            
            progress += 0.02;
            if (progress >= 1.0) {
                isTunneling = false;
                return;
            }
            
            // Обновляем частицы
            for (QuantumParticle particle : tunnelParticles) {
                particle.update();
            }
        }
    }
    
    public void setActive(boolean active) {
        isActive = active;
        if (!active) {
            tunnelStates.clear();
        }
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void update() {
        if (!isActive) return;
        
        phase += PHASE_SPEED;
        
        // Обновляем состояния туннелирования
        tunnelStates.values().forEach(TunnelState::update);
        
        // Удаляем завершенные состояния
        tunnelStates.entrySet().removeIf(entry -> !entry.getValue().isTunneling);
    }
    
    public void draw(Graphics2D g2d, int width, int height) {
        if (!isActive) return;
        
        // Рисуем все активные туннели
        for (TunnelState state : tunnelStates.values()) {
            if (!state.isTunneling) continue;
            
            // Рисуем путь туннелирования
            drawTunnelPath(g2d, state);
            
            // Рисуем квантовые частицы
            drawTunnelParticles(g2d, state);
            
            // Рисуем порталы на концах туннеля
            drawTunnelPortals(g2d, state);
        }
    }
    
    private void drawTunnelPath(Graphics2D g2d, TunnelState state) {
        // Создаем градиент для пути туннелирования
        Point2D start = new Point2D.Float((float)state.startX, (float)state.startY);
        Point2D end = new Point2D.Float((float)state.endX, (float)state.endY);
        
        float[] fractions = {0.0f, 0.5f, 1.0f};
        Color[] colors = {
            new Color(100, 200, 255, 50),
            new Color(150, 100, 255, 100),
            new Color(100, 200, 255, 50)
        };
        
        LinearGradientPaint gradient = new LinearGradientPaint(
            start, end, fractions, colors, CycleMethod.NO_CYCLE
        );
        
        // Рисуем основной путь
        g2d.setStroke(new BasicStroke(
            TUNNEL_WIDTH * 0.5f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        ));
        g2d.setPaint(gradient);
        g2d.drawLine(
            (int)state.startX,
            (int)state.startY,
            (int)state.endX,
            (int)state.endY
        );
        
        // Рисуем волновые линии вдоль пути
        drawWaveLines(g2d, state);
    }
    
    private void drawWaveLines(Graphics2D g2d, TunnelState state) {
        double dx = state.endX - state.startX;
        double dy = state.endY - state.startY;
        double length = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);
        
        // Рисуем несколько волновых линий
        for (int i = 0; i < 3; i++) {
            Path2D path = new Path2D.Double();
            double offset = (i - 1) * TUNNEL_WIDTH * 0.2;
            
            for (double t = 0; t <= 1; t += 0.01) {
                double x = state.startX + dx * t;
                double y = state.startY + dy * t;
                
                // Добавляем волновое смещение
                double wave = Math.sin(t * 10 + phase * 2) * 
                            Math.sin(t * Math.PI) * TUNNEL_WIDTH * 0.15;
                x += Math.sin(angle) * (wave + offset);
                y -= Math.cos(angle) * (wave + offset);
                
                if (t == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.setColor(new Color(150, 200, 255, 50));
            g2d.draw(path);
        }
    }
    
    private void drawTunnelParticles(Graphics2D g2d, TunnelState state) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        
        for (QuantumParticle particle : state.tunnelParticles) {
            // Создаем цвет частицы
            Color particleColor = Color.getHSBColor(particle.hue, 0.8f, 1.0f);
            
            // Рисуем свечение
            g2d.setColor(new Color(
                particleColor.getRed(),
                particleColor.getGreen(),
                particleColor.getBlue(),
                (int)(100 * particle.alpha)
            ));
            
            float glowSize = particle.size * 3;
            g2d.fillOval(
                (int)(particle.x - glowSize/2),
                (int)(particle.y - glowSize/2),
                (int)glowSize,
                (int)glowSize
            );
            
            // Рисуем ядро частицы
            g2d.setColor(new Color(
                particleColor.getRed(),
                particleColor.getGreen(),
                particleColor.getBlue(),
                (int)(255 * particle.alpha)
            ));
            
            g2d.fillOval(
                (int)(particle.x - particle.size/2),
                (int)(particle.y - particle.size/2),
                (int)particle.size,
                (int)particle.size
            );
        }
    }
    
    private void drawTunnelPortals(Graphics2D g2d, TunnelState state) {
        // Рисуем порталы на обоих концах туннеля
        drawPortal(g2d, state.startX, state.startY, state.progress);
        drawPortal(g2d, state.endX, state.endY, 1.0 - state.progress);
    }
    
    private void drawPortal(Graphics2D g2d, double x, double y, double intensity) {
        // Создаем градиент для портала
        Point2D center = new Point2D.Float((float)x, (float)y);
        float radius = TUNNEL_WIDTH * 0.5f;
        
        float[] fractions = {0.0f, 0.5f, 1.0f};
        Color[] colors = {
            new Color(255, 255, 255, (int)(200 * intensity)),
            new Color(100, 200, 255, (int)(100 * intensity)),
            new Color(0, 100, 255, 0)
        };
        
        RadialGradientPaint gradient = new RadialGradientPaint(
            center, radius, fractions, colors, CycleMethod.NO_CYCLE
        );
        
        g2d.setPaint(gradient);
        g2d.fillOval(
            (int)(x - radius),
            (int)(y - radius),
            (int)(radius * 2),
            (int)(radius * 2)
        );
        
        // Добавляем кольца вокруг портала
        g2d.setStroke(new BasicStroke(2.0f));
        for (int i = 0; i < 3; i++) {
            float ringRadius = radius * (1.0f + i * 0.2f);
            float alpha = (float)(0.5f * intensity * (1.0f - i * 0.2f));
            g2d.setColor(new Color(100, 200, 255, (int)(255 * alpha)));
            g2d.drawOval(
                (int)(x - ringRadius),
                (int)(y - ringRadius),
                (int)(ringRadius * 2),
                (int)(ringRadius * 2)
            );
        }
    }
    
    public void checkTunneling(Ball ball, int width, int height) {
        if (!isActive || tunnelStates.containsKey(ball)) return;
        
        // Проверяем возможность туннелирования
        if (random.nextDouble() < TUNNEL_PROBABILITY) {
            // Определяем точку назначения
            double endX = random.nextDouble() * width;
            double endY = random.nextDouble() * height;
            
            // Создаем новое состояние туннелирования
            tunnelStates.put(ball, new TunnelState(
                ball.getX(),
                ball.getY(),
                endX,
                endY
            ));
        }
    }
    
    public void applyTunneling(Ball ball) {
        if (!isActive) return;
        
        TunnelState state = tunnelStates.get(ball);
        if (state == null || !state.isTunneling) return;
        
        // Перемещаем шар вдоль пути туннелирования
        double x = state.startX + (state.endX - state.startX) * state.progress;
        double y = state.startY + (state.endY - state.startY) * state.progress;
        
        // Здесь нужно добавить метод teleport в класс Ball
        // ball.teleport(x, y);
        
        // Временное решение - напрямую изменяем координаты
        ball.setPosition((float) x, (float) y);
    }
} 