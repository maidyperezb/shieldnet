package com.example.security.servicios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.security.dto.QoSData;
import com.example.security.modelo.QoSMetrics;
import com.example.security.repositorio.QoSMetricsRepository;

@Service
public class QoSService {
    
    private static final Logger logger = LoggerFactory.getLogger(QoSService.class);
    
    @Autowired
    private QoSMetricsRepository qosRepository;

    @Autowired
    private QosPredictionService predictionService;
    
    public QoSData measureQoS(String targetIp, int packetCount) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                pb = new ProcessBuilder("ping", "-n", String.valueOf(packetCount), targetIp);
            } else {
                pb = new ProcessBuilder("ping", "-c", String.valueOf(packetCount), targetIp);
            }
            
            logger.info("Ejecutando ping a {} con {} paquetes (OS: {})", targetIp, packetCount, os);
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            List<Double> rttValues = new ArrayList<>();
            int sent = packetCount;
            int received = 0;
            
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("Línea ping: {}", line);
                Double rtt = extractRTT(line);
                if (rtt != null) {
                    rttValues.add(rtt);
                    received++;
                    logger.debug("RTT encontrado: {}ms", rtt);
                }
            }
            
            process.waitFor();
            
            logger.info("Ping completado - Enviados: {}, Recibidos: {}, RTTs capturados: {}",
                        sent, received, rttValues.size());
            
            QoSData qosData = calculateMetrics(rttValues, sent, received);
            
            saveQoSMetrics(targetIp, qosData, "ICMP");
            
            return qosData;
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error al medir QoS para {}: {}", targetIp, e.getMessage());
            return createErrorQoSData();
        }
    }
    
    private Double extractRTT(String line) {
        if (line.contains("time") || line.contains("tiempo")) {
            logger.debug("Analizando línea con tiempo: {}", line);
        }
        
        Pattern[] patterns = {
            Pattern.compile("time[=<](\\d+(?:\\.\\d+)?)\\s*ms", Pattern.CASE_INSENSITIVE),
            Pattern.compile("time=(\\d+(?:\\.\\d+)?)\\s*ms", Pattern.CASE_INSENSITIVE),
            Pattern.compile("tiempo[=<](\\d+(?:\\.\\d+)?)\\s*ms", Pattern.CASE_INSENSITIVE),
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                try {
                    double rtt = Double.parseDouble(matcher.group(1));
                    logger.debug("RTT extraído: {}ms", rtt);
                    return rtt;
                } catch (NumberFormatException e) {
                    logger.warn("Error parseando RTT: {}", matcher.group(1));
                }
            }
        }
        
        return null;
    }
    
    private QoSData calculateMetrics(List<Double> rttValues, int sent, int received) {
        QoSData qosData = new QoSData();
        
        if (rttValues.isEmpty() || received == 0) {
            logger.warn("No se recibieron respuestas de ping. Enviados: {}, recibidos: {}", sent, received);
            return createErrorQoSData();
        }
        
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (double rtt : rttValues) {
            sum += rtt;
            if (rtt < min) min = rtt;
            if (rtt > max) max = rtt;
        }
        
        double avgLatency = sum / rttValues.size();
        qosData.setLatencyMs(avgLatency);
        qosData.setRttAvgMs(avgLatency);
        qosData.setRttMinMs(min);
        qosData.setRttMaxMs(max);
        
        double jitter = calculateJitter(rttValues);
        qosData.setJitterMs(jitter);
        
        int receivedNormalized = Math.min(received, sent);
        int lost = sent - receivedNormalized;
        double packetLoss = (lost / (double) sent) * 100.0;
        qosData.setPacketLossPercent(packetLoss);
        qosData.setPacketsSent(sent);
        qosData.setPacketsReceived(receivedNormalized);
        qosData.setPacketsLost(lost);
        
        double mosScore = calculateMOSScore(avgLatency, jitter, packetLoss);
        qosData.setMosScore(mosScore);
        
        String qualityStatus = determineQualityStatus(mosScore);
        qosData.setQualityStatus(qualityStatus);
        
        logger.info("QoS calculado - Latencia: {}ms, Jitter: {}ms, Pérdida: {}%, MOS: {}, Estado: {}",
                    String.format("%.2f", avgLatency),
                    String.format("%.2f", jitter),
                    String.format("%.2f", packetLoss),
                    String.format("%.2f", mosScore),
                    qualityStatus);
        
        return qosData;
    }

    private String determineQualityStatus(double mosScore) {
        if (mosScore >= 4.3) return "EXCELENTE";
        else if (mosScore >= 4.0) return "BUENO";
        else if (mosScore >= 3.6) return "ACEPTABLE";
        else if (mosScore >= 3.1) return "MALO";
        else return "MUY MALO";
    }
    
    private double calculateJitter(List<Double> rttValues) {
        if (rttValues.size() < 2) return 0.0;
        
        double jitterSum = 0.0;
        for (int i = 1; i < rttValues.size(); i++) {
            jitterSum += Math.abs(rttValues.get(i) - rttValues.get(i - 1));
        }
        return jitterSum / (rttValues.size() - 1);
    }
    
    private double calculateMOSScore(double latency, double jitter, double packetLoss) {
        double r = 93.2;
        
        double latencyPenalty;
        if (latency < 160) {
            latencyPenalty = latency / 40.0;
        } else {
            latencyPenalty = (latency - 120) / 10.0;
        }
        
        double jitterPenalty = jitter / 2.0;
        double packetLossPenalty = packetLoss * 2.5;
        
        r = r - latencyPenalty - jitterPenalty - packetLossPenalty;
        if (r < 0) r = 0;
        if (r > 100) r = 100;
        
        double mos;
        if (r < 0) {
            mos = 1.0;
        } else if (r > 100) {
            mos = 4.5;
        } else {
            mos = 1 + 0.035 * r + 7 * Math.pow(10, -6) * r * (r - 60) * (100 - r);
        }
        
        if (mos < 1.0) mos = 1.0;
        if (mos > 5.0) mos = 5.0;
        
        return Math.round(mos * 100.0) / 100.0;
    }
    
    private void saveQoSMetrics(String targetIp, QoSData qosData, String protocolType) {
        try {
            QoSMetrics metrics = new QoSMetrics();
            metrics.setTargetIp(targetIp);
            metrics.setLatencyMs(qosData.getLatencyMs());
            metrics.setJitterMs(qosData.getJitterMs());
            metrics.setPacketLossPercent(qosData.getPacketLossPercent());
            metrics.setMosScore(qosData.getMosScore());
            metrics.setRttAvgMs(qosData.getRttAvgMs());
            metrics.setRttMinMs(qosData.getRttMinMs());
            metrics.setRttMaxMs(qosData.getRttMaxMs());
            metrics.setPacketsSent(qosData.getPacketsSent());
            metrics.setPacketsReceived(qosData.getPacketsReceived());
            metrics.setPacketsLost(qosData.getPacketsLost());
            metrics.setProtocolType(protocolType);
            metrics.setQualityStatus(qosData.getQualityStatus());

            // ── INTEGRACIÓN ML ────────────────────────────────────────
            try {
                // Buscar medición anterior del mismo host para calcular tendencia
                double mosAnterior     = qosData.getMosScore();    // default: sin cambio
                double jitterAnterior  = qosData.getJitterMs();
                double latencyAnterior = qosData.getLatencyMs();

                List<QoSMetrics> historial = qosRepository.findByTargetIpOrderByTimestampDesc(targetIp);
                if (!historial.isEmpty()) {
                    QoSMetrics anterior = historial.get(0);
                    mosAnterior     = anterior.getMosScore();
                    jitterAnterior  = anterior.getJitterMs();
                    latencyAnterior = anterior.getLatencyMs();
                    logger.info("Tendencia detectada para {} - MOS anterior: {}, actual: {}",
                                targetIp,
                                String.format("%.2f", mosAnterior),
                                String.format("%.2f", qosData.getMosScore()));
                } else {
                    logger.info("Primera medición para {}, sin historial previo", targetIp);
                }

                String predicted = predictionService.predict(
                    qosData.getMosScore(),     mosAnterior,
                    qosData.getJitterMs(),     jitterAnterior,
                    qosData.getLatencyMs(),    latencyAnterior,
                    qosData.getPacketLossPercent()
                );

                if (predicted != null) {
                    metrics.setPredictedStatus(predicted);
                    qosData.setPredictedStatus(predicted);
                    logger.info("ML predijo estado futuro: {} (sistema actual: {})",
                                predicted, qosData.getQualityStatus());
                }
            } catch (Exception e) {
                logger.warn("Predicción ML no disponible (no crítico): {}", e.getMessage());
            }
            // ─────────────────────────────────────────────────────────

            qosRepository.save(metrics);
            logger.info("Métricas QoS guardadas para {}: {}", targetIp, qosData);
        } catch (Exception e) {
            logger.error("Error al guardar métricas QoS: {}", e.getMessage());
        }
    }
    
    private QoSData createErrorQoSData() {
        QoSData qosData = new QoSData();
        qosData.setLatencyMs(0.0);
        qosData.setJitterMs(0.0);
        qosData.setPacketLossPercent(100.0);
        qosData.setMosScore(1.0);
        qosData.setQualityStatus("MUY_MALO");
        return qosData;
    }
    
    public List<QoSMetrics> getRecentMetrics(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return qosRepository.findRecentMetrics(since);
    }
    
    public List<QoSMetrics> getMetricsByIp(String targetIp) {
        return qosRepository.findByTargetIpOrderByTimestampDesc(targetIp);
    }
    
    public Double getAverageMosScore() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return qosRepository.getAverageMosScore(since);
    }

    public void updateLastMetricCallStatus(boolean duringCall) {
        try {
            List<QoSMetrics> lastMetrics = qosRepository.findTop10ByOrderByTimestampDesc();
            if (!lastMetrics.isEmpty()) {
                QoSMetrics lastMetric = lastMetrics.get(0);
                lastMetric.setDuringActiveCall(duringCall);
                qosRepository.save(lastMetric);
                logger.debug("Flag duringActiveCall actualizado a {} para métrica ID {}",
                            duringCall, lastMetric.getId());
            }
        } catch (Exception e) {
            logger.error("Error actualizando flag duringActiveCall: {}", e.getMessage());
        }
    }

    public Map<String, Object> getComparativeMetrics() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        
        Double avgMosDuringCalls    = qosRepository.getAverageMosDuringCalls(last24Hours);
        Double avgMosWithoutCalls   = qosRepository.getAverageMosWithoutCalls(last24Hours);
        
        List<QoSMetrics> allMetrics = qosRepository.findTop100ByOrderByTimestampDesc();
        long duringCallCount = allMetrics.stream()
            .filter(m -> m.getDuringActiveCall() != null && m.getDuringActiveCall()).count();
        long withoutCallCount = allMetrics.stream()
            .filter(m -> m.getDuringActiveCall() == null || !m.getDuringActiveCall()).count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("averageMosDuringCalls",    avgMosDuringCalls    != null ? avgMosDuringCalls    : 0.0);
        result.put("averageMosWithoutCalls",   avgMosWithoutCalls   != null ? avgMosWithoutCalls   : 0.0);
        result.put("measurementsDuringCalls",  duringCallCount);
        result.put("measurementsWithoutCalls", withoutCallCount);
        result.put("totalMeasurements",        allMetrics.size());
        
        return result;
    }

    public List<QoSMetrics> getAllMetrics() {
        return qosRepository.findAllByOrderByTimestampDesc();
    }
}