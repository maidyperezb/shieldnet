"""
predict_api.py — Microservicio Flask para ShieldNet v2.0
Predice el ESTADO FUTURO del host usando tendencias,
no solo el estado actual (que ya lo calcula el sistema Java).

Endpoints:
  GET  /health
  POST /predict        ← predicción individual con tendencia
  POST /predict/batch  ← predicción en lote
"""

from flask import Flask, request, jsonify
import pickle
import numpy as np
import os
import pandas as pd

app = Flask(__name__)

# ── Cargar modelo al iniciar ──────────────────────────────────
MODEL_PATH = os.getenv('MODEL_PATH', 'qos_model.pkl')
with open(MODEL_PATH, 'rb') as f:
    model = pickle.load(f)

# Features que espera el modelo v2 (tendencia)
FEATURES = [
    'mos_actual',
    'mos_anterior',
    'delta_mos',
    'jitter_actual',
    'delta_jitter',
    'latency_actual',
    'delta_latency',
    'packet_loss'
]

# Descripciones amigables por label
LABEL_INFO = {
    'ESTABLE':    {'emoji': '✅', 'color': 'green',  'desc': 'Conexión estable'},
    'EN RIESGO':  {'emoji': '⚠️', 'color': 'yellow', 'desc': 'Calidad bajando'},
    'DEGRADANDO': {'emoji': '📉', 'color': 'orange', 'desc': 'Deterioro activo'},
    'CRITICO':    {'emoji': '🔴', 'color': 'red',    'desc': 'Intervención recomendada'},
}

# ── Health check ──────────────────────────────────────────────
@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        "status": "ok",
        "model": "QoS Trend Predictor v2.0",
        "model_type": "RandomForestClassifier",
        "prediction": "estado_futuro_del_host",
        "labels": list(LABEL_INFO.keys())
    })

# ── Predicción individual ─────────────────────────────────────
@app.route('/predict', methods=['POST'])
def predict():
    """
    Body JSON esperado:
    {
      "mos_actual":      4.16,   ← MOS de la medición actual
      "mos_anterior":    4.20,   ← MOS de la medición anterior del mismo host
      "jitter_actual":   15.53,
      "jitter_anterior": 12.00,
      "latency_actual":  62.55,
      "latency_anterior":58.00,
      "packet_loss":     0.0
    }

    Respuesta:
    {
      "predicted_status": "EN RIESGO",
      "confidence":       0.82,
      "emoji":            "⚠️",
      "description":      "Calidad bajando",
      "delta_mos":        -0.04
    }
    """
    try:
        body = request.get_json()

        # Leer valores con defaults si no vienen (primera medición sin historial)
        mos_actual     = float(body.get('mos_actual', 4.0))
        mos_anterior   = float(body.get('mos_anterior', mos_actual))   # sin cambio si no hay historial
        jitter_actual  = float(body.get('jitter_actual', 0))
        jitter_ant     = float(body.get('jitter_anterior', jitter_actual))
        latency_actual = float(body.get('latency_actual', 0))
        latency_ant    = float(body.get('latency_anterior', latency_actual))
        packet_loss    = max(0.0, float(body.get('packet_loss', 0)))

        # Calcular deltas
        delta_mos      = mos_actual - mos_anterior
        delta_jitter   = jitter_actual - jitter_ant
        delta_latency  = latency_actual - latency_ant

        X = pd.DataFrame([{
            'mos_actual': mos_actual,
            'mos_anterior': mos_anterior,
            'delta_mos': delta_mos,
            'jitter_actual': jitter_actual,
            'delta_jitter': delta_jitter,
            'latency_actual': latency_actual,
            'delta_latency': delta_latency,
            'packet_loss': packet_loss
        }])

        prediction    = model.predict(X)[0]
        probabilities = model.predict_proba(X)[0]
        confidence    = float(max(probabilities))

        info = LABEL_INFO.get(prediction, {'emoji': '❓', 'color': 'gray', 'desc': ''})

        return jsonify({
            "predicted_status": prediction,
            "confidence":       round(confidence, 4),
            "emoji":            info['emoji'],
            "color":            info['color'],
            "description":      info['desc'],
            "delta_mos":        round(delta_mos, 3),
            "trend":            "bajando" if delta_mos < -0.05 else "subiendo" if delta_mos > 0.05 else "estable"
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── Predicción en lote ────────────────────────────────────────
@app.route('/predict/batch', methods=['POST'])
def predict_batch():
    """
    Predice múltiples hosts a la vez.
    Body: lista de objetos con los mismos campos de /predict.
    """
    try:
        items = request.get_json()
        if not isinstance(items, list):
            return jsonify({"error": "Se espera un array JSON"}), 400

        results = []
        for item in items:
            mos_actual     = float(item.get('mos_actual', 4.0))
            mos_anterior   = float(item.get('mos_anterior', mos_actual))
            jitter_actual  = float(item.get('jitter_actual', 0))
            jitter_ant     = float(item.get('jitter_anterior', jitter_actual))
            latency_actual = float(item.get('latency_actual', 0))
            latency_ant    = float(item.get('latency_anterior', latency_actual))
            packet_loss    = max(0.0, float(item.get('packet_loss', 0)))

            delta_mos     = mos_actual - mos_anterior
            delta_jitter  = jitter_actual - jitter_ant
            delta_latency = latency_actual - latency_ant

            X = pd.DataFrame([{
                'mos_actual': mos_actual,
                'mos_anterior': mos_anterior,
                'delta_mos': delta_mos,
                'jitter_actual': jitter_actual,
                'delta_jitter': delta_jitter,
                'latency_actual': latency_actual,
                'delta_latency': delta_latency,
                'packet_loss': packet_loss
            }])

            pred = model.predict(X)[0]
            conf = float(max(model.predict_proba(X)[0]))
            info = LABEL_INFO.get(pred, {'emoji': '❓', 'color': 'gray', 'desc': ''})

            results.append({
                "id":               item.get('id'),
                "predicted_status": pred,
                "confidence":       round(conf, 4),
                "emoji":            info['emoji'],
                "description":      info['desc'],
                "delta_mos":        round(delta_mos, 3)
            })

        return jsonify(results)

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── Iniciar servidor ──────────────────────────────────────────
if __name__ == '__main__':
    port = int(os.getenv('PORT', 5050))
    print(f"ShieldNet ML v2.0 corriendo en puerto {port}")
    print(f"Predicción: estado futuro del host (ESTABLE / EN RIESGO / DEGRADANDO / CRITICO)")
    app.run(host='0.0.0.0', port=port, debug=False)