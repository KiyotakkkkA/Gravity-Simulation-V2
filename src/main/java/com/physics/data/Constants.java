package com.physics.data;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    // Размеры и визуальные параметры
    public static final int INFO_PANEL_HEIGHT = 160;
    public static final int MATRIX_CHARS = 50;
    public static final int MAX_TRAJECTORY_POINTS = 200;
    
    // Временные параметры
    public static final long KING_CRIMSON_DURATION = 5000;  // 5 секунд
    public static final long MAX_HISTORY_TIME = 10000; // 10 секунд для обращения времени
    
    // Физические константы
    public static final double WAVE_SPEED = 0.05;
    public static final double WAVE_AMPLITUDE = 0.5;
    
    // Пределы параметров
    public static final double MIN_TIME_SCALE = 0.01;
    public static final double MAX_TIME_SCALE = 10.0;
    public static final double MIN_GRAVITY = -5.0;
    public static final double MAX_GRAVITY = 5.0;
    public static final double MIN_WARP = -5.0;
    public static final double MAX_WARP = 5.0;
    public static final double MIN_ENERGY = 0.1;
    public static final double MAX_ENERGY = 5.0;
    public static final double MIN_DENSITY = 0.1;
    public static final double MAX_DENSITY = 5.0;

    // Описания эффектов
    public static final Map<String, String> EFFECT_DESCRIPTIONS = new HashMap<>();
    public static final Map<String, String> PARAMETER_DESCRIPTIONS = new HashMap<>();

    static {
        EFFECT_DESCRIPTIONS.put("Заморозка", "Останавливает время в локальной области пространства");
        EFFECT_DESCRIPTIONS.put("Чёрная дыра", "Создает сингулярность, притягивающую все объекты");
        EFFECT_DESCRIPTIONS.put("Врем. вихрь", "Закручивает пространство-время вокруг точки");
        EFFECT_DESCRIPTIONS.put("Радужный", "Квантовая нестабильность, вызывающая хроматические аномалии");
        EFFECT_DESCRIPTIONS.put("Квант. туннель", "Позволяет объектам проходить сквозь друг друга");
        EFFECT_DESCRIPTIONS.put("Взрывы", "Создает локальные разрывы в пространстве-времени");
        EFFECT_DESCRIPTIONS.put("Матрица", "Показывает истинную природу реальности");
        EFFECT_DESCRIPTIONS.put("Грав. волны", "Генерирует волны гравитационных возмущений");
        EFFECT_DESCRIPTIONS.put("Разрыв измерений", "Открывает порталы в параллельные вселенные");
        EFFECT_DESCRIPTIONS.put("Дилатация", "Локальное искривление течения времени");
        EFFECT_DESCRIPTIONS.put("Антиматерия", "Создает поле отрицательной материи");
        EFFECT_DESCRIPTIONS.put("Искажение", "Нарушает законы физики в случайных областях");
        EFFECT_DESCRIPTIONS.put("King Crimson", "Стирает время для всех, кроме пользователя. " +
            "Оставляет следы движения объектов и позволяет видеть их будущие позиции. " +
            "Активация: K");

        PARAMETER_DESCRIPTIONS.put("Время", "Управляет скоростью течения времени. Влияет на все движущиеся объекты");
        PARAMETER_DESCRIPTIONS.put("Гравитация", "Сила притяжения/отталкивания. Определяет падение объектов");
        PARAMETER_DESCRIPTIONS.put("Искривление", "Деформация пространства-времени. Создает локальные гравитационные аномалии");
        PARAMETER_DESCRIPTIONS.put("Энергия", "Уровень кинетической энергии. Влияет на силу отскока и взаимодействий");
        PARAMETER_DESCRIPTIONS.put("Плотность", "Плотность материи. Определяет количество и массу частиц");
    }
} 