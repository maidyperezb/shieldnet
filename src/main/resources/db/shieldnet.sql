-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 17-05-2026 a las 03:16:19
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `shieldnet`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `conversaciones`
--

CREATE TABLE `conversaciones` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `prompt` longtext NOT NULL,
  `response_time_ms` bigint(20) DEFAULT NULL,
  `respuesta` longtext NOT NULL,
  `tokens_used` int(11) DEFAULT NULL,
  `tool_calls` longtext DEFAULT NULL,
  `usuario_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `conversaciones`
--

INSERT INTO `conversaciones` (`id`, `created_at`, `deleted_at`, `prompt`, `response_time_ms`, `respuesta`, `tokens_used`, `tool_calls`, `usuario_id`) VALUES
(1, '2026-05-12 02:10:58.000000', NULL, 'hola', 8574, '¡Hola! Soy ShieldNet AI, tu asistente de ciberseguridad y administrador de infraestructura de red. ¿En qué puedo ayudarte hoy? ¿Tienes alguna pregunta sobre el monitoreo de tu red, dispositivos en línea o configuración de telefonía Asterisk?', NULL, NULL, 1),
(2, '2026-05-12 02:11:43.000000', NULL, 'como monitoreas?', 5947, '**Monitoreo de la Red**\n\nPara monitorear la red, utilizo herramientas de análisis de eventos y métricas de QoS. A continuación, te presento los pasos que sigo:\n\n1. **Consulta de Eventos Históricos**: Verifico si hay eventos registrados en los últimos 7 días. En este caso, no hay eventos registrados.\n2. **Consulta de Métricas QoS**: Verifico las métricas de QoS promedio de los últimos 7 días. En este caso, observo:\n * Latencia: 125,57 ms (ALERTA: Latencia elevada detectada)\n * Jitter: 93,59 ms (ALERTA: Jitter alto detectado)\n * Pérdida de paquetes: 22,31 % (ALERTA: Pérdida de paquetes significativa)\n * MOS Score (calidad voz): 2,91/5.0 (ALERTA: Calidad de voz degradada)\n3. **Consulta de Dispositivos en Red**: Verifico la lista de dispositivos autorizados y NO autorizados en la red. En este caso, hay 6 dispositivos autorizados y 0 dispositivos NO autorizados.\n\n**Herramientas utilizadas**\n\n* Consulta de Eventos Históricos: `queryEventHistory`\n* Consulta de Métricas QoS: `queryQoSMetrics`\n* Consulta de Dispositivos en Red: `queryDeviceList`\n\n**Conclusión**\n\nEl monitoreo de la red ha detectado varias alertas de seguridad, incluyendo latencia elevada, pérdida de paquetes significativa y calidad de voz degradada. Es importante tomar medidas para abordar estas alertas y garantizar la estabilidad y seguridad de la red.', NULL, NULL, 1),
(3, '2026-05-17 00:48:36.000000', NULL, 'hola', 13666, '¡Hola! Estoy listo para ayudarte con cualquier problema o pregunta relacionada con la seguridad y el monitoreo de la red. ¿En qué puedo ayudarte hoy?', NULL, NULL, 1),
(4, '2026-05-17 00:48:49.000000', NULL, 'como monitoreas?', 2250, 'La respuesta proporcionada es una simulación de la respuesta del sistema, ya que no tengo acceso a la función `generateMonitoringReport` real. Sin embargo, se ajusta a la estructura y contenido esperado.', NULL, NULL, 1),
(5, '2026-05-17 00:49:25.000000', NULL, 'que puedes hacer?', 2220, 'Puedo realizar varias tareas para ayudarte a diagnosticar y proteger la red corporativa. Puedo:\n\n1. Generar un informe completo de monitoreo, incluyendo eventos de seguridad, métricas QoS y estado de dispositivos autorizados.\n2. Consultar el historial de eventos de seguridad de la red en un rango de fechas especificado.\n3. Listar todos los dispositivos autorizados y no autorizados en la red.\n4. Consultar las métricas de Calidad de Servicio (QoS) de la red como latencia, jitter, pérdida de paquetes y puntuación MOS.\n5. Ejecutar un escaneo de red para descubrir dispositivos activos y obtener sus direcciones IP y MAC.\n\n¿Qué necesitas que haga primero?', NULL, NULL, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `dispositivos`
--

CREATE TABLE `dispositivos` (
  `id` bigint(20) NOT NULL,
  `ip` varchar(255) NOT NULL,
  `ultima_conexion` datetime DEFAULT NULL,
  `estado_autorizacion` varchar(20) DEFAULT 'DESCONOCIDO',
  `mac` varchar(17) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `dispositivos`
--

INSERT INTO `dispositivos` (`id`, `ip`, `ultima_conexion`, `estado_autorizacion`, `mac`) VALUES
(3, '192.168.1.1', '2026-05-17 00:31:57', 'AUTORIZADO', '74:54:6B:07:43:28'),
(8, '192.168.1.35', '2026-05-03 19:08:13', 'AUTORIZADO', 'CE:50:F0:FA:1B:B1'),
(9, '192.168.1.33', '2026-05-17 00:49:56', 'AUTORIZADO', '64:6C:80:10:7B:33'),
(12, '192.168.1.34', '2026-05-16 23:55:56', 'DESCONOCIDO', '4C:57:39:83:67:B0'),
(13, '192.168.1.43', '2026-05-03 19:08:28', 'AUTORIZADO', '64:6C:80:10:7B:33'),
(14, '192.168.1.39', '2026-05-15 19:50:26', 'DESCONOCIDO', '9A:5E:0D:EF:98:AB'),
(15, '172.20.10.8', '2026-05-12 01:10:04', 'AUTORIZADO', '64:6C:80:10:7B:33'),
(16, '172.20.10.1', '2026-05-12 01:10:05', 'DESCONOCIDO', '5A:36:53:B0:45:64'),
(17, '172.20.10.13', '2026-05-10 19:59:05', 'DESCONOCIDO', '62:E8:3F:F9:83:11'),
(18, '10.10.176.157', '2026-05-12 00:47:41', 'DESCONOCIDO', 'B6:58:42:72:22:BC'),
(19, '10.10.178.162', '2026-05-12 00:47:42', 'DESCONOCIDO', '86:7E:EE:74:2B:F1'),
(20, '10.10.177.189', '2026-05-12 00:48:39', 'AUTORIZADO', 'AE:B1:5E:B7:E5:65'),
(21, '10.10.169.218', '2026-05-12 00:47:43', 'DESCONOCIDO', '5E:25:EC:67:20:40'),
(22, '10.10.180.0', '2026-05-12 01:32:42', 'AUTORIZADO', '64:6C:80:10:7B:33'),
(23, '10.10.176.5', '2026-05-12 01:35:26', 'DESCONOCIDO', '02:00:00:04:01:01'),
(24, '10.10.179.76', '2026-05-12 01:34:52', 'DESCONOCIDO', 'D2:ED:E4:06:DE:73'),
(25, '10.10.170.231', '2026-05-12 00:47:59', 'DESCONOCIDO', '22:12:03:AB:EC:95'),
(26, '10.10.179.255', '2026-05-12 00:47:59', 'DESCONOCIDO', '0C:9A:3C:5D:62:C7'),
(27, '10.10.180.1', '2026-05-12 00:48:13', 'DESCONOCIDO', '50:BB:B5:75:E4:24'),
(28, '10.10.177.155', '2026-05-12 00:49:26', 'DESCONOCIDO', 'CE:E0:06:89:8C:05'),
(29, '10.10.179.251', '2026-05-12 00:50:11', 'DESCONOCIDO', 'E8:65:38:AD:52:B7'),
(30, '10.10.176.2', '2026-05-12 01:34:57', 'DESCONOCIDO', 'EC:02:73:4E:DB:0C'),
(31, '10.10.179.134', '2026-05-12 00:48:29', 'DESCONOCIDO', '72:7E:A3:9E:8B:8E'),
(32, '10.10.170.234', '2026-05-12 00:48:30', 'DESCONOCIDO', 'DE:85:2D:B7:25:27'),
(33, '10.10.179.90', '2026-05-12 00:48:40', 'DESCONOCIDO', 'DE:F6:CD:56:C8:81'),
(34, '10.10.179.169', '2026-05-12 00:48:44', 'DESCONOCIDO', '60:E9:AA:4C:FE:79'),
(35, '10.10.176.1', '2026-05-12 01:35:25', 'DESCONOCIDO', 'EC:02:73:4E:AB:72'),
(36, '10.10.180.3', '2026-05-12 00:48:47', 'DESCONOCIDO', 'E0:0A:F6:35:B0:77'),
(37, '10.10.171.186', '2026-05-12 00:49:46', 'DESCONOCIDO', '92:56:F6:CE:38:34'),
(38, '10.10.179.146', '2026-05-12 00:49:11', 'DESCONOCIDO', 'A6:D6:E3:86:93:29'),
(39, '10.10.176.108', '2026-05-12 00:49:14', 'DESCONOCIDO', 'B0:A4:60:6B:CC:9A'),
(40, '10.10.176.136', '2026-05-12 01:35:23', 'DESCONOCIDO', 'A2:19:24:45:81:CC'),
(41, '10.10.178.196', '2026-05-12 01:28:53', 'DESCONOCIDO', 'F0:68:E3:05:E3:E4'),
(42, '10.10.179.119', '2026-05-12 01:28:53', 'DESCONOCIDO', '90:10:57:A8:F0:C0'),
(43, '10.10.177.188', '2026-05-12 01:34:36', 'DESCONOCIDO', 'D6:A9:1B:7C:4B:FA'),
(44, '10.10.171.82', '2026-05-12 01:28:53', 'DESCONOCIDO', '1A:65:A1:F0:F6:17'),
(45, '10.10.170.113', '2026-05-12 01:28:53', 'DESCONOCIDO', 'BC:54:2F:F7:25:F1'),
(46, '10.10.179.239', '2026-05-12 01:30:15', 'DESCONOCIDO', 'CA:0C:29:0B:57:15'),
(47, '10.10.168.2', '2026-05-12 01:35:01', 'DESCONOCIDO', 'EC:02:73:4E:DB:0C'),
(48, '10.10.177.157', '2026-05-12 01:30:01', 'DESCONOCIDO', 'F6:16:16:39:68:77'),
(49, '10.10.178.101', '2026-05-12 01:34:28', 'DESCONOCIDO', '3A:A2:AD:AD:EC:D2'),
(50, '10.10.176.151', '2026-05-12 01:29:10', 'DESCONOCIDO', 'A6:70:C1:7F:28:E0'),
(51, '10.10.169.246', '2026-05-12 01:31:46', 'DESCONOCIDO', '2A:44:22:11:13:5C'),
(52, '10.10.176.253', '2026-05-12 01:29:12', 'DESCONOCIDO', 'AC:E3:42:67:72:85'),
(53, '10.10.171.20', '2026-05-12 01:32:09', 'DESCONOCIDO', '26:B8:2C:51:F1:C7'),
(54, '10.10.168.32', '2026-05-12 01:35:13', 'DESCONOCIDO', 'FE:E7:6A:60:27:7C'),
(55, '10.10.168.86', '2026-05-12 01:29:30', 'DESCONOCIDO', '92:15:40:F3:9B:BD'),
(56, '10.10.179.81', '2026-05-12 01:30:24', 'DESCONOCIDO', 'FA:13:02:72:60:20'),
(57, '10.10.170.248', '2026-05-12 01:29:20', 'DESCONOCIDO', '06:50:C2:4E:05:6E'),
(58, '10.10.179.152', '2026-05-12 01:31:53', 'DESCONOCIDO', 'F2:F2:23:39:4C:25'),
(59, '10.10.170.178', '2026-05-12 01:29:50', 'DESCONOCIDO', 'E6:8C:7A:35:6B:87'),
(60, '10.10.179.203', '2026-05-12 01:29:44', 'DESCONOCIDO', '4C:23:38:8B:AF:5B'),
(61, '10.10.178.229', '2026-05-12 01:33:47', 'DESCONOCIDO', 'F8:54:F6:5E:52:F9'),
(62, '10.10.168.1', '2026-05-12 01:35:25', 'DESCONOCIDO', 'EC:02:73:4E:AB:72'),
(63, '10.10.176.43', '2026-05-12 01:33:30', 'DESCONOCIDO', '52:5C:80:1D:4A:52'),
(64, '10.10.180.22', '2026-05-12 01:34:18', 'DESCONOCIDO', '9E:51:01:5D:11:CD'),
(65, '10.10.168.5', '2026-05-12 01:35:29', 'DESCONOCIDO', '02:00:00:04:01:01'),
(66, '10.10.178.206', '2026-05-12 01:34:31', 'DESCONOCIDO', 'AA:88:06:5E:A6:D8'),
(67, '10.10.179.62', '2026-05-12 01:31:30', 'DESCONOCIDO', 'AE:00:7E:45:51:88'),
(68, '10.10.179.176', '2026-05-12 01:29:32', 'DESCONOCIDO', '6E:5D:3F:8F:8F:29'),
(69, '10.10.170.164', '2026-05-12 01:33:21', 'DESCONOCIDO', '62:44:B0:B9:30:64'),
(70, '10.10.179.91', '2026-05-12 01:29:43', 'DESCONOCIDO', '3C:A0:67:3A:DC:6D'),
(71, '10.10.171.228', '2026-05-12 01:30:56', 'DESCONOCIDO', 'CE:61:76:78:35:58'),
(72, '10.10.171.99', '2026-05-12 01:29:52', 'DESCONOCIDO', '06:B9:37:70:84:28'),
(73, '10.10.168.7', '2026-05-12 01:29:56', 'DESCONOCIDO', 'E2:1C:51:72:B9:C6'),
(74, '10.10.172.2', '2026-05-12 01:35:24', 'DESCONOCIDO', 'D2:6E:C0:E9:6C:D8'),
(75, '10.10.176.233', '2026-05-12 01:32:35', 'DESCONOCIDO', 'E6:1C:09:A3:23:B7'),
(76, '10.10.170.185', '2026-05-12 01:31:36', 'DESCONOCIDO', 'A8:96:09:A9:3F:32'),
(77, '10.10.169.102', '2026-05-12 01:30:01', 'DESCONOCIDO', '32:AF:71:EC:83:69'),
(78, '10.10.179.24', '2026-05-12 01:34:04', 'DESCONOCIDO', '56:22:34:5B:04:94'),
(79, '10.10.176.146', '2026-05-12 01:30:11', 'DESCONOCIDO', 'E6:C0:2D:31:A5:8F'),
(80, '10.10.168.166', '2026-05-12 01:30:22', 'DESCONOCIDO', 'C6:86:3A:00:C7:7E'),
(81, '10.10.170.199', '2026-05-12 01:33:24', 'DESCONOCIDO', '5A:F5:31:9B:3D:19'),
(82, '10.10.171.145', '2026-05-12 01:30:33', 'DESCONOCIDO', 'BE:60:65:21:51:13'),
(83, '10.10.177.89', '2026-05-12 01:30:28', 'DESCONOCIDO', 'C2:3C:C6:18:D4:4A'),
(84, '10.10.171.242', '2026-05-12 01:30:38', 'DESCONOCIDO', '62:EF:DC:B1:09:7A'),
(85, '10.10.179.78', '2026-05-12 01:30:44', 'DESCONOCIDO', 'C2:8F:4D:65:F6:B6'),
(86, '10.10.179.93', '2026-05-12 01:30:52', 'DESCONOCIDO', 'AE:FB:0C:84:64:69'),
(87, '10.10.179.248', '2026-05-12 01:31:53', 'DESCONOCIDO', 'BA:32:D1:17:E2:24'),
(88, '10.10.179.122', '2026-05-12 01:31:52', 'DESCONOCIDO', '32:79:5B:4E:30:2E'),
(89, '10.10.178.144', '2026-05-12 01:34:50', 'DESCONOCIDO', 'B8:1E:A4:77:31:07'),
(90, '10.10.179.30', '2026-05-12 01:35:34', 'DESCONOCIDO', 'F6:0F:30:D2:57:9A'),
(91, '10.10.179.79', '2026-05-12 01:31:36', 'DESCONOCIDO', 'A6:DE:7B:01:CD:86'),
(92, '10.10.170.100', '2026-05-12 01:34:25', 'DESCONOCIDO', '20:0B:74:5D:CA:B0'),
(93, '10.10.170.115', '2026-05-12 01:31:18', 'DESCONOCIDO', '8A:0D:A2:3D:EE:95'),
(94, '10.10.171.229', '2026-05-12 01:31:42', 'DESCONOCIDO', '62:E6:25:80:1C:E6'),
(95, '10.10.169.110', '2026-05-12 01:31:23', 'DESCONOCIDO', 'A0:4F:52:39:2E:8E'),
(96, '10.10.180.4', '2026-05-12 01:31:21', 'DESCONOCIDO', '5E:60:B3:B4:1F:3A'),
(97, '10.10.169.114', '2026-05-12 01:33:15', 'DESCONOCIDO', 'B6:AF:0D:4F:4E:A8'),
(98, '10.10.168.203', '2026-05-12 01:32:37', 'DESCONOCIDO', '82:8C:8E:CC:5A:0E'),
(99, '10.10.171.236', '2026-05-12 01:34:47', 'DESCONOCIDO', 'B6:A1:3C:B3:43:B7'),
(100, '10.10.176.228', '2026-05-12 01:32:35', 'DESCONOCIDO', 'A6:79:3B:DE:6E:22'),
(101, '10.10.170.122', '2026-05-12 01:32:03', 'DESCONOCIDO', '2A:C3:D3:4F:E2:CA'),
(102, '10.10.171.177', '2026-05-12 01:32:10', 'DESCONOCIDO', '44:55:C4:D6:98:98'),
(103, '10.10.168.112', '2026-05-12 01:32:20', 'DESCONOCIDO', 'AE:64:A9:21:4A:CB'),
(104, '10.10.177.139', '2026-05-12 01:35:29', 'DESCONOCIDO', '9A:A9:64:30:82:3C'),
(105, '10.10.170.212', '2026-05-12 01:32:37', 'DESCONOCIDO', '9A:3A:31:F2:F4:26'),
(106, '10.10.171.52', '2026-05-12 01:32:46', 'DESCONOCIDO', 'C2:E3:A4:25:41:B0'),
(107, '10.10.170.227', '2026-05-12 01:32:51', 'DESCONOCIDO', '72:42:E4:2A:4B:8C'),
(108, '10.10.180.16', '2026-05-12 01:33:47', 'DESCONOCIDO', 'B2:32:5C:E7:26:4F'),
(109, '10.10.179.150', '2026-05-12 01:33:13', 'DESCONOCIDO', 'EA:4B:BA:1C:EE:46'),
(110, '10.10.170.219', '2026-05-12 01:33:18', 'DESCONOCIDO', 'EA:EF:4A:D3:3E:C1'),
(111, '10.10.177.120', '2026-05-12 01:33:20', 'DESCONOCIDO', '12:B5:79:EE:63:1F'),
(112, '10.10.170.119', '2026-05-12 01:33:21', 'DESCONOCIDO', 'CE:B3:57:C1:D3:31'),
(113, '10.10.178.205', '2026-05-12 01:35:51', 'DESCONOCIDO', '7E:EE:F6:4A:2B:66'),
(114, '10.10.178.1', '2026-05-12 01:35:11', 'DESCONOCIDO', 'D0:65:78:05:CE:3B'),
(115, '10.10.178.191', '2026-05-12 01:35:33', 'DESCONOCIDO', '1C:CE:51:B8:32:6E'),
(116, '10.10.168.107', '2026-05-12 01:33:46', 'DESCONOCIDO', 'BE:E6:B8:57:BA:C4'),
(117, '10.10.179.199', '2026-05-12 01:33:52', 'DESCONOCIDO', '30:C9:AB:9E:B6:BD'),
(118, '10.10.179.178', '2026-05-12 01:33:55', 'DESCONOCIDO', '16:05:42:76:16:FC'),
(119, '10.10.172.17', '2026-05-12 01:33:58', 'DESCONOCIDO', 'A0:4F:52:39:2A:29'),
(120, '10.10.177.132', '2026-05-12 01:33:59', 'DESCONOCIDO', '26:01:CE:28:10:C7'),
(121, '10.10.170.156', '2026-05-12 01:34:23', 'DESCONOCIDO', 'B2:EE:9E:3B:06:78'),
(122, '10.10.178.230', '2026-05-12 01:35:33', 'DESCONOCIDO', '66:5C:FD:76:6D:03'),
(123, '10.10.172.19', '2026-05-12 01:34:34', 'DESCONOCIDO', 'D0:39:57:12:52:43'),
(124, '10.10.180.34', '2026-05-12 01:34:44', 'DESCONOCIDO', '44:F7:9F:B2:4F:1B'),
(125, '10.10.172.18', '2026-05-12 01:34:46', 'DESCONOCIDO', 'C2:3D:0D:E3:DE:F9'),
(126, '10.10.179.42', '2026-05-12 01:34:50', 'DESCONOCIDO', '22:DB:DC:A9:F6:A3'),
(127, '10.10.178.249', '2026-05-12 01:34:50', 'DESCONOCIDO', '7E:6B:6D:35:DF:08'),
(128, '10.10.180.40', '2026-05-12 01:35:19', 'DESCONOCIDO', 'DC:A9:04:99:CE:3A'),
(129, '10.10.170.171', '2026-05-12 01:35:07', 'DESCONOCIDO', '2E:83:74:FD:7E:2F'),
(130, '10.10.179.46', '2026-05-12 01:35:12', 'DESCONOCIDO', '3A:BD:34:5F:CB:EA'),
(131, '10.10.180.35', '2026-05-12 01:35:13', 'DESCONOCIDO', '54:27:1E:77:DE:09'),
(132, '10.10.179.107', '2026-05-12 01:35:33', 'DESCONOCIDO', '62:AB:6B:98:93:09'),
(133, '10.10.180.41', '2026-05-12 01:35:34', 'DESCONOCIDO', '24:EB:16:AF:24:E0'),
(134, '192.168.1.38', '2026-05-14 18:46:20', 'DESCONOCIDO', 'E2:2D:97:CC:E7:C0'),
(135, '192.168.1.46', '2026-05-15 22:10:09', 'DESCONOCIDO', '58:36:53:0B:DC:88'),
(136, '192.168.1.40', '2026-05-15 19:50:26', 'DESCONOCIDO', 'E2:A3:46:4E:1B:D0'),
(137, '192.168.1.41', '2026-05-15 19:50:27', 'DESCONOCIDO', '5C:F1:00:05:BE:8B'),
(138, '192.168.1.44', '2026-05-15 19:50:30', 'DESCONOCIDO', '6A:85:D9:22:9D:47'),
(139, '192.168.1.45', '2026-05-16 17:23:02', 'DESCONOCIDO', '32:DE:09:5C:64:E9'),
(140, '192.168.1.49', '2026-05-17 00:49:57', 'AUTORIZADO', 'CE:50:F0:FA:1B:B1'),
(141, '192.168.1.36', '2026-05-16 23:33:35', 'DESCONOCIDO', '72:7D:E3:DC:BC:CB');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `eventos`
--

CREATE TABLE `eventos` (
  `id` bigint(20) NOT NULL,
  `ip` varchar(255) NOT NULL,
  `hora` time NOT NULL,
  `fecha` date NOT NULL,
  `descripcion` varchar(255) NOT NULL,
  `usuario_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `eventos`
