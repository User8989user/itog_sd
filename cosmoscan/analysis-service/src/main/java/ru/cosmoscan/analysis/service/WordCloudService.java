package ru.cosmoscan.analysis.service;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.LayerBackgrounds;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.LinearGradientColorPalette;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordCloudService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${storing.service.url}")
    private String storingUrl;

    public byte[] generateWordCloud(Long workId) throws IOException {
        // 1. Получить файл
        ResponseEntity<byte[]> fileResp = restTemplate.getForEntity(
                storingUrl + "/api/works/" + workId + "/file", byte[].class);
        byte[] fileData = fileResp.getBody();
        if (fileData == null) throw new IOException("No file data");

        // 2. Извлечь текст
        String text;
        try (InputStream is = new ByteArrayInputStream(fileData)) {
            Tika tika = new Tika();
            text = tika.parseToString(is);
        }
        if (text.isBlank()) text = "no words";

        // 3. Анализ частотности
        FrequencyAnalyzer freqAnalyzer = new FrequencyAnalyzer();
        freqAnalyzer.setWordFrequenciesToReturn(200);
        freqAnalyzer.setMinWordLength(3);
        List<WordFrequency> wordFrequencies = freqAnalyzer.load(getStopWords(), text);

        // 4. Создание облака слов
        Dimension size = new Dimension(800, 600);
        WordCloud wordCloud = new WordCloud(size, LayerBackgrounds.WHITE);
        wordCloud.setPadding(2);
        wordCloud.setBackgroundColor(Color.WHITE);
        wordCloud.setColorPalette(new LinearGradientColorPalette(Color.BLUE, Color.CYAN, Color.MAGENTA));
        wordCloud.setFontScalar(new LinearFontScalar(10, 48));
        wordCloud.setCollisionMode(CollisionMode.PIXEL_PERFECT);
        wordCloud.build(wordFrequencies);

        BufferedImage image = wordCloud.getBufferedImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private List<String> getStopWords() {
        return Arrays.asList(
                "и", "в", "на", "с", "а", "но", "за", "по", "к", "у", "о", "об", "от", "для", "без",
                "the", "a", "an", "and", "of", "to", "in", "for", "on", "with", "that", "this", "is"
        );
    }
}