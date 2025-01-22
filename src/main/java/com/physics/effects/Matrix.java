package com.physics.effects;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Matrix {
    private boolean isActive = false;
    private final Random random = new Random();
    private final List<MatrixColumn> columns = new ArrayList<>();
    private static final int CHAR_SIZE = 20;
    
    private class MatrixColumn {
        int x;
        List<MatrixChar> chars = new ArrayList<>();
        float speed;
        
        MatrixColumn(int x) {
            this.x = x;
            this.speed = 2 + random.nextFloat() * 3;
            // Создаем начальную цепочку символов
            for (int i = 0; i < 20; i++) {
                chars.add(new MatrixChar(-i * CHAR_SIZE));
            }
        }
        
        void update(int height) {
            // Обновляем позиции всех символов
            for (MatrixChar ch : chars) {
                ch.y += speed;
                // Меняем символ с небольшой вероятностью
                if (random.nextFloat() < 0.05f) {
                    ch.symbol = getRandomSymbol();
                }
            }
            
            // Если первый символ вышел за пределы экрана, перемещаем его наверх
            MatrixChar first = chars.get(0);
            if (first.y > height) {
                first.y = chars.get(chars.size() - 1).y - CHAR_SIZE;
                chars.add(chars.remove(0));
            }
        }
    }
    
    private class MatrixChar {
        double y;
        char symbol;
        
        MatrixChar(double y) {
            this.y = y;
            this.symbol = getRandomSymbol();
        }
    }
    
    public Matrix() {
        initializeColumns(800); // Начальная инициализация
    }
    
    private void initializeColumns(int width) {
        columns.clear();
        int numColumns = width / CHAR_SIZE;
        for (int i = 0; i < numColumns; i++) {
            columns.add(new MatrixColumn(i * CHAR_SIZE));
        }
    }
    
    public void setActive(boolean active) {
        if (active && !isActive) {
            initializeColumns(800);
        }
        isActive = active;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void update() {
        if (!isActive) return;
        
        for (MatrixColumn column : columns) {
            column.update(600);
        }
    }
    
    public void draw(Graphics2D g2d, int width, int height) {
        if (!isActive) return;
        
        // Если размер окна изменился, переинициализируем колонки
        if (columns.isEmpty() || columns.size() != width / CHAR_SIZE) {
            initializeColumns(width);
        }
        
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Font font = new Font(Font.MONOSPACED, Font.BOLD, CHAR_SIZE);
        g2d.setFont(font);
        
        // Рисуем каждую колонку
        for (MatrixColumn column : columns) {
            for (int i = 0; i < column.chars.size(); i++) {
                MatrixChar ch = column.chars.get(i);
                // Первый символ в колонке ярче
                if (i == 0) {
                    g2d.setColor(Color.WHITE);
                } else {
                    // Остальные символы зеленые с уменьшающейся яркостью
                    int alpha = Math.max(0, 255 - i * 12);
                    g2d.setColor(new Color(0, 255, 0, alpha));
                }
                g2d.drawString(String.valueOf(ch.symbol), column.x, (int)ch.y);
            }
        }
    }
    
    private char getRandomSymbol() {
        if (random.nextFloat() < 0.5f) {
            // Катакана
            return (char)(0x30A0 + random.nextInt(96));
        } else if (random.nextFloat() < 0.5f) {
            // Хирагана
            return (char)(0x3040 + random.nextInt(96));
        } else {
            // Кандзи
            return (char)(0x4E00 + random.nextInt(0x9FFF - 0x4E00));
        }
    }
} 