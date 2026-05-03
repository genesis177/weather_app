package com.example.weatherapp.ai;

import com.example.weatherapp.weather.WeatherReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiSummaryService {

    private final String apiKey;
    private final String modelName;
    private final ObjectMapper mapper = new ObjectMapper();

    private volatile ChatLanguageModel cachedModel;

    public AiSummaryService(
            @Value("${ai.gemini.api-key:}") String apiKey,
            @Value("${ai.gemini.model:gemini-1.5-flash}") String modelName
    ) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelName = (modelName == null || modelName.isBlank()) ? "gemini-1.5-flash" : modelName;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String summarize(WeatherReport report, String timezone, String city) {
        if (!isConfigured()) {
            return "AI-сводка недоступна: отсутствует ключ API Google Gemini.";
        }
        final var model = getModel();
        String reportJson;
        try {
            reportJson = mapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            reportJson = safeReport(report);
        }

        final var location = city != null && !city.isBlank() ? city : "предоставленные координаты";
        final var prompt = getPrompt(timezone, location, reportJson);

        try {
            return model.generate(prompt);
        } catch (RuntimeException ex) {
            return "AI-сводка недоступна в данный момент. Причина: " + ex.getMessage();
        }
    }

    @NotNull
    private static String getPrompt(String timezone, String location, String reportJson) {
        final var tz = timezone != null ? timezone : "auto";

        return "Вы — ассистент, который кратко излагает краткосрочные прогнозы погоды для широкой аудитории.\n" +
                "Исходя из JSON отчета о погоде от Open-Meteo (почасовые данные на следующие ~72 часа) и контекста, предоставьте:\n" +
                "1) Краткий обзор предстоящей погоды на следующие 1-3 дня в " + location + " (временная зона: " + tz + ").\n" +
                "2) Практические советы.\n" +
                "3) 3-5 предложений по активности, подходящих под текущие условия (на улице/в помещении).\n" +
                "Будьте конкретны в указании диапазонов температур, вероятности осадков, ветра и влажности.\n" +
                "Соблюдайте объем не более 180 слов, используйте короткие абзацы и маркированные списки.\n\n" +
                "JSON отчета о погоде:\n" + reportJson + "\n\n" +
                "Ответ в обычном тексте (без JSON).";
    }

    private String safeReport(WeatherReport r) {
        try { return mapper.writeValueAsString(r); } catch (Exception e) { return "{}"; }
    }

    private ChatLanguageModel getModel() {
        final var m = cachedModel;
        if (m == null) {
            synchronized (this) {
                final var m2 = cachedModel;
                if (m2 == null) {
                    cachedModel = GoogleAiGeminiChatModel.builder()
                            .apiKey(apiKey)
                            .modelName(modelName)
                            .build();
                }
                return cachedModel;
            }
        }
        return m;
    }
}