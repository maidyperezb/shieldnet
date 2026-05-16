-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 30-04-2026 a las 21:09:12
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
(3, '192.168.1.1', '2026-04-30 19:09:07', 'AUTORIZADO', '74:54:6B:07:43:28'),
(8, '192.168.1.35', '2026-04-29 23:43:56', 'AUTORIZADO', '64:6C:80:10:7B:33'),
(9, '192.168.1.33', '2026-04-30 19:08:38', 'AUTORIZADO', 'CE:50:F0:FA:1B:B1'),
(12, '192.168.1.34', '2026-04-29 23:43:56', 'DESCONOCIDO', '4C:57:39:83:67:B0'),
(13, '192.168.1.43', '2026-04-30 19:09:07', 'AUTORIZADO', '64:6C:80:10:7B:33'),
(14, '192.168.1.39', '2026-04-30 18:59:41', 'DESCONOCIDO', '9A:5E:0D:EF:98:AB');

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
  `usuario_id` bigint(20) NOT NULL
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
(34, 'google.com', '14:08:32', '2026-04-30', 'Escaneo de puertos a google.com (1-1000)', 1);

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
(4, '', '2026-04-29 22:54:01.000000', '58:36:53:0B:DC:88');

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
  `during_active_call` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `qos_metrics`
--

