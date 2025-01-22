package com.physics.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import com.physics.model.Ball;
import com.physics.model.Particle;

public class Split {
    private boolean isActive = false;
    private final Random random = new Random();
    private static final float MIN_RADIUS = 10f;
    private BufferedImage effectBuffer;
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void update() {
        // Обновление не требуется
    }
    
    public void draw(Graphics2D g2d, int width, int height) {
        // Визуальный эффект не требуется
    }
    
    public void applyEffect(ArrayList<Ball> balls, ArrayList<Particle> particles) {
        if (!isActive) return;
        
        ArrayList<Ball> newBalls = new ArrayList<>();
        
        for (Ball ball : balls) {
            if (ball.getRadius() > MIN_RADIUS) {
                // Создаем два новых шара меньшего размера
                float newRadius = ball.getRadius() / 1.4f;
                float offset = newRadius * 1.2f;
                
                // Первый шар
                Ball ball1 = new Ball(
                    ball.getX() - offset,
                    ball.getY(),
                    newRadius
                );
                ball1.setVelocityX(ball.getVelocityX() - 2f);
                ball1.setVelocityY(ball.getVelocityY());
                ball1.setColor(ball.getColor());
                
                // Второй шар
                Ball ball2 = new Ball(
                    ball.getX() + offset,
                    ball.getY(),
                    newRadius
                );
                ball2.setVelocityX(ball.getVelocityX() + 2f);
                ball2.setVelocityY(ball.getVelocityY());
                ball2.setColor(ball.getColor());
                
                newBalls.add(ball1);
                newBalls.add(ball2);
                
                // Создаем частицы для эффекта
                for (int i = 0; i < 10; i++) {
                    float particleAngle = random.nextFloat() * (float)(Math.PI * 2);
                    float speed = 2f + random.nextFloat() * 3f;
                    
                    Particle particle = new Particle(
                        ball.getX(),
                        ball.getY(),
                        (float)(Math.cos(particleAngle) * speed),
                        (float)(Math.sin(particleAngle) * speed),
                        100,
                        new Color(255, 200, 100),
                        3
                    );
                    particles.add(particle);
                }
            } else {
                // Если шар слишком маленький, сохраняем его
                newBalls.add(ball);
            }
        }
        
        // Заменяем старые шары новыми
        balls.clear();
        balls.addAll(newBalls);
        
        // Деактивируем эффект после применения
        isActive = false;
    }
} 