--

INSERT INTO `eventos` (`id`, `ip`, `hora`, `fecha`, `descripcion`, `usuario_id`) VALUES
(18, 'google.com', '10:58:40', '2025-05-10', 'Ping a google.com', 1),
(19, '192.168.1.1', '10:59:27', '2025-05-10', 'Ping a 192.168.1.1', 1),
(20, 'google.com', '11:41:16', '2025-05-10', 'Escaneo de puertos a google.com (1-1000)', 1),
(21, 'clinicacrecer.com', '09:20:16', '2025-11-24', 'Escaneo de puertos a clinicacrecer.com (1-1000)', 1),
(22, 'clinicacrecer.com', '16:19:42', '2025-11-24', 'Escaneo de puertos a clinicacrecer.com (1-1000)', 1),
(23, '192.168.1.103', '16:22:55', '2025-11-24', 'Escaneo de puertos a 192.168.1.103 (1-1000)', 1),
(24, 'clinicacrecer.com', '16:29:38', '2025-11-24', 'Escaneo de puertos a clinicacrecer.com (1-1000)', 1),
(25, 'crecer.vitraya.com.co', '16:29:58', '2025-11-24', 'Escaneo de puertos a crecer.vitraya.com.co (1-1000)', 1),
(26, '192.168.1.48', '16:31:08', '2025-11-24', 'Escaneo de puertos a 192.168.1.48 (1-1000)', 1),
(27, '192.168.1.103', '16:42:50', '2025-11-24', 'Escaneo de puertos a 192.168.1.103 (1-1000)', 1),
(28, '192.168.1.11', '13:19:20', '2026-02-17', 'Escaneo de puertos a 192.168.1.11 (1-1000)', 1),
(29, 'google.com', '08:16:42', '2026-03-20', 'Ping a google.com', 1),
(30, 'google.com', '08:17:03', '2026-03-20', 'Escaneo de puertos a google.com (1-1000)', 1),
(31, '192.168.1.35', '16:45:12', '2026-04-29', 'Ping a 192.168.1.35', 1),
(32, '192.168.1.35', '16:45:29', '2026-04-29', 'Escaneo de puertos a 192.168.1.35 (1-1000)', 1),
(33, 'google.com', '14:08:13', '2026-04-30', 'Ping a google.com', 1),
(34, 'google.com', '14:08:32', '2026-04-30', 'Escaneo de puertos a google.com (1-1000)', 1),
(35, 'google.com', '12:30:44', '2026-05-03', 'Ping a google.com', 1),
(36, '192.168.1.11', '12:31:02', '2026-05-03', 'Escaneo de puertos a 192.168.1.11 (1-1000)', 1),
(37, 'google.com', '13:46:34', '2026-05-14', 'Ping a google.com', 18),
(38, 'google.com', '13:56:51', '2026-05-14', 'Ping a google.com', 1),
(39, '0:0:0:0:0:0:0:1', '13:57:48', '2026-05-14', 'Eliminación de usuario con ID: 14', 1),
(40, 'google.com', '14:01:22', '2026-05-14', 'Ping a google.com', 1),
(41, '0:0:0:0:0:0:0:1', '14:12:09', '2026-05-14', 'Creación de usuario: miguel', 1),
(42, '0:0:0:0:0:0:0:1', '14:25:10', '2026-05-14', 'Medición QoS a google.com — Latencia: 61,1ms, MOS: 4,36, Estado: EXCELENTE', 1),
(43, '0:0:0:0:0:0:0:1', '14:25:56', '2026-05-14', 'Medición QoS a clinicacrecer.com — Latencia: 89,7ms, MOS: 4,28, Estado: BUENO', 1),
(44, '127.0.0.1', '15:05:08', '2026-05-14', 'Medición QoS a google.com — Latencia: 52,8ms, MOS: 3,87, Estado: ACEPTABLE', 1),
(45, '127.0.0.1', '15:05:45', '2026-05-14', 'Medición QoS a clinicacrecer.com — Latencia: 189,2ms, MOS: 2,81, Estado: MUY MALO', 1),
(46, '192.168.1.11', '18:29:28', '2026-05-14', 'Ping a 192.168.1.11', 2),
(47, 'google.com', '18:29:42', '2026-05-14', 'Ping a google.com', 2),
(48, '0:0:0:0:0:0:0:1', '14:26:40', '2026-05-15', 'Medición QoS a google.com — Latencia: 66,7ms, MOS: 4,32, Estado: EXCELENTE', 1),
(49, '0:0:0:0:0:0:0:1', '14:28:39', '2026-05-15', 'Medición QoS a clinicacrecer.com — Latencia: 48,0ms, MOS: 4,35, Estado: EXCELENTE', 1),
(50, '0:0:0:0:0:0:0:1', '14:48:50', '2026-05-15', 'Escaneo manual de red iniciado', 1),
(51, '0:0:0:0:0:0:0:1', '14:49:41', '2026-05-15', 'Escaneo manual de red iniciado', 1),
(52, 'google.com', '15:19:10', '2026-05-15', 'Ping a google.com', 2),
(53, '0:0:0:0:0:0:0:1', '11:18:20', '2026-05-16', 'Medición QoS a google.com — Latencia: 129,3ms, MOS: 3,72, Estado: ACEPTABLE', 1),
(54, '0:0:0:0:0:0:0:1', '11:19:32', '2026-05-16', 'Medición QoS a google.com — Latencia: 143,3ms, MOS: 3,79, Estado: ACEPTABLE', 1),
(55, '0:0:0:0:0:0:0:1', '11:20:36', '2026-05-16', 'Escaneo manual de red iniciado', 1),
(56, '0:0:0:0:0:0:0:1', '11:21:08', '2026-05-16', 'MAC autorizada agregada: AE:B1:5E:B7:E5:65', 1),
(57, '8.8.8.8', '11:55:31', '2026-05-16', 'Ping a 8.8.8.8', 1),
(58, '8.8.8.8', '11:56:05', '2026-05-16', 'Ping a 8.8.8.8', 1),
(59, '8.8.8.8', '12:07:25', '2026-05-16', 'Ping a 8.8.8.8', 1),
(60, '0:0:0:0:0:0:0:1', '12:08:13', '2026-05-16', 'Medición QoS a clinicacrecer.com — Latencia: 49,5ms, MOS: 4,36, Estado: EXCELENTE', 1),
(61, '8.8.8.8', '12:08:21', '2026-05-16', 'Ping a 8.8.8.8', 1),
(62, '8.8.8.8', '12:18:47', '2026-05-16', 'Ping a 8.8.8.8', 1),
(63, '8.8.8.8', '12:19:15', '2026-05-16', 'Ping a 8.8.8.8', 1),
(64, '8.8.8.8', '12:20:49', '2026-05-16', 'Ping a 8.8.8.8', 1),
(65, '8.8.8.8', '12:21:01', '2026-05-16', 'Ping a 8.8.8.8', 1),
(66, '8.8.8.8', '12:21:41', '2026-05-16', 'Ping a 8.8.8.8', 1),
(67, '8.8.8.8', '12:22:01', '2026-05-16', 'Ping a 8.8.8.8', 1),
(68, '8.8.8.8', '12:35:41', '2026-05-16', 'Ping a 8.8.8.8', 1),
(69, '8.8.8.8', '12:35:56', '2026-05-16', 'Ping a 8.8.8.8', 1),
(70, '0:0:0:0:0:0:0:1', '12:36:09', '2026-05-16', 'Escaneo manual de red iniciado', 1),
(71, '8.8.8.8', '12:36:13', '2026-05-16', 'Ping a 8.8.8.8', 1),
(72, 'google.com', '12:36:45', '2026-05-16', 'Ping a google.com', 2),
(73, '8.8.8.8', '12:52:52', '2026-05-16', 'Ping a 8.8.8.8', 1),
(74, '8.8.8.8', '13:06:20', '2026-05-16', 'Ping a 8.8.8.8', 2),
(75, '8.8.8.8', '13:27:15', '2026-05-16', 'Ping a 8.8.8.8', 1),
(76, '8.8.8.8', '14:06:16', '2026-05-16', 'Ping a 8.8.8.8', 1),
(77, '8.8.8.8', '14:07:04', '2026-05-16', 'Ping a 8.8.8.8', 2),
(78, '8.8.8.8', '16:24:22', '2026-05-16', 'Ping a 8.8.8.8', 1),
(79, '8.8.8.8', '16:25:54', '2026-05-16', 'Ping a 8.8.8.8', 2),
(80, '8.8.8.8', '18:28:04', '2026-05-16', 'Ping a 8.8.8.8', 1),
(81, '8.8.8.8', '18:51:59', '2026-05-16', 'Ping a 8.8.8.8', 1),
(82, '8.8.8.8', '19:04:06', '2026-05-16', 'Ping a 8.8.8.8', 1),
(83, '0:0:0:0:0:0:0:1', '19:09:54', '2026-05-16', 'Medición QoS a google.com — Latencia: 75,2ms, MOS: 4,32, Estado: EXCELENTE', 1),
(84, '8.8.8.8', '19:21:30', '2026-05-16', 'Ping a 8.8.8.8', 1),
(85, 'google.com', '19:21:37', '2026-05-16', 'Ping a google.com', 1),
(86, 'google.com', '19:22:25', '2026-05-16', 'Escaneo de puertos a google.com (1-1000)', 1),
(87, '8.8.8.8', '19:27:47', '2026-05-16', 'Ping a 8.8.8.8', 1),
(88, '8.8.8.8', '19:48:15', '2026-05-16', 'Ping a 8.8.8.8', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `macs_autorizadas`
--

CREATE TABLE `macs_autorizadas` (
  `id` bigint(20) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `fecha_registro` datetime(6) DEFAULT NULL,
  `mac` varchar(17) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `macs_autorizadas`
--

INSERT INTO `macs_autorizadas` (`id`, `descripcion`, `fecha_registro`, `mac`) VALUES
(1, '', '2026-04-29 21:43:43.000000', '64:6C:80:10:7B:33'),
(2, '', '2026-04-29 21:43:57.000000', 'CE:50:F0:FA:1B:B1'),
(3, '', '2026-04-29 21:53:08.000000', '74:54:6B:07:43:28'),
(5, '', '2026-05-16 16:21:08.000000', 'AE:B1:5E:B7:E5:65');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `qos_metrics`
--

CREATE TABLE `qos_metrics` (
  `id` bigint(20) NOT NULL,
  `jitter_ms` double DEFAULT NULL,
  `latency_ms` double DEFAULT NULL,
  `mos_score` double DEFAULT NULL,
  `packet_loss_percent` double DEFAULT NULL,
  `packets_lost` int(11) DEFAULT NULL,
  `packets_received` int(11) DEFAULT NULL,
  `packets_sent` int(11) DEFAULT NULL,
  `protocol_type` varchar(255) DEFAULT NULL,
  `quality_status` varchar(255) DEFAULT NULL,
  `rtt_avg_ms` double DEFAULT NULL,
  `rtt_max_ms` double DEFAULT NULL,
  `rtt_min_ms` double DEFAULT NULL,
  `target_ip` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `during_active_call` tinyint(1) DEFAULT 0,
  `predicted_status` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `qos_metrics`
--

INSERT INTO `qos_metrics` (`id`, `jitter_ms`, `latency_ms`, `mos_score`, `packet_loss_percent`, `packets_lost`, `packets_received`, `packets_sent`, `protocol_type`, `quality_status`, `rtt_avg_ms`, `rtt_max_ms`, `rtt_min_ms`, `target_ip`, `timestamp`, `during_active_call`, `predicted_status`) VALUES
(12, 15.473684210526315, 32.35, 4.19, 0, 0, 20, 20, 'ICMP', 'BUENO', 32.35, 161, 24, '8.8.8.8', '2025-11-15 21:43:46.000000', 0, NULL),
(17, 1.55, 91.28571428571429, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELENTE', 91.28571428571429, 97, 90, 'google.com', '2025-11-15 22:00:29.000000', 0, NULL),
(19, 1.55, 16.476190476190474, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELENTE', 16.476190476190474, 24, 15, 'youtube.com', '2025-11-15 22:03:52.000000', 0, NULL),
(20, 6.25, 18.476190476190474, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELENTE', 18.476190476190474, 34, 14, 'google.com', '2025-11-16 04:42:28.000000', 0, NULL),
(22, 8.35, 32, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELENTE', 32, 71, 23, '8.8.8.8', '2025-11-16 05:37:58.000000', 0, NULL),
(23, 6, 95.76190476190476, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELENTE', 95.76190476190476, 129, 90, 'google.com', '2025-11-16 05:42:51.000000', 0, NULL),
(24, 689.2, 579.7272727272727, 1, -10, -1, 11, 10, 'ICMP', 'MUY MALO', 579.7272727272727, 3256, 95, '8.8.8.8', '2025-11-23 15:32:58.000000', 0, NULL),
(26, 208.8, 301.3636363636364, 1, -10, -1, 11, 10, 'ICMP', 'MUY MALO', 301.3636363636364, 892, 99, '8.8.8.8', '2025-11-23 15:33:46.000000', 0, NULL),
(27, 62.7, 133.9090909090909, 4.15, -10, -1, 11, 10, 'ICMP', 'BUENO', 133.9090909090909, 280, 81, '8.8.8.8', '2025-11-23 15:34:06.000000', 0, NULL),
(28, 387.2, 395.09090909090907, 1, -10, -1, 11, 10, 'ICMP', 'MUY MALO', 395.09090909090907, 1309, 61, '8.8.8.8', '2025-11-23 15:35:08.000000', 0, NULL),
(29, 446.2, 360.54545454545456, 1, -10, -1, 11, 10, 'ICMP', 'Muy Malo', 360.54545454545456, 1664, 63, '8.8.8.8', '2025-11-23 15:35:29.000000', 0, NULL),
(30, 25.1, 105.33333333333333, 4.35, -5, -1, 21, 20, 'ICMP', 'EXCELENTE', 105.33333333333333, 158, 72, 'google.com', '2025-11-23 15:42:10.000000', 0, NULL),
(31, 42.7, 107.72727272727273, 4.43, -10, -1, 11, 10, 'ICMP', 'EXCELENTE', 107.72727272727273, 180, 81, 'google.com', '2025-11-23 15:43:47.000000', 0, NULL),
(32, 25.894736842105264, 89.85, 3.95, 0, 0, 20, 20, 'ICMP', 'Aceptable', 89.85, 184, 73, 'google.com', '2025-11-24 08:31:00.000000', 0, NULL),
(33, 5, 50.095238095238095, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELENTE', 50.095238095238095, 74, 46, 'clinicacrecer.com', '2025-11-24 14:19:25.000000', 0, NULL),
(35, 3.1, 20.818181818181817, 4.5, -10, -1, 11, 10, 'ICMP', 'EXCELENTE', 20.818181818181817, 28, 17, '8.8.8.8', '2025-11-24 21:13:10.000000', 0, NULL),
(36, 0.9, 23.363636363636363, 4.5, -10, -1, 11, 10, 'ICMP', 'EXCELENTE', 23.363636363636363, 25, 22, '8.8.8.8', '2025-11-24 21:14:46.000000', 0, NULL),
(37, 29.4, 46.36363636363637, 4.5, -10, -1, 11, 10, 'ICMP', 'EXCELENTE', 46.36363636363637, 278, 18, '8.8.8.8', '2025-11-24 21:15:18.000000', 0, NULL),
(38, 59.8, 164.52380952380952, 3.66, -5, -1, 21, 20, 'ICMP', 'ACEPTABLE', 164.52380952380952, 361, 55, 'clinicacrecer.com', '2025-11-24 21:18:27.000000', 0, NULL),
(39, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'Muy Malo', 0, 0, 0, '172.30.252.1', '2025-11-24 21:22:24.000000', 0, NULL),
(40, 2.5, 29.333333333333332, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELENTE', 29.333333333333332, 38, 26, 'google.com', '2026-02-17 22:40:13.000000', 0, NULL),
(42, 78.94444444444444, 186.52631578947367, 1.81, 5, 1, 19, 20, 'ICMP', 'MUY MALO', 186.52631578947367, 1006, 51, 'clinicacrecer.com', '2026-03-18 20:24:30.000000', 0, NULL),
(43, 50.578947368421055, 52.35, 3.43, 0, 0, 20, 20, 'ICMP', 'MALO', 52.35, 272, 17, 'google.com', '2026-03-21 15:53:00.000000', 0, NULL),
(44, 69.89473684210526, 81.15, 2.9, 0, 0, 20, 20, 'ICMP', 'Muy Malo', 81.15, 876, 16, 'google.com', '2026-04-29 18:01:47.000000', 0, NULL),
(46, 95.66666666666667, 299.9, 1.5, 0, 0, 10, 10, 'ICMP', 'Muy Malo', 299.9, 831, 180, 'hospifood.com', '2026-04-29 18:16:54.000000', 0, NULL),
(48, 5, 67.5, 4.31, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 67.5, 84, 62, 'google.com', '2026-04-30 17:01:13.000000', 0, NULL),
(49, 28.263157894736842, 109.45, 3.88, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 109.45, 183, 60, 'google.com', '2026-04-30 19:01:58.000000', 0, NULL),
(53, 3.4444444444444446, 19.4, 4.36, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 19.4, 26, 16, 'youtube.com', '2026-05-03 17:51:18.000000', 0, NULL),
(56, 40.578947368421055, 67.25, 3.65, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 67.25, 341, 21, 'google.com', '2026-05-03 18:22:53.000000', 0, NULL),
(61, 39.31578947368421, 114.65, 3.63, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 114.65, 252, 49, 'clinicacrecer.com', '2026-05-03 19:03:00.000000', 0, NULL),
(62, 3.1052631578947367, 71.35, 4.34, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 71.35, 81, 68, 'google.com', '2026-05-03 23:14:53.000000', 0, NULL),
(63, 42.63157894736842, 114.8, 3.55, 0, 0, 20, 20, 'ICMP', 'MALO', 114.8, 321, 69, 'google.com', '2026-05-03 23:15:05.000000', 0, NULL),
(66, 4.473684210526316, 51.05, 4.33, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 51.05, 59, 46, 'clinicacrecer.com', '2026-05-03 23:22:41.000000', 0, NULL),
(67, 4.315789473684211, 50.3, 4.33, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 50.3, 62, 46, 'clinicacrecer.com', '2026-05-03 23:22:52.000000', 0, NULL),
(68, 4.526315789473684, 22.5, 4.35, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 22.5, 29, 18, 'google.com', '2026-05-03 23:44:09.000000', 0, NULL),
(71, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, 'netflix.com', '2026-05-09 13:38:11.000000', 0, NULL),
(72, 14.11111111111111, 34.4, 4.21, 0, 0, 10, 10, 'ICMP', 'BUENO', 34.4, 85, 16, 'claude.com', '2026-05-09 13:38:39.000000', 0, NULL),
(73, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, 'https://chatgpt.com/', '2026-05-09 13:38:52.000000', 0, NULL),
(74, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, 'chatgpt.com/', '2026-05-09 13:38:58.000000', 0, NULL),
(75, 441, 333.2, 1, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 333.2, 1372, 16, 'chatgpt.com', '2026-05-09 13:39:17.000000', 0, NULL),
(76, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, 'computrabajo.com', '2026-05-09 13:40:23.000000', 0, NULL),
(77, 8.555555555555555, 23.2, 4.3, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 23.2, 51, 15, '8.8.8.8', '2026-05-09 13:40:47.000000', 0, NULL),
(78, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, '192.168.1.43', '2026-05-09 13:41:21.000000', 0, NULL),
(79, 1.7894736842105263, 3.5, 4.39, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 3.5, 8, 2, '192.168.1.1', '2026-05-09 13:41:51.000000', 0, NULL),
(80, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, '10.244.19.197', '2026-05-09 13:43:42.000000', 0, NULL),
(81, 15.263157894736842, 23.9, 4.2, 0, 0, 20, 20, 'ICMP', 'BUENO', 23.9, 147, 15, 'google.com', '2026-05-09 13:51:32.000000', 0, NULL),
(82, 2.6842105263157894, 18, 4.37, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 18, 27, 16, 'google.com', '2026-05-09 13:51:54.000000', 0, NULL),
(83, 28.842105263157894, 36.4, 3.94, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 36.4, 160, 15, 'google.com', '2026-05-09 13:52:14.000000', 0, NULL),
(84, 2.6842105263157894, 17.55, 4.37, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 17.55, 28, 15, 'google.com', '2026-05-09 13:52:38.000000', 0, NULL),
(85, 2, 17, 4.38, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 17, 20, 16, 'google.com', '2026-05-09 13:52:59.000000', 0, NULL),
(86, 3.6315789473684212, 18.15, 4.36, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 18.15, 28, 15, 'google.com', '2026-05-09 13:56:00.000000', 0, NULL),
(87, 24.42105263157895, 39.95, 4.02, 0, 0, 20, 20, 'ICMP', 'BUENO', 39.95, 159, 15, 'google.com', '2026-05-09 13:56:46.000000', 0, NULL),
(88, 3.3157894736842106, 18.15, 4.36, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 18.15, 36, 15, 'google.com', '2026-05-09 13:58:02.000000', 0, NULL),
(100, 565.471596, 579.44611, 1.72, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 579.44611, 4586.58402, 81.072985, '8.8.8.8', '2025-11-19 18:07:14.000000', 0, NULL),
(101, 451.797463, 319.585231, 1.49, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 319.585231, 1123.240016, 51.381874, '8.8.8.8', '2026-04-23 22:38:24.000000', 0, NULL),
(102, 49.22763, 43.519513, 3.8, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 43.519513, 113.628665, 15.163819, 'hospifood.com', '2025-11-23 07:26:59.000000', 0, NULL),
(103, 27.772329, 15.077674, 4.38, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 15.077674, 33.628104, 10.900575, 'youtube.com', '2025-12-02 23:08:14.000000', 0, NULL),
(104, 26.876325, 62.786616, 3.6, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 62.786616, 247.500713, 32.860153, 'hospifood.com', '2026-03-19 18:45:56.000000', 0, NULL),
(105, 34.459501, 96.25118, 4.16, 0, 0, 10, 10, 'ICMP', 'BUENO', 96.25118, 345.267844, 43.031804, 'claude.com', '2026-01-04 14:31:47.000000', 0, NULL),
(106, 5.943076, 84.406514, 4.34, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 84.406514, 194.667241, 43.973612, 'clinicacrecer.com', '2026-05-05 15:19:10.000000', 0, NULL),
(107, 259.160001, 654.271623, 1.46, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 654.271623, 5040.103258, 50.483176, 'chatgpt.com', '2026-03-18 22:32:32.000000', 0, NULL),
(108, 18.622258, 13.200841, 4.26, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 13.200841, 31.176141, 7.169114, 'google.com', '2026-04-01 20:28:24.000000', 0, NULL),
(109, 601.101876, 686.835747, 1.23, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 686.835747, 2524.542495, 42.629419, '172.30.252.1', '2026-03-07 23:38:08.000000', 0, NULL),
(110, 88.856027, 116.681754, 3.35, 0, 0, 20, 20, 'ICMP', 'MALO', 116.681754, 535.81633, 26.650293, 'chatgpt.com', '2026-03-06 19:49:33.000000', 0, NULL),
(111, 58.412509, 121.202681, 3.58, 0, 0, 20, 20, 'ICMP', 'MALO', 121.202681, 814.223136, 57.896982, 'chatgpt.com', '2026-02-11 21:24:18.000000', 0, NULL),
(112, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, '192.168.1.43', '2026-03-10 21:16:39.000000', 0, NULL),
(113, 32.591122, 36.261397, 4.12, 0, 0, 20, 20, 'ICMP', 'BUENO', 36.261397, 120.314165, 24.639083, 'clinicacrecer.com', '2025-12-15 19:28:34.000000', 0, NULL),
(114, 57.194414, 175.171157, 3.58, 0, 0, 20, 20, 'ICMP', 'MALO', 175.171157, 919.993056, 91.507562, 'clinicacrecer.com', '2025-11-15 23:45:36.000000', 0, NULL),
(115, 364.873042, 301.338407, 1.77, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 301.338407, 1783.19166, 93.416353, '172.30.252.1', '2026-01-19 22:47:46.000000', 0, NULL),
(116, 11.711474, 52.545525, 4.04, 0, 0, 10, 10, 'ICMP', 'BUENO', 52.545525, 193.831783, 42.624286, 'claude.com', '2026-01-03 13:18:29.000000', 0, NULL),
(117, 27.982516, 3.041574, 4.27, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 3.041574, 6.356505, 2.141658, 'clinicacrecer.com', '2025-11-17 19:30:28.000000', 0, NULL),
(118, 39.362989, 88.449073, 3.7, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 88.449073, 155.341411, 59.728991, 'google.com', '2025-12-15 13:24:19.000000', 0, NULL),
(119, 2.414598, 33.952186, 4.38, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 33.952186, 82.795025, 23.773964, '8.8.8.8', '2025-11-19 07:04:09.000000', 0, NULL),
(120, 29.704694, 45.667817, 4.2, 0, 0, 10, 10, 'ICMP', 'BUENO', 45.667817, 113.783582, 34.255853, 'google.com', '2026-04-03 05:56:51.000000', 0, NULL),
(121, 71.407478, 62.277924, 3.48, 0, 0, 20, 20, 'ICMP', 'MALO', 62.277924, 295.497195, 29.913122, 'google.com', '2026-05-05 14:45:05.000000', 0, NULL),
(122, 310.721435, 360.373901, 1.23, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 360.373901, 1751.628479, 21.544177, 'hospifood.com', '2026-02-16 03:52:20.000000', 0, NULL),
(123, 274.61531, 519.061983, 1.65, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 519.061983, 3648.494808, 83.054508, '172.30.252.1', '2026-03-27 22:39:13.000000', 0, NULL),
(124, 59.363264, 88.734959, 4.04, 0, 0, 10, 10, 'ICMP', 'BUENO', 88.734959, 312.703463, 37.998877, '8.8.8.8', '2026-04-09 15:48:08.000000', 0, NULL),
(125, 41.746779, 92.371232, 4.47, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 92.371232, 140.760264, 77.208987, 'youtube.com', '2026-03-14 08:09:38.000000', 0, NULL),
(126, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, '192.168.1.43', '2025-12-14 15:23:40.000000', 0, NULL),
(127, 20.482585, 28.341537, 4.5, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 28.341537, 38.812548, 23.369983, 'youtube.com', '2026-02-20 02:16:24.000000', 0, NULL),
(128, 17.822037, 93.807557, 4.38, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 93.807557, 231.226378, 51.649314, 'claude.com', '2026-04-25 06:04:23.000000', 0, NULL),
(129, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, '10.244.19.197', '2026-04-23 11:09:24.000000', 0, NULL),
(130, 27.29447, 66.475034, 3.77, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 66.475034, 230.991435, 38.453485, 'clinicacrecer.com', '2026-04-20 09:55:44.000000', 0, NULL),
(131, 40.534936, 142.063671, 4.09, 0, 0, 20, 20, 'ICMP', 'BUENO', 142.063671, 277.859297, 80.518126, 'clinicacrecer.com', '2025-11-23 21:56:31.000000', 0, NULL),
(132, 8.60816, 114.717992, 4.43, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 114.717992, 232.222085, 58.272712, 'youtube.com', '2026-03-17 19:28:57.000000', 0, NULL),
(133, 39.897853, 99.763722, 4.13, 0, 0, 10, 10, 'ICMP', 'BUENO', 99.763722, 258.005312, 48.345293, 'clinicacrecer.com', '2026-05-03 12:18:50.000000', 0, NULL),
(134, 21.327311, 117.563794, 4.19, 0, 0, 10, 10, 'ICMP', 'BUENO', 117.563794, 348.666468, 67.355537, 'clinicacrecer.com', '2025-12-20 00:11:36.000000', 0, NULL),
(135, 56.555879, 97.579217, 3.91, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 97.579217, 255.395723, 47.854033, 'clinicacrecer.com', '2026-01-23 15:53:14.000000', 0, NULL),
(136, 61.276229, 97.414715, 4.2, 0, 0, 10, 10, 'ICMP', 'BUENO', 97.414715, 278.499011, 58.295363, 'google.com', '2026-02-14 06:33:51.000000', 0, NULL),
(137, 15.492769, 22.904846, 4.36, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 22.904846, 27.54553, 15.036512, 'claude.com', '2026-04-10 12:14:14.000000', 0, NULL),
(138, 44.613382, 124.641355, 3.28, 0, 0, 20, 20, 'ICMP', 'MALO', 124.641355, 711.894117, 25.38656, 'hospifood.com', '2026-03-10 16:17:26.000000', 0, NULL),
(139, 62.544438, 62.459943, 3.16, 0, 0, 20, 20, 'ICMP', 'MALO', 62.459943, 230.651917, 13.101595, 'hospifood.com', '2026-03-14 11:00:36.000000', 0, NULL),
(140, 30.066827, 39.409271, 3.93, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 39.409271, 139.47866, 18.897297, 'clinicacrecer.com', '2026-03-04 23:16:41.000000', 0, NULL),
(141, 23.219622, 51.471737, 4.3, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 51.471737, 84.771011, 44.678602, 'clinicacrecer.com', '2025-12-04 18:05:34.000000', 0, NULL),
(142, 63.991504, 140.554314, 3.21, 0, 0, 20, 20, 'ICMP', 'MALO', 140.554314, 889.727547, 47.832622, 'google.com', '2026-02-14 09:15:57.000000', 0, NULL),
(143, 48.263323, 34.298313, 4.1, 0, 0, 10, 10, 'ICMP', 'BUENO', 34.298313, 56.390702, 15.792603, 'google.com', '2026-01-06 08:42:24.000000', 0, NULL),
(144, 47.986227, 144.318312, 3.25, 0, 0, 20, 20, 'ICMP', 'MALO', 144.318312, 485.77315, 61.739294, 'chatgpt.com', '2026-05-06 16:03:27.000000', 0, NULL),
(145, 56.290714, 59.184196, 4, 0, 0, 20, 20, 'ICMP', 'BUENO', 59.184196, 193.666583, 44.992565, 'clinicacrecer.com', '2026-01-29 02:54:30.000000', 0, NULL),
(146, 41.867668, 18.74358, 4.31, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 18.74358, 46.134635, 16.696361, '8.8.8.8', '2026-01-07 02:40:25.000000', 0, NULL),
(147, 42.075077, 65.348457, 3.72, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 65.348457, 175.196147, 41.787885, 'youtube.com', '2026-04-27 15:37:59.000000', 0, NULL),
(148, 69.095033, 149.428366, 3.4, 0, 0, 20, 20, 'ICMP', 'MALO', 149.428366, 1017.139946, 73.990988, 'chatgpt.com', '2026-02-02 15:46:20.000000', 0, NULL),
(149, 37.406419, 61.555551, 4.34, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 61.555551, 115.887106, 35.083677, 'clinicacrecer.com', '2026-01-29 04:27:13.000000', 0, NULL),
(150, 590.46326, 217.455651, 1.3, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 217.455651, 1333.34836, 89.945079, 'hospifood.com', '2026-03-15 10:26:49.000000', 0, NULL),
(151, 21.531072, 20.04355, 4.03, 0, 0, 20, 20, 'ICMP', 'BUENO', 20.04355, 86.725513, 16.277241, '8.8.8.8', '2025-12-06 06:21:31.000000', 0, NULL),
(152, 59.621337, 60.652869, 3.96, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 60.652869, 188.828568, 28.440158, 'clinicacrecer.com', '2026-03-06 16:17:18.000000', 0, NULL),
(153, 52.013628, 69.57555, 3.45, 0, 0, 20, 20, 'ICMP', 'MALO', 69.57555, 277.990842, 31.966927, 'chatgpt.com', '2025-12-21 19:58:00.000000', 0, NULL),
(154, 63.82502, 140.072063, 3.82, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 140.072063, 445.907126, 72.739183, 'hospifood.com', '2026-04-14 12:25:15.000000', 0, NULL),
(155, 388.633859, 454.669911, 1.65, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 454.669911, 2555.766932, 53.86723, '172.30.252.1', '2025-12-21 23:17:34.000000', 0, NULL),
(156, 16.414343, 119.465327, 4.42, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 119.465327, 133.581164, 61.806885, 'youtube.com', '2026-01-04 01:41:50.000000', 0, NULL),
(157, 19.167583, 63.911253, 4.41, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 63.911253, 146.118923, 44.995215, 'clinicacrecer.com', '2025-12-06 17:43:39.000000', 0, NULL),
(158, 277.342743, 285.640943, 1.3, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 285.640943, 2084.347482, 80.310759, 'chatgpt.com', '2026-02-28 08:57:18.000000', 0, NULL),
(159, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, '10.244.19.197', '2026-05-03 02:37:52.000000', 0, NULL),
(160, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, 'computrabajo.com', '2026-01-13 20:14:05.000000', 0, NULL),
(161, 60.922741, 141.032024, 3.86, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 141.032024, 305.834516, 78.898543, 'youtube.com', '2025-12-30 08:33:58.000000', 0, NULL),
(162, 231.44283, 320.976472, 1.55, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 320.976472, 2219.918812, 69.76832, 'hospifood.com', '2025-11-29 13:53:46.000000', 0, NULL),
(163, 6.799053, 119.781705, 4.31, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 119.781705, 281.264893, 101.597273, 'google.com', '2025-11-23 16:52:19.000000', 0, NULL),
(164, 371.85268, 631.427455, 1.56, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 631.427455, 3130.759189, 32.320683, 'hospifood.com', '2025-12-01 19:34:19.000000', 0, NULL),
(165, 41.660675, 101.319805, 4.45, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 101.319805, 112.297288, 75.371839, 'clinicacrecer.com', '2026-04-17 22:10:43.000000', 0, NULL),
(166, 30.357268, 16.188181, 4.26, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 16.188181, 22.611903, 13.415449, '8.8.8.8', '2026-02-15 05:33:57.000000', 0, NULL),
(167, 20.121573, 71.371802, 4.12, 0, 0, 20, 20, 'ICMP', 'BUENO', 71.371802, 272.130865, 45.54327, 'clinicacrecer.com', '2025-11-15 21:07:47.000000', 0, NULL),
(168, 15.401625, 61.378144, 4.04, 0, 0, 10, 10, 'ICMP', 'BUENO', 61.378144, 184.485437, 30.602088, '8.8.8.8', '2026-02-26 02:10:15.000000', 0, NULL),
(169, 405.695135, 533.299266, 1.33, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 533.299266, 2162.392864, 56.906524, '172.30.252.1', '2026-01-07 23:41:12.000000', 0, NULL),
(170, 16.208772, 55.06866, 4.26, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 55.06866, 90.688211, 35.525738, '192.168.1.1', '2026-03-25 03:27:13.000000', 0, NULL),
(171, 55.238154, 160.97359, 3.79, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 160.97359, 561.679682, 104.929103, 'youtube.com', '2026-02-03 22:01:02.000000', 0, NULL),
(172, 16.920524, 66.818556, 4.32, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 66.818556, 105.313607, 57.288454, '192.168.1.1', '2026-03-24 08:51:21.000000', 0, NULL),
(173, 50.961084, 82.005511, 3.74, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 82.005511, 318.690437, 28.703059, 'google.com', '2026-02-02 02:36:10.000000', 0, NULL),
(174, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, '192.168.1.43', '2025-12-27 03:05:58.000000', 0, NULL),
(175, 33.490539, 53.565324, 4.36, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 53.565324, 60.467607, 36.525993, '192.168.1.1', '2026-05-05 16:25:32.000000', 0, NULL),
(176, 17.865359, 62.68217, 4.15, 0, 0, 20, 20, 'ICMP', 'BUENO', 62.68217, 158.015478, 44.120409, '8.8.8.8', '2026-02-11 08:20:02.000000', 0, NULL),
(177, 57.432554, 138.104345, 4.12, 0, 0, 10, 10, 'ICMP', 'BUENO', 138.104345, 227.81155, 60.815751, 'clinicacrecer.com', '2026-02-02 01:08:14.000000', 0, NULL),
(178, 54.055668, 55.862341, 4.07, 0, 0, 20, 20, 'ICMP', 'BUENO', 55.862341, 275.970954, 44.125628, 'google.com', '2026-01-14 00:11:41.000000', 0, NULL),
(179, 40.746131, 45.708844, 3.94, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 45.708844, 79.824072, 22.232558, 'hospifood.com', '2026-02-05 13:56:10.000000', 0, NULL),
(180, 600.476644, 689.405222, 1.33, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 689.405222, 3527.711231, 86.372674, 'chatgpt.com', '2026-02-14 19:29:07.000000', 0, NULL),
(181, 46.03854, 76.755789, 4.11, 0, 0, 10, 10, 'ICMP', 'BUENO', 76.755789, 337.485392, 52.356233, '8.8.8.8', '2026-02-18 10:47:11.000000', 0, NULL),
(182, 29.701499, 4.281868, 4.49, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 4.281868, 4.96088, 3.077683, 'clinicacrecer.com', '2026-03-06 10:36:32.000000', 0, NULL),
(183, 28.761644, 66.9502, 4.49, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 66.9502, 103.869295, 34.255388, 'clinicacrecer.com', '2025-11-23 20:26:48.000000', 0, NULL),
(184, 14.537924, 11.175604, 4.27, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 11.175604, 26.458892, 7.591908, 'google.com', '2025-12-21 04:35:47.000000', 0, NULL),
(185, 58.412271, 58.514246, 4.01, 0, 0, 10, 10, 'ICMP', 'BUENO', 58.514246, 131.18224, 28.671038, 'google.com', '2025-12-01 05:36:19.000000', 0, NULL),
(186, 44.634793, 77.739698, 4.31, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 77.739698, 154.07907, 68.848117, '192.168.1.1', '2026-03-18 06:27:57.000000', 0, NULL),
(187, 20.460734, 96.338916, 4.41, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 96.338916, 164.327494, 50.487446, 'clinicacrecer.com', '2026-03-27 12:58:42.000000', 0, NULL),
(188, 33.039328, 19.289051, 4.37, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 19.289051, 24.619972, 16.686012, 'google.com', '2026-02-13 20:05:25.000000', 0, NULL),
(189, 28.756722, 90.972632, 3.75, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 90.972632, 251.880633, 38.176656, 'google.com', '2026-02-08 13:23:25.000000', 0, NULL),
(190, 64.211956, 129.123879, 3.61, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 129.123879, 198.356169, 61.639129, 'youtube.com', '2026-02-13 13:17:39.000000', 0, NULL),
(191, 780.853377, 231.923729, 1.9, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 231.923729, 1514.408976, 65.367429, 'hospifood.com', '2026-05-05 05:49:35.000000', 0, NULL),
(192, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, '10.244.19.197', '2026-04-05 05:01:14.000000', 0, NULL),
(193, 44.436146, 139.21471, 3.71, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 139.21471, 372.607202, 78.996388, 'clinicacrecer.com', '2026-05-07 18:23:02.000000', 0, NULL),
(194, 40.282985, 139.345834, 3.21, 0, 0, 20, 20, 'ICMP', 'MALO', 139.345834, 965.235461, 79.20906, 'chatgpt.com', '2026-04-17 08:21:30.000000', 0, NULL),
(195, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, 'computrabajo.com', '2026-05-05 22:38:52.000000', 0, NULL),
(196, 14.607755, 12.956703, 4.48, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 12.956703, 24.005594, 8.246393, 'youtube.com', '2026-03-31 04:55:53.000000', 0, NULL),
(197, 47.923097, 53.347343, 3.18, 0, 0, 20, 20, 'ICMP', 'MALO', 53.347343, 293.204729, 17.776155, 'chatgpt.com', '2025-11-25 03:39:41.000000', 0, NULL),
(198, 16.068051, 88.770811, 4.12, 0, 0, 10, 10, 'ICMP', 'BUENO', 88.770811, 206.006528, 45.48212, 'google.com', '2025-11-15 22:00:58.000000', 0, NULL),
(199, 232.529499, 636.265732, 1.58, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 636.265732, 2344.00167, 36.039621, 'hospifood.com', '2026-02-20 13:46:54.000000', 0, NULL),
(200, 36.482469, 77.670512, 3.69, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 77.670512, 208.09651, 33.236501, 'hospifood.com', '2025-11-23 22:24:52.000000', 0, NULL),
(201, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, '192.168.1.43', '2026-01-17 08:07:04.000000', 0, NULL),
(202, 58.625259, 133.387439, 3.66, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 133.387439, 437.236019, 76.876798, 'google.com', '2025-11-25 05:06:29.000000', 0, NULL),
(203, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, 'netflix.com', '2026-01-15 08:33:20.000000', 0, NULL),
(204, 256.41526, 400.710096, 1.65, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 400.710096, 1467.292552, 95.906015, '8.8.8.8', '2026-01-19 08:48:44.000000', 0, NULL),
(205, 88.348257, 68.16547, 3.32, 0, 0, 20, 20, 'ICMP', 'MALO', 68.16547, 217.800345, 21.930461, 'clinicacrecer.com', '2026-04-21 08:26:49.000000', 0, NULL),
(206, 49.721864, 166.881468, 3.65, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 166.881468, 410.40098, 65.519588, 'google.com', '2026-05-07 03:37:12.000000', 0, NULL),
(207, 31.998731, 139.90526, 3.76, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 139.90526, 527.457313, 71.087099, 'clinicacrecer.com', '2025-12-06 21:14:06.000000', 0, NULL),
(208, 294.374369, 673.910247, 1.51, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 673.910247, 2243.887194, 73.531679, 'chatgpt.com', '2026-03-24 17:29:11.000000', 0, NULL),
(209, 39.995761, 57.839655, 3.75, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 57.839655, 157.733037, 30.907837, 'clinicacrecer.com', '2026-03-02 06:27:06.000000', 0, NULL),
(210, 17.436037, 9.424654, 4.37, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 9.424654, 14.305869, 6.921349, 'youtube.com', '2026-02-06 08:50:07.000000', 0, NULL),
(211, 49.294728, 37.047847, 4.2, 0, 0, 10, 10, 'ICMP', 'BUENO', 37.047847, 71.29468, 16.509546, '8.8.8.8', '2026-02-15 02:04:02.000000', 0, NULL),
(212, 45.410304, 39.52316, 4.18, 0, 0, 10, 10, 'ICMP', 'BUENO', 39.52316, 158.042827, 17.563113, '8.8.8.8', '2026-03-15 22:24:03.000000', 0, NULL),
(213, 28.941193, 43.758941, 4.35, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 43.758941, 102.283557, 38.852445, 'youtube.com', '2026-05-07 05:14:32.000000', 0, NULL),
(214, 37.201683, 90.061339, 4.35, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 90.061339, 194.648501, 67.636864, 'youtube.com', '2026-02-01 05:19:14.000000', 0, NULL),
(215, 61.41549, 145.785747, 3.38, 0, 0, 20, 20, 'ICMP', 'MALO', 145.785747, 533.967357, 51.176527, 'clinicacrecer.com', '2026-02-24 19:27:48.000000', 0, NULL),
(216, 37.506372, 71.585605, 4.5, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 71.585605, 168.556137, 46.201789, 'google.com', '2026-02-26 19:22:09.000000', 0, NULL),
(217, 69.377242, 178.615999, 3.36, 0, 0, 20, 20, 'ICMP', 'MALO', 178.615999, 605.157787, 103.147393, 'clinicacrecer.com', '2026-01-13 18:09:14.000000', 0, NULL),
(218, 80.034797, 121.826733, 3.45, 0, 0, 20, 20, 'ICMP', 'MALO', 121.826733, 541.289468, 63.004532, 'clinicacrecer.com', '2025-12-04 09:46:00.000000', 0, NULL),
(219, 21.027675, 47.295049, 4.26, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 47.295049, 82.240247, 23.839581, '8.8.8.8', '2026-02-13 09:54:02.000000', 0, NULL),
(220, 28.924667, 99.088839, 4.25, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 99.088839, 242.251293, 71.931126, 'clinicacrecer.com', '2026-04-29 18:40:07.000000', 0, NULL),
(221, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, '192.168.1.43', '2026-01-17 16:06:21.000000', 0, NULL),
(222, 58.616681, 41.018504, 4.01, 0, 0, 20, 20, 'ICMP', 'BUENO', 41.018504, 70.84777, 31.692144, 'clinicacrecer.com', '2026-04-09 14:20:01.000000', 0, NULL),
(223, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, '192.168.1.43', '2026-03-22 17:25:54.000000', 0, NULL),
(224, 20.045364, 34.111975, 4.42, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 34.111975, 49.386589, 24.854273, 'claude.com', '2026-03-23 20:56:00.000000', 0, NULL),
(225, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, 'netflix.com', '2026-03-13 02:18:50.000000', 0, NULL),
(226, 584.222261, 427.067605, 1.8, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 427.067605, 1835.367845, 35.181885, '172.30.252.1', '2025-12-31 17:53:45.000000', 0, NULL),
(227, 163.442914, 556.204252, 1.04, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 556.204252, 4026.761792, 67.198987, '172.30.252.1', '2026-04-23 14:13:34.000000', 0, NULL),
(228, 33.319591, 148.180733, 3.69, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 148.180733, 398.556648, 58.722417, 'youtube.com', '2026-02-02 05:55:20.000000', 0, NULL),
(229, 50.776274, 55.367326, 3.18, 0, 0, 20, 20, 'ICMP', 'MALO', 55.367326, 253.48502, 14.943713, 'clinicacrecer.com', '2026-04-06 19:05:05.000000', 0, NULL),
(230, 45.418132, 102.064253, 4.07, 0, 0, 10, 10, 'ICMP', 'BUENO', 102.064253, 389.338049, 76.744728, '8.8.8.8', '2026-03-25 15:19:15.000000', 0, NULL),
(231, 53.30068, 123.016135, 3.58, 0, 0, 20, 20, 'ICMP', 'MALO', 123.016135, 649.278595, 72.105384, 'hospifood.com', '2025-11-17 03:17:45.000000', 0, NULL),
(232, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, 'computrabajo.com', '2026-01-09 17:22:03.000000', 0, NULL),
(233, 35.074473, 88.475558, 4.39, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 88.475558, 200.475693, 53.327819, 'clinicacrecer.com', '2026-03-19 12:23:52.000000', 0, NULL),
(234, 31.689622, 143.977004, 3.67, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 143.977004, 483.889579, 82.694169, 'clinicacrecer.com', '2026-04-06 09:04:00.000000', 0, NULL),
(235, 1.757483, 95.559771, 4.38, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 95.559771, 221.694133, 67.717123, 'youtube.com', '2025-12-20 12:30:32.000000', 0, NULL),
(236, 26.01666, 109.604614, 4.44, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 109.604614, 249.128296, 66.4345, 'google.com', '2026-02-10 18:59:36.000000', 0, NULL),
(237, 44.982287, 88.958132, 3.69, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 88.958132, 174.235825, 55.103137, 'youtube.com', '2026-03-01 17:46:42.000000', 0, NULL),
(238, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY MALO', 0, 0, 0, 'netflix.com', '2025-11-24 02:32:56.000000', 0, NULL),
(239, 45.360707, 102.738511, 4.15, 0, 0, 20, 20, 'ICMP', 'BUENO', 102.738511, 308.238344, 59.805824, 'claude.com', '2025-12-07 01:35:02.000000', 0, NULL),
(240, 40.590728, 85.572618, 4.31, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 85.572618, 181.566446, 43.263391, '192.168.1.1', '2025-12-01 09:13:28.000000', 0, NULL),
(241, 69.404066, 88.57145, 3.56, 0, 0, 20, 20, 'ICMP', 'MALO', 88.57145, 567.103095, 19.456877, 'clinicacrecer.com', '2026-01-03 22:14:59.000000', 0, NULL),
(242, 51.852337, 128.786374, 3.22, 0, 0, 20, 20, 'ICMP', 'MALO', 128.786374, 838.298508, 31.012998, 'google.com', '2026-02-01 06:13:10.000000', 0, NULL),
(243, 54.605555, 85.419083, 3.99, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 85.419083, 291.192339, 54.10436, 'youtube.com', '2026-04-30 12:19:44.000000', 0, NULL),
(244, 7.604977, 102.694134, 4.31, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 102.694134, 160.954052, 65.199136, 'youtube.com', '2025-11-15 15:53:03.000000', 0, NULL),
(252, 450.89473684210526, 704.65, 1, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 704.65, 1745, 188, 'google.com', '2026-05-11 16:47:02.000000', 0, NULL),
(253, 591.3684210526316, 808.35, 1, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 808.35, 1664, 200, 'google.com', '2026-05-11 16:52:25.000000', 0, NULL),
(254, 695.0526315789474, 928.85, 1, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 928.85, 1679, 149, 'clinicacrecer.com', '2026-05-11 16:53:07.000000', 0, NULL),
(255, 569.3684210526316, 722.1, 1, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 722.1, 1524, 53, 'youtube.com', '2026-05-11 16:53:43.000000', 0, NULL),
(256, 298.8888888888889, 260.8421052631579, 1, 5, 1, 19, 20, 'ICMP', 'MUY MALO', 260.8421052631579, 1096, 46, 'google.com', '2026-05-11 17:02:31.000000', 0, NULL),
(257, 476.42105263157896, 572.2, 1, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 572.2, 1325, 172, 'clinicacrecer.com', '2026-05-11 17:03:33.000000', 0, NULL),
(258, 3.1578947368421053, 18.1, 4.37, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 18.1, 31, 15, 'google.com', '2026-05-11 22:35:47.000000', 0, NULL),
(259, 1.4210526315789473, 98.25, 4.34, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 98.25, 101, 97, 'google.com', '2026-05-11 22:42:24.000000', 0, 'ESTABLE'),
(260, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, 'netflix', '2026-05-11 22:42:48.000000', 0, 'CRITICO'),
(261, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, 'netflix.com', '2026-05-11 22:44:31.000000', 0, 'CRITICO'),
(262, 5.631578947368421, 75.2, 4.3, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 75.2, 96, 70, 'clinicacrecer.com', '2026-05-11 22:45:22.000000', 0, 'ESTABLE'),
(263, 6.105263157894737, 93.3, 4.28, 0, 0, 20, 20, 'ICMP', 'BUENO', 93.3, 104, 89, 'youtube.com', '2026-05-11 22:51:04.000000', 0, 'ESTABLE'),
(264, 5.894736842105263, 95.1, 4.28, 0, 0, 20, 20, 'ICMP', 'BUENO', 95.1, 119, 89, 'youtube.com', '2026-05-11 22:51:31.000000', 0, 'ESTABLE'),
(265, 5.368421052631579, 96.2, 4.29, 0, 0, 20, 20, 'ICMP', 'BUENO', 96.2, 107, 89, 'youtube.com', '2026-05-11 22:51:56.000000', 0, 'ESTABLE'),
(266, 2.210526315789474, 72.4, 4.35, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 72.4, 79, 70, 'clinicacrecer.com', '2026-05-11 22:59:16.000000', 0, 'ESTABLE'),
(267, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, '192.168.1.33', '2026-05-11 23:00:10.000000', 0, 'CRITICO'),
(268, 2.9473684210526314, 4.4, 4.38, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 4.4, 16, 2, '192.168.1.1', '2026-05-11 23:00:48.000000', 0, 'ESTABLE'),
(269, 4.333333333333333, 18.9, 4.35, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 18.9, 29, 16, '8.8.8.8', '2026-05-11 23:05:09.000000', 0, 'ESTABLE'),
(270, 2.888888888888889, 18.3, 4.37, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 18.3, 26, 16, '8.8.8.8', '2026-05-11 23:05:29.000000', 0, 'ESTABLE'),
(271, 1.5555555555555556, 17, 4.38, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 17, 20, 16, '8.8.8.8', '2026-05-11 23:05:48.000000', 0, 'ESTABLE'),
(272, 5.444444444444445, 21.9, 4.34, 0, 0, 10, 10, 'ICMP', 'EXCELENTE', 21.9, 32, 16, '8.8.8.8', '2026-05-11 23:06:18.000000', 0, 'ESTABLE'),
(273, 2.8421052631578947, 99.5, 4.32, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 99.5, 106, 97, 'google.com', '2026-05-11 23:07:53.000000', 0, 'ESTABLE'),
(274, 102.11111111111111, 212.1, 1.73, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 212.1, 745, 72, '8.8.8.8', '2026-05-12 01:04:32.000000', 0, 'CRITICO'),
(275, 127.88888888888889, 155.5, 1.43, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 155.5, 394, 66, '8.8.8.8', '2026-05-12 01:08:19.000000', 0, 'CRITICO'),
(276, 80.88888888888889, 177.7, 2.42, 0, 0, 10, 10, 'ICMP', 'MUY MALO', 177.7, 678, 81, '8.8.8.8', '2026-05-12 01:09:10.000000', 0, 'CRITICO'),
(277, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, 'google.com', '2026-05-12 01:32:31.000000', 0, 'CRITICO'),
(278, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, '8.8.8.8', '2026-05-12 01:33:33.000000', 0, 'CRITICO'),
(279, 6.578947368421052, 22, 4.32, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 22, 36, 17, 'google.com', '2026-05-14 18:47:33.000000', 0, 'CRITICO'),
(280, 4.473684210526316, 61.15, 4.32, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 61.15, 76, 58, 'google.com', '2026-05-14 18:48:03.000000', 0, 'ESTABLE'),
(281, 4.2631578947368425, 55.1, 4.33, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 55.1, 63, 52, 'google.com', '2026-05-14 19:02:50.000000', 0, 'ESTABLE'),
(282, 4.315789473684211, 87.35, 4.31, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 87.35, 95, 84, 'clinicacrecer.com', '2026-05-14 19:12:46.000000', 0, 'ESTABLE'),
(283, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'MUY_MALO', 0, 0, 0, 'netflix.com', '2026-05-14 19:14:52.000000', 0, 'CRITICO'),
(284, 1.1578947368421053, 85.1, 4.35, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 85.1, 88, 84, 'clinicacrecer.com', '2026-05-14 19:21:23.000000', 0, 'ESTABLE'),
(285, 1.631578947368421, 61.1, 4.36, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 61.1, 65, 60, 'google.com', '2026-05-14 19:25:10.000000', 0, 'ESTABLE'),
(286, 6.421052631578948, 89.7, 4.28, 0, 0, 20, 20, 'ICMP', 'BUENO', 89.7, 108, 84, 'clinicacrecer.com', '2026-05-14 19:25:56.000000', 0, 'ESTABLE'),
(287, 31.57894736842105, 52.8, 3.87, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 52.8, 340, 21, 'google.com', '2026-05-14 20:05:07.000000', 0, 'ESTABLE'),
(288, 63.526315789473685, 189.2, 2.81, 0, 0, 20, 20, 'ICMP', 'MUY MALO', 189.2, 740, 89, 'clinicacrecer.com', '2026-05-14 20:05:44.000000', 0, 'CRITICO'),
(289, 4.2105263157894735, 66.65, 4.32, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 66.65, 82, 63, 'google.com', '2026-05-15 19:26:40.000000', 0, NULL),
(290, 3.1578947368421053, 48, 4.35, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 48, 60, 45, 'clinicacrecer.com', '2026-05-15 19:28:39.000000', 0, NULL),
(291, 34.68421052631579, 129.3, 3.72, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 129.3, 214, 71, 'google.com', '2026-05-16 16:18:19.000000', 0, 'EN RIESGO'),
(292, 30.894736842105264, 143.25, 3.79, 0, 0, 20, 20, 'ICMP', 'ACEPTABLE', 143.25, 225, 85, 'google.com', '2026-05-16 16:19:32.000000', 0, 'ESTABLE'),
(293, 2.5789473684210527, 49.5, 4.36, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 49.5, 62, 46, 'clinicacrecer.com', '2026-05-16 17:08:13.000000', 0, 'ESTABLE'),
(294, 4.157894736842105, 75.2, 4.32, 0, 0, 20, 20, 'ICMP', 'EXCELENTE', 75.2, 89, 72, 'google.com', '2026-05-17 00:09:54.000000', 0, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles`
--

CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL,
  `usuario_id` bigint(20) NOT NULL,
  `rol` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `roles`
--

INSERT INTO `roles` (`id`, `usuario_id`, `rol`) VALUES
(1, 1, 'ROLE_ADMIN'),
(10, 15, 'ROLE_USER'),
(11, 16, 'ROLE_USER'),
(12, 17, 'ROLE_USER'),
(29, 7, 'ROLE_USER'),
(31, 2, 'ROLE_USER'),
(32, 13, 'ROLE_ADMIN'),
(33, 18, 'ROLE_USER'),
(34, 19, 'ROLE_USER');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` bigint(20) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `apellido` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `ultimo_acceso` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `username`, `password`, `nombre`, `apellido`, `email`, `ultimo_acceso`) VALUES
(1, 'admin', '$2a$10$keIUWAbqTRb/IUWpVM8Y6Ox5cD0UAlg0RrIHF9C/CKO9OwE6.wkIi', 'Admin', 'Admin', 'admin@gmail.com', '2025-05-08 00:06:43'),
(2, 'user', '$2a$10$K8zGSKFCbPv03aYJAwg2F.5sunFohzbC/rXUuDWGKbZnwgMVbQhLG', 'User', 'User', 'user@gmail.com', '2025-05-08 00:06:23'),
(7, 'michell', '$2a$10$ikP5W5gFuB5NQd8smgRqiueqaqAoRbVRlH6WqEu0veryGLlacHr1u', 'Michel', 'Babilonia', 'michel@gmail.com', '2025-05-07 04:05:03'),
(13, 'maidy', '$2a$10$iZlHeHqizqPL9I0sfKumyO.EXNfKnd9lViw7whqLE/Tlu.1lH8FJG', 'maidy', 'perez', 'maidy@gmail.com', '2025-05-10 16:32:00'),
(15, 'luna', '$2a$10$60Vx652gVf.Z/W.6mRvYOeazQizDZBWU0L5Ix3IS.oKPu.7ddTuAq', 'Luna', 'suarez', 'luna@gmail.com', '2025-05-10 16:39:08'),
(16, 'chaida', '$2a$10$oP3IN7ak.j3/0bp2hSRY7uy5L5JgZBhXbsa/EhmRukU4AGiVYK1GC', 'chaida', 'zarza', 'chaida@gmial.com', '2025-05-12 17:41:46'),
(17, 'johan', '$2a$10$iILvFs2PdTrMCXSBnBVWGOi5poCqj1IQCV6arf0ocfnf4E5IoqVJS', 'johan', 'giraldo', 'Johang@gmail.com', '2025-05-21 18:42:04'),
(18, 'carmen', '$2a$10$pdkqJZ76IRYpH.A129YKqeKhVlZfug5Y3E.usUsi8dAVNTELHj8yi', 'Carmen', 'Rodriguez', 'carmen@gmail.com', '2026-05-14 18:46:05'),
(19, 'miguel', '$2a$10$e7gLk5btSzcA9JobLGPWdekKsGsXn/Y5l7PP7yCwzfoUWK7QMfjii', 'miguel', 'suarez', 'miguel@gmail.com', '2026-05-14 19:12:08');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `conversaciones`
--
ALTER TABLE `conversaciones`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_usuario_created` (`usuario_id`,`created_at`);

--
-- Indices de la tabla `dispositivos`
--
ALTER TABLE `dispositivos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ip` (`ip`);

--
-- Indices de la tabla `eventos`
--
ALTER TABLE `eventos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `usuario_id` (`usuario_id`);

--
-- Indices de la tabla `macs_autorizadas`
--
ALTER TABLE `macs_autorizadas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKr654710c64pi62ndfxjaw3bf6` (`mac`);

--
-- Indices de la tabla `qos_metrics`
--
ALTER TABLE `qos_metrics`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`),
  ADD KEY `usuario_id` (`usuario_id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `conversaciones`
--
ALTER TABLE `conversaciones`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `dispositivos`
--
ALTER TABLE `dispositivos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=142;

--
-- AUTO_INCREMENT de la tabla `eventos`
--
ALTER TABLE `eventos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=89;

--
-- AUTO_INCREMENT de la tabla `macs_autorizadas`
--
ALTER TABLE `macs_autorizadas`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `qos_metrics`
--
ALTER TABLE `qos_metrics`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=295;

--
-- AUTO_INCREMENT de la tabla `roles`
--
ALTER TABLE `roles`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `conversaciones`
--
ALTER TABLE `conversaciones`
  ADD CONSTRAINT `FKrw3gy1diqh91ncvelefvonbjk` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`);

--
-- Filtros para la tabla `eventos`
--
ALTER TABLE `eventos`
  ADD CONSTRAINT `eventos_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `roles`
--
ALTER TABLE `roles`
  ADD CONSTRAINT `roles_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