INSERT INTO `qos_metrics` (`id`, `jitter_ms`, `latency_ms`, `mos_score`, `packet_loss_percent`, `packets_lost`, `packets_received`, `packets_sent`, `protocol_type`, `quality_status`, `rtt_avg_ms`, `rtt_max_ms`, `rtt_min_ms`, `target_ip`, `timestamp`, `during_active_call`) VALUES
(12, 15.473684210526315, 32.35, 4.19, 0, 0, 20, 20, 'ICMP', 'GOOD', 32.35, 161, 24, '8.8.8.8', '2025-11-15 21:43:46.000000', 0),
(17, 1.55, 91.28571428571429, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELLENT', 91.28571428571429, 97, 90, 'google.com', '2025-11-15 22:00:29.000000', 0),
(19, 1.55, 16.476190476190474, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELLENT', 16.476190476190474, 24, 15, 'youtube.com', '2025-11-15 22:03:52.000000', 0),
(20, 6.25, 18.476190476190474, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELLENT', 18.476190476190474, 34, 14, 'google.com', '2025-11-16 04:42:28.000000', 0),
(22, 8.35, 32, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELLENT', 32, 71, 23, '8.8.8.8', '2025-11-16 05:37:58.000000', 0),
(23, 6, 95.76190476190476, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELLENT', 95.76190476190476, 129, 90, 'google.com', '2025-11-16 05:42:51.000000', 0),
(24, 689.2, 579.7272727272727, 1, -10, -1, 11, 10, 'ICMP', 'BAD', 579.7272727272727, 3256, 95, '8.8.8.8', '2025-11-23 15:32:58.000000', 0),
(25, 293.375, 329, 1, 10, 1, 9, 10, 'ICMP', 'BAD', 329, 1522, 89, '8.8.8.8', '2025-11-23 15:33:26.000000', 0),
(26, 208.8, 301.3636363636364, 1, -10, -1, 11, 10, 'ICMP', 'BAD', 301.3636363636364, 892, 99, '8.8.8.8', '2025-11-23 15:33:46.000000', 0),
(27, 62.7, 133.9090909090909, 4.15, -10, -1, 11, 10, 'ICMP', 'GOOD', 133.9090909090909, 280, 81, '8.8.8.8', '2025-11-23 15:34:06.000000', 0),
(28, 387.2, 395.09090909090907, 1, -10, -1, 11, 10, 'ICMP', 'BAD', 395.09090909090907, 1309, 61, '8.8.8.8', '2025-11-23 15:35:08.000000', 0),
(29, 446.2, 360.54545454545456, 1, -10, -1, 11, 10, 'ICMP', 'BAD', 360.54545454545456, 1664, 63, '8.8.8.8', '2025-11-23 15:35:29.000000', 0),
(30, 25.1, 105.33333333333333, 4.35, -5, -1, 21, 20, 'ICMP', 'EXCELLENT', 105.33333333333333, 158, 72, 'google.com', '2025-11-23 15:42:10.000000', 0),
(31, 42.7, 107.72727272727273, 4.43, -10, -1, 11, 10, 'ICMP', 'EXCELLENT', 107.72727272727273, 180, 81, 'google.com', '2025-11-23 15:43:47.000000', 0),
(32, 25.894736842105264, 89.85, 3.95, 0, 0, 20, 20, 'ICMP', 'FAIR', 89.85, 184, 73, 'google.com', '2025-11-24 08:31:00.000000', 0),
(33, 5, 50.095238095238095, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELLENT', 50.095238095238095, 74, 46, 'clinicacrecer.com', '2025-11-24 14:19:25.000000', 0),
(34, 4.894736842105263, 49.7, 4.33, 0, 0, 20, 20, 'ICMP', 'EXCELLENT', 49.7, 63, 46, 'clinicacrecer.com', '2025-11-24 14:19:39.000000', 0),
(35, 3.1, 20.818181818181817, 4.5, -10, -1, 11, 10, 'ICMP', 'EXCELLENT', 20.818181818181817, 28, 17, '8.8.8.8', '2025-11-24 21:13:10.000000', 0),
(36, 0.9, 23.363636363636363, 4.5, -10, -1, 11, 10, 'ICMP', 'EXCELLENT', 23.363636363636363, 25, 22, '8.8.8.8', '2025-11-24 21:14:46.000000', 0),
(37, 29.4, 46.36363636363637, 4.5, -10, -1, 11, 10, 'ICMP', 'EXCELLENT', 46.36363636363637, 278, 18, '8.8.8.8', '2025-11-24 21:15:18.000000', 0),
(38, 59.8, 164.52380952380952, 3.66, -5, -1, 21, 20, 'ICMP', 'FAIR', 164.52380952380952, 361, 55, 'clinicacrecer.com', '2025-11-24 21:18:27.000000', 0),
(39, 0, 0, 1, 100, 0, 0, 0, 'ICMP', 'BAD', 0, 0, 0, '172.30.252.1', '2025-11-24 21:22:24.000000', 0),
(40, 2.5, 29.333333333333332, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELLENT', 29.333333333333332, 38, 26, 'google.com', '2026-02-17 22:40:13.000000', 0),
(41, 5.1, 25.428571428571427, 4.5, -5, -1, 21, 20, 'ICMP', 'EXCELLENT', 25.428571428571427, 36, 21, '10.244.19.197', '2026-03-18 19:58:57.000000', 0),
(42, 78.94444444444444, 186.52631578947367, 1.81, 5, 1, 19, 20, 'ICMP', 'BAD', 186.52631578947367, 1006, 51, 'clinicacrecer.com', '2026-03-18 20:24:30.000000', 0),
(43, 50.578947368421055, 52.35, 3.43, 0, 0, 20, 20, 'ICMP', 'POOR', 52.35, 272, 17, 'google.com', '2026-03-21 15:53:00.000000', 0),
(44, 69.89473684210526, 81.15, 2.9, 0, 0, 20, 20, 'ICMP', 'BAD', 81.15, 876, 16, 'google.com', '2026-04-29 18:01:47.000000', 0),
(45, 16.444444444444443, 127.3, 4.09, 0, 0, 10, 10, 'ICMP', 'GOOD', 127.3, 168, 98, 'clinicacrecer.com', '2026-04-29 18:02:35.000000', 0),
(46, 95.66666666666667, 299.9, 1.5, 0, 0, 10, 10, 'ICMP', 'BAD', 299.9, 831, 180, 'hospifood.com', '2026-04-29 18:16:54.000000', 0),
(47, 49.94736842105263, 160.85, 3.31, 0, 0, 20, 20, 'ICMP', 'POOR', 160.85, 670, 81, 'google.com', '2026-04-29 18:18:09.000000', 0),
(48, 5, 67.5, 4.31, 0, 0, 20, 20, 'ICMP', 'EXCELLENT', 67.5, 84, 62, 'google.com', '2026-04-30 17:01:13.000000', 0),
(49, 28.263157894736842, 109.45, 3.88, 0, 0, 20, 20, 'ICMP', 'FAIR', 109.45, 183, 60, 'google.com', '2026-04-30 19:01:58.000000', 0),
(50, 45.26315789473684, 122.65, 3.48, 0, 0, 20, 20, 'ICMP', 'POOR', 122.65, 444, 68, 'clinicacrecer.com', '2026-04-30 19:05:24.000000', 0);

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
(9, 14, 'ROLE_USER'),
(10, 15, 'ROLE_USER'),
(11, 16, 'ROLE_USER'),
(12, 17, 'ROLE_USER'),
(29, 7, 'ROLE_USER'),
(31, 2, 'ROLE_USER'),
(32, 13, 'ROLE_ADMIN');

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
(14, 'miguel', '$2a$10$ORWuh7f7Tcm1wMGstPzGu.RkQb8TfwEd2c0RwcmkivPaVxfeyW.Ya', 'miguel', 'perez', 'miguel@gmail.com', '2025-05-10 16:32:45'),
(15, 'luna', '$2a$10$60Vx652gVf.Z/W.6mRvYOeazQizDZBWU0L5Ix3IS.oKPu.7ddTuAq', 'Luna', 'suarez', 'luna@gmail.com', '2025-05-10 16:39:08'),
(16, 'chaida', '$2a$10$oP3IN7ak.j3/0bp2hSRY7uy5L5JgZBhXbsa/EhmRukU4AGiVYK1GC', 'chaida', 'zarza', 'chaida@gmial.com', '2025-05-12 17:41:46'),
(17, 'johan', '$2a$10$iILvFs2PdTrMCXSBnBVWGOi5poCqj1IQCV6arf0ocfnf4E5IoqVJS', 'johan', 'giraldo', 'Johang@gmail.com', '2025-05-21 18:42:04');

--
-- Índices para tablas volcadas
--

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
-- AUTO_INCREMENT de la tabla `dispositivos`
--
ALTER TABLE `dispositivos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `eventos`
--
ALTER TABLE `eventos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT de la tabla `macs_autorizadas`
--
ALTER TABLE `macs_autorizadas`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `qos_metrics`
--
ALTER TABLE `qos_metrics`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=51;

--
-- AUTO_INCREMENT de la tabla `roles`
--
ALTER TABLE `roles`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- Restricciones para tablas volcadas
--

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
