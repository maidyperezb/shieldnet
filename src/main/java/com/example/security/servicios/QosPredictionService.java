package com.example.security.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service
public class QosPredictionService {

    private static final Logger logger = LoggerFactory.getLogger(QosPredictionService.class);

    @Value("${ml.service.url:http://localhost:5050}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public static class PredictRequest {
        @JsonProperty("mos_actual")       public double mosActual;
        @JsonProperty("mos_anterior")     public double mosAnterior;
        @JsonProperty("jitter_actual")    public double jitterActual;
        @JsonProperty("jitter_anterior")  public double jitterAnterior;
        @JsonProperty("latency_actual")   public double latencyActual;
        @JsonProperty("latency_anterior") public double latencyAnterior;
        @JsonProperty("packet_loss")      public double packetLoss;

        public PredictRequest(double mosActual,    double mosAnterior,
                              double jitterActual, double jitterAnterior,
                              double latencyActual,double latencyAnterior,
                              double packetLoss) {
            this.mosActual       = mosActual;
            this.mosAnterior     = mosAnterior;
            this.jitterActual    = jitterActual;
            this.jitterAnterior  = jitterAnterior;
            this.latencyActual   = latencyActual;
            this.latencyAnterior = latencyAnterior;
            this.packetLoss      = packetLoss;
        }
    }

    public static class PredictResponse {
        @JsonProperty("predicted_status") public String predictedStatus;
        @JsonProperty("confidence")       public double confidence;
        @JsonProperty("trend")            public String trend;
    }

    public String predict(double mosActual,    double mosAnterior,
                          double jitterActual, double jitterAnterior,
                          double latencyActual,double latencyAnterior,
                          double packetLoss) {
        try {
            String url = mlServiceUrl + "/predict";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            PredictRequest body = new PredictRequest(
                mosActual, mosAnterior,
                jitterActual, jitterAnterior,
                latencyActual, latencyAnterior,
                packetLoss
            );

            HttpEntity<PredictRequest> entity = new HttpEntity<>(body, headers);

            ResponseEntity<PredictResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, PredictResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                PredictResponse result = response.getBody();
                logger.info("ML Predicción futura: {} (confianza: {}%, tendencia: {})",
                            result.predictedStatus,
                            Math.round(result.confidence * 100),
                            result.trend);
                return result.predictedStatus;
            }

        } catch (Exception e) {
            logger.warn("Microservicio ML no disponible: {}", e.getMessage());
        }
        return null;
    }
}