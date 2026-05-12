# ShieldNet ML — Microservicio de Predicción QoS

## Estructura
```
shieldnet-ml/
├── predict_api.py          ← Servidor Flask (microservicio)
├── qos_model.pkl           ← Modelo entrenado (Decision Tree)
├── qos_model_thresholds.json ← Umbrales del modelo en JSON
├── train_and_export.py     ← Script para re-entrenar el modelo
└── requirements.txt        ← Dependencias Python
```

## 1. Instalar y correr el microservicio

```bash
pip install -r requirements.txt
python predict_api.py
# Corre en http://localhost:5050
```

## 2. Endpoints disponibles

### GET /health
Verifica que el servicio esté activo.

### POST /predict
Predice la calidad de una medición QoS.

**Body:**
```json
{
  "jitter_ms": 25.5,
  "latency_ms": 110.0,
  "mos_score": 3.88,
  "packet_loss_percent": 0.0
}
```
**Respuesta:**
```json
{
  "predicted_status": "ACEPTABLE",
  "confidence": 0.97
}
```

### POST /predict/batch
Predice una lista de métricas.

## 3. Integración en Spring Boot

### Agregar al application.properties:
```properties
ml.service.url=http://localhost:5050
```

### Usar QosPredictionService.java (incluido abajo)
Llama automáticamente al microservicio después de cada medición QoS
y guarda el predicted_status en la base de datos.

## 4. Re-entrenar el modelo
Cuando tengas más datos reales, ejecuta:
```bash
python train_and_export.py
```
Esto regenera qos_model.pkl con los datos más recientes.
