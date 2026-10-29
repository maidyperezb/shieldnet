-- Ejecutar en MariaDB para agregar la columna de predicción ML
-- Base de datos: shieldnet

ALTER TABLE `qos_metrics`
  ADD COLUMN `predicted_status` VARCHAR(50) DEFAULT NULL
  COMMENT 'Predicción del modelo ML (árbol de decisión)';

ALTER TABLE `qos_metrics`
  ADD COLUMN `predicted_confidence` DOUBLE DEFAULT NULL
  COMMENT 'Confianza de la predicción ML (0.0 - 1.0)';

-- Verificar que se agregó correctamente
DESCRIBE qos_metrics;
