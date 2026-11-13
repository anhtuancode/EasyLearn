package com.studying.easylearn.utils;

public class LevelManager {

    // Trả về tên cấp độ mới nhất dựa trên thông số
    public static String calculateLevel(int storiesCompleted, int wordsMastered) {
        if (storiesCompleted >= 75 && wordsMastered >= 350) return "C2";
        if (storiesCompleted >= 50 && wordsMastered >= 200) return "C1";
        if (storiesCompleted >= 30 && wordsMastered >= 120) return "B2";
        if (storiesCompleted >= 15 && wordsMastered >= 60) return "B1";
        if (storiesCompleted >= 5 && wordsMastered >= 20) return "A2";
        return "A1";
    }

    // Tính % hoàn thành tới cấp độ tiếp theo
    public static int calculateProgressToNextLevel(int storiesCompleted, int wordsMastered) {
        String current = calculateLevel(storiesCompleted, wordsMastered);
        int targetStories = 5;
        int targetWords = 20;

        switch (current) {
            case "A1": targetStories = 5; targetWords = 20; break;
            case "A2": targetStories = 15; targetWords = 60; break;
            case "B1": targetStories = 30; targetWords = 120; break;
            case "B2": targetStories = 50; targetWords = 200; break;
            case "C1": targetStories = 75; targetWords = 350; break;
            case "C2": return 100; // Đã Max Level
        }

        float storyProgress = Math.min(1.0f, (float) storiesCompleted / targetStories);
        float wordProgress = Math.min(1.0f, (float) wordsMastered / targetWords);

        return (int) (((storyProgress + wordProgress) / 2.0f) * 100);
    }
}