-- Ejecutar en MariaDB para agregar la columna de predicción ML
-- Base de datos: shieldnet

ALTER TABLE `qos_metrics`
  ADD COLUMN `predicted_status` VARCHAR(50) DEFAULT NULL
  COMMENT 'Predicción del modelo ML (árbol de decisión)';

-- Verificar que se agregó correctamente
DESCRIBE qos_metrics;
