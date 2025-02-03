package com.physics;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("Запуск физической симуляции...");
        
        try {
            // Запускаем GUI в потоке обработки событий Swing
            SwingUtilities.invokeLater(() -> {
                PhysicsSimulation simulation = new PhysicsSimulation();
                simulation.setVisible(true);
            });
            
        } catch (Exception e) {
            System.err.println("Ошибка при запуске симуляции: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